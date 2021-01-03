/*
 * Copyright (C) 2020-2021  ZekerZhayard
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

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.module.Configuration;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.jar.Manifest;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.ITransformer;
import io.github.zekerzhayard.nashorncompatlayer.remapper.RemapperEntrance;
import jdk.internal.loader.BuiltinClassLoader;
import jdk.internal.misc.Unsafe;
import net.minecraftforge.fml.loading.LibraryFinder;
import org.apache.commons.lang3.ArrayUtils;

public class NashornCompatLayerEntrance implements ITransformationService {

    // bootstrap

    private final static String ASM_MODULE_NAME = "org.objectweb.asm";
    private final static String NASHORN_MODULE_NAME = "org.openjdk.nashorn";

    static {
        bootstrap();
        RemapperEntrance.bootstrap();
    }

    @SuppressWarnings("unchecked")
    private static void bootstrap() {
        try {
            // All members in java.lang.Module is in the jdk.internal.reflect.Reflection#fieldFilterMap,
            // so we should use method lookup instead of reflection.
            CheckedLambdaUtils.wrapBiConsumerWithIterable(
                MethodHandles.privateLookupIn(Module.class, MethodHandles.lookup())
                    .findVirtual(Module.class, "implAddExportsToAllUnnamed", MethodType.methodType(void.class, String.class)),
                Set.of("jdk.internal.loader", "jdk.internal.misc"),
                (mh, s) -> mh.invoke(Object.class.getModule(), s)
            );

            CheckedLambdaUtils.wrapBiConsumer(
                CheckedLambdaUtils.wrapBiFunction(
                    Unsafe.getUnsafe(),
                    MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP"),
                    (unsafe, field) -> (MethodHandles.Lookup) unsafe.getReference(unsafe.staticFieldBase(field), unsafe.staticFieldOffset(field))
                ),

                // Search nashorn in the jar.
                CheckedLambdaUtils.wrapLazyFunction(
                    NashornCompatLayerEntrance.class.getProtectionDomain().getCodeSource().getLocation().toURI(),
                    uri -> FileSystems.newFileSystem(URI.create("jar:" + uri), Map.of("create", "true")),
                    (URI uri, FileSystem zipfs) -> {
                        try (var is = Files.newInputStream(zipfs.getPath("/META-INF/MANIFEST.MF"))) {
                            return ModuleFinder.compose(
                                ModuleFinder.of(Paths.get(uri)),
                                ModuleFinder.of(Stream.of(new Manifest(is).getMainAttributes().getValue("Dependencies").split("\\s+")).map(zipfs::getPath).toArray(Path[]::new))
                            );
                        }
                    }
                ),

                (lookup, finder) -> {
                    CheckedLambdaUtils.wrapBiConsumerWithIterable(
                        lookup,
                        finder.findAll(),
                        (_lookup, mr) -> {
                            ((BuiltinClassLoader) (!mr.descriptor().name().startsWith(NASHORN_MODULE_NAME) ? ClassLoader.getSystemClassLoader() : ClassLoader.getPlatformClassLoader())).loadModule(mr);

                            // We need to remove all asm requirements because asm jars are initialized too early so that they are recognized as unnamed modules.
                            _lookup.findSetter(ModuleDescriptor.class, "requires", Set.class)
                                .invoke(mr.descriptor(), Set.of(mr.descriptor().requires().stream().filter(r -> !r.name().startsWith(ASM_MODULE_NAME)).toArray(ModuleDescriptor.Requires[]::new)));

                            // Change nashorn module to open module so that other mods can reflect into it.
                            _lookup.findSetter(ModuleDescriptor.class, "modifiers", Set.class)
                                .invoke(mr.descriptor(), Set.of(mr.descriptor().modifiers().toArray(ModuleDescriptor.Modifier[]::new), ModuleDescriptor.Modifier.OPEN));
                            _lookup.findSetter(ModuleDescriptor.class, "open", boolean.class)
                                .invoke(mr.descriptor(), true);
                        }
                    );

                    // Make sure nashorn was loaded in platform class loader.
                    CheckedLambdaUtils.wrapBiConsumer(
                        lookup,
                        Class.forName("jdk.internal.module.ModuleLoaderMap$Modules", false, ClassLoader.getSystemClassLoader()),
                        (_lookup, c) -> Objects.requireNonNull(_lookup.findStaticSetter(c, "platformModules", Set.class))
                            .invoke(Set.of(ArrayUtils.add(((Set<String>) _lookup.findStaticGetter(c, "platformModules", Set.class).invoke()).toArray(String[]::new), NASHORN_MODULE_NAME)))
                    );

                    // Define the nashorn module and add reads.
                    CheckedLambdaUtils.wrapBiConsumer(
                        lookup,
                        Configuration.resolveAndBind(finder, List.of(ModuleLayer.boot().configuration()), finder, Set.of(NASHORN_MODULE_NAME)),
                        (_lookup, config) -> _lookup.findVirtual(Module.class, "implAddReadsAllUnnamed", MethodType.methodType(void.class))
                            .invoke(ModuleLayer.defineModules(
                                config,
                                List.of(ModuleLayer.boot()),
                                (Function<String, ClassLoader>) _lookup.findConstructor(
                                    Class.forName("jdk.internal.module.ModuleLoaderMap$Mapper", false, ClassLoader.getSystemClassLoader()),
                                    MethodType.methodType(void.class, Configuration.class)
                                ).invoke(config)
                            ).layer().findModule(NASHORN_MODULE_NAME).orElseThrow())
                    );
                }
            );
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }


    // implement ITransformationService

    @Nonnull
    @Override
    public String name() {
        return "NashornCompatLayer";
    }

    @Override
    public void initialize(@Nonnull IEnvironment environment) {

    }

    @Override
    public void beginScanning(@Nonnull IEnvironment environment) {

    }

    // Forge uses asm Opcodes.class to locate the libraries folder, but this mod bundles new asm libraries,
    // so we need to define the correct path before it was used.
    @Override
    public void onLoad(@Nonnull IEnvironment env, @Nonnull Set<String> otherServices) {
        try {
            MethodHandles.privateLookupIn(LibraryFinder.class, MethodHandles.lookup())
                .findStaticSetter(LibraryFinder.class, "libsPath", Path.class)
                .invoke(Paths.get(ITransformationService.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                    //<version> /modlauncher/mods       /cpw        /libraries
                    .getParent().getParent().getParent().getParent().getParent());
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @Nonnull
    @Override
    @SuppressWarnings("rawtypes")
    public List<ITransformer> transformers() {
        return List.of();
    }
}
