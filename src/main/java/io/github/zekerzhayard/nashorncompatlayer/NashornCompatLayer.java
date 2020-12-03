/*
 * Copyright (C) 2020  ZekerZhayard
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package io.github.zekerzhayard.nashorncompatlayer;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.module.Configuration;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.script.ScriptEngine;

import cpw.mods.modlauncher.api.LamdbaExceptionUtils;
import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import jdk.internal.loader.BuiltinClassLoader;
import jdk.internal.loader.URLClassPath;
import jdk.nashorn.api.scripting.NashornScriptEngine;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.objectweb.asm.Type;
import sun.misc.Unsafe;

/**
 * We use {@link ILaunchPluginService} to make sure this will be displayed in crash reports and prevent this from being loaded as a mod.
 */
public class NashornCompatLayer implements ILaunchPluginService {

    // utilities methods

    public static ScriptEngine convertScriptEngine(ScriptEngine engine) {
        if (engine instanceof org.openjdk.nashorn.api.scripting.NashornScriptEngine) {
            return new NashornScriptEngine((org.openjdk.nashorn.api.scripting.NashornScriptEngine) engine);
        } else if (engine instanceof NashornScriptEngine) {
            return ((NashornScriptEngine) engine).instance;
        }
        return engine;
    }

    public static Object convertScriptObjectMirror(Object object) {
        if (object instanceof org.openjdk.nashorn.api.scripting.ScriptObjectMirror) {
            return new ScriptObjectMirror((org.openjdk.nashorn.api.scripting.ScriptObjectMirror) object);
        } else if (object instanceof ScriptObjectMirror) {
            return ((ScriptObjectMirror) object).instance;
        }
        return object;
    }


    // bootstrap

    private final static ClassLoader SYSTEM_CLASS_LOADER = ClassLoader.getSystemClassLoader();
    private final static ModuleLayer BOOT_MODULE_LAYER = ModuleLayer.boot();
    private final static String NASHORN_MODULE_NAME = "org.openjdk.nashorn";

    private final static MethodHandle REQUIRES_SETTER;
    private final static MethodHandles.Lookup IMPL_LOOKUP;
    private final static Module ALL_UNNAMED_MODULE;
    private final static ModuleFinder NASHORN_MODULE_FINDER;

    static {
        try {
            var unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            var unsafe = (Unsafe) unsafeField.get(null);
            var implLookupField = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            IMPL_LOOKUP =  (MethodHandles.Lookup) unsafe.getObject(unsafe.staticFieldBase(implLookupField), unsafe.staticFieldOffset(implLookupField));

            // All members in java.lang.Module is in the jdk.internal.reflect.Reflection#fieldFilterMap,
            // so we should use method lookup instead of reflection.
            ALL_UNNAMED_MODULE = (Module) IMPL_LOOKUP.findStaticGetter(Module.class, "ALL_UNNAMED_MODULE", Module.class).invoke();
            REQUIRES_SETTER = IMPL_LOOKUP.findSetter(ModuleDescriptor.class, "requires", Set.class);

            // Unlock module limitation.
            IMPL_LOOKUP.findVirtual(Module.class, "implAddExportsToAllUnnamed", MethodType.methodType(void.class, String.class))
                .invoke(BOOT_MODULE_LAYER.findModule("java.base").orElseThrow(), "jdk.internal.loader");

            // Search nashorn in class path.
            NASHORN_MODULE_FINDER = ModuleFinder.of(
                ((ArrayList<?>) IMPL_LOOKUP.findGetter(URLClassPath.class, "path", ArrayList.class)
                    .invoke(IMPL_LOOKUP.findGetter(SYSTEM_CLASS_LOADER.getClass(), "ucp", URLClassPath.class).invoke(SYSTEM_CLASS_LOADER))
                ).stream()
                    .map(url -> LamdbaExceptionUtils.uncheck(_url -> Paths.get(_url.toURI()), (URL) url))
                    .filter(p -> p.getFileName().toString().startsWith("nashorn-core-"))
                    .toArray(Path[]::new)
            );

            NASHORN_MODULE_FINDER.findAll().forEach(mref -> {
                try {
                    var desc = mref.descriptor();

                    // We need to remove all asm requirements because asm jars are initialized too early so that they are recognized as unnamed modules.
                    REQUIRES_SETTER.invoke(desc, Set.of(desc.requires().stream().filter(r -> !r.name().startsWith("org.objectweb.asm")).toArray(ModuleDescriptor.Requires[]::new)));

                    // Define the nashorn module and add reads
                    var controller = ModuleLayer.defineModules(
                        Configuration.resolveAndBind(NASHORN_MODULE_FINDER, List.of(BOOT_MODULE_LAYER.configuration()), NASHORN_MODULE_FINDER, Set.of(NASHORN_MODULE_NAME)),
                        List.of(BOOT_MODULE_LAYER),
                        name -> SYSTEM_CLASS_LOADER
                    );
                    controller.addReads(controller.layer().findModule(NASHORN_MODULE_NAME).orElseThrow(), ALL_UNNAMED_MODULE);

                    ((BuiltinClassLoader) SYSTEM_CLASS_LOADER).loadModule(mref);
                } catch (Throwable t) {
                    throw new RuntimeException(t);
                }
            });
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }


    // implement ILaunchPluginService

    @Override
    public String name() {
        return "NashornCompatLayer";
    }

    // This method is useless for us.
    @Override
    public EnumSet<Phase> handlesClass(Type classType, boolean isEmpty) {
        return EnumSet.noneOf(Phase.class);
    }
}
