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

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.module.Configuration;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.ITransformer;
import jdk.internal.loader.BuiltinClassLoader;
import jdk.internal.misc.Unsafe;
import net.minecraftforge.fml.loading.LibraryFinder;

public class NashornCompatLayerEntrance implements ITransformationService {

    // bootstrap

    private final static String NASHORN_MODULE_NAME = "org.openjdk.nashorn";

    static {
        try {
            // All members in java.lang.Module is in the jdk.internal.reflect.Reflection#fieldFilterMap,
            // so we should use method lookup instead of reflection.
            CheckedLambdaUtils.wrapBiConsumerWithIterable(
                MethodHandles.privateLookupIn(Module.class, MethodHandles.lookup())
                    .findVirtual(Module.class, "implAddExportsToAllUnnamed", MethodType.methodType(void.class, String.class)),
                Module.class.getModule(),
                Set.of("jdk.internal.loader", "jdk.internal.misc"),
                (mh, m, s) -> mh.invoke(m, s)
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
                    // We need to remove all asm requirements because asm jars are initialized too early so that they are recognized as unnamed modules.
                    CheckedLambdaUtils.wrapBiConsumer(
                        lookup,
                        finder.findAll().stream()
                            .peek(((BuiltinClassLoader) ClassLoader.getSystemClassLoader())::loadModule)
                            .collect(Collectors.toSet())
                            .stream()
                            .map(ModuleReference::descriptor)
                            .filter(d -> d.name().equals(NASHORN_MODULE_NAME))
                            .findFirst().orElseThrow(),
                        (_lookup, desc) -> _lookup.findVarHandle(ModuleDescriptor.class, "requires", Set.class)
                            .set(desc, Set.of(desc.requires().stream().filter(r -> !r.name().startsWith("org.objectweb.asm")).toArray(ModuleDescriptor.Requires[]::new)))
                    );

                    // Define the nashorn module and add reads
                    lookup.findVirtual(Module.class, "implAddReadsAllUnnamed", MethodType.methodType(void.class))
                        .invoke(ModuleLayer.defineModules(
                            Configuration.resolveAndBind(finder, List.of(ModuleLayer.boot().configuration()), finder, Set.of(NASHORN_MODULE_NAME)),
                            List.of(ModuleLayer.boot()),
                            name -> ClassLoader.getSystemClassLoader()
                        ).layer().findModule(NASHORN_MODULE_NAME).orElseThrow());
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
