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

package io.github.zekerzhayard.nashorncompatlayer.remapper;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import cpw.mods.modlauncher.LaunchPluginHandler;
import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.ITransformerActivity;
import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import io.github.zekerzhayard.nashorncompatlayer.CheckedLambdaUtils;
import jdk.internal.misc.Unsafe;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.tree.ClassNode;

public class RemapperEntrance implements ILaunchPluginService {
    final static MethodHandles.Lookup IMPL_LOOKUP = CheckedLambdaUtils.wrapBiFunction(
        Unsafe.getUnsafe(),
        CheckedLambdaUtils.wrapSupplier(() -> MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP")),
        (unsafe, field) -> (MethodHandles.Lookup) unsafe.getReference(unsafe.staticFieldBase(field), unsafe.staticFieldOffset(field))
    );

    @SuppressWarnings("unchecked")
    public static void bootstrap() {
        CheckedLambdaUtils.wrapConsumer(
            new RemapperEntrance(),
            plugin -> {
                ((Map<String, ILaunchPluginService>) IMPL_LOOKUP.findGetter(LaunchPluginHandler.class, "plugins", Map.class).invoke(
                    IMPL_LOOKUP.findGetter(Launcher.class, "launchPlugins", LaunchPluginHandler.class).invoke(Launcher.INSTANCE)
                )).put(plugin.name(), plugin);

                Launcher.INSTANCE.environment().getProperty(IEnvironment.Keys.MODLIST.get()).orElseThrow().add(new HashMap<>(Map.of(
                    "name", plugin.name(),
                    "type", "PLUGINSERVICE",
                    "file", CheckedLambdaUtils.wrapFunction(plugin.getClass().getProtectionDomain().getCodeSource().getLocation().getFile(), f -> f.substring(f.lastIndexOf("/")))
                )));
            }
        );
    }

    static void replaceFields(Object src, Object dest, Class<?> clz) {
        if (!Objects.equals(src.getClass(), dest.getClass()) || !clz.isInstance(src)) {
            throw new RuntimeException(String.format("Unable to replace fields between different classes (source class: %s, dest class: %s, target class: %s)!", src.getClass().getName(), dest.getClass().getName(), clz.getName()));
        }

        // Use getDeclaredFields0 instead of getDeclaredFields to avoid filtering fields.
        Stream.of(CheckedLambdaUtils.wrapFunction(clz, clazz -> (Field[]) IMPL_LOOKUP.findVirtual(Class.class, "getDeclaredFields0", MethodType.methodType(Field[].class, boolean.class)).invoke(clazz, false)))
            .filter(f -> (f.getModifiers() | Modifier.STATIC) != f.getModifiers())
            .forEach((CheckedLambdaUtils.CheckedConsumer<Field>) f -> Objects.requireNonNull(IMPL_LOOKUP.findSetter(f.getDeclaringClass(), f.getName(), f.getType()))
                .invoke(dest, IMPL_LOOKUP.findGetter(f.getDeclaringClass(), f.getName(), f.getType()).invoke(src)));

        Optional.ofNullable(clz.getSuperclass()).ifPresent(cl -> replaceFields(src, dest, cl));
    }

    // implement ILaunchPluginService

    @Override
    public String name() {
        return "NashornCompatLayerRemapper";
    }

    @Override
    public EnumSet<Phase> handlesClass(Type classType, boolean isEmpty) {
        return this.handlesClass(classType, isEmpty, null);
    }

    @Override
    public EnumSet<Phase> handlesClass(Type classType, boolean isEmpty, String reason) {
        return isEmpty || !Objects.equals(reason, ITransformerActivity.CLASSLOADING_REASON) ? EnumSet.noneOf(Phase.class) : EnumSet.of(Phase.AFTER);
    }

    @Override
    public boolean processClass(Phase phase, ClassNode classNode, Type classType, String reason) {
        var newClassNode = new ClassNode();
        var remapper = new NashornPackageRemapper();
        classNode.accept(new ClassRemapper(newClassNode, remapper));

        if (!remapper.hasRemapped()) {
            return false;
        }

        replaceFields(newClassNode, classNode, ClassNode.class);
        return true;
    }
}
