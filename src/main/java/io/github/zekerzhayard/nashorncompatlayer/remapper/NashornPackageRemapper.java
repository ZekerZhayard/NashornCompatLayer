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

package io.github.zekerzhayard.nashorncompatlayer.remapper;

import java.lang.invoke.MethodType;
import java.util.Objects;

import io.github.zekerzhayard.nashorncompatlayer.CheckedLambdaUtils;
import org.objectweb.asm.commons.Remapper;
import org.openjdk.nashorn.api.scripting.NashornScriptEngine;

// Code from: https://github.com/TabooLib/taboolib-gradle-plugin/blob/7bd4e0f87e95c429045aede3593f4adbdfd1656f/src/main/groovy/io/izzel/taboolib/gradle/RelocateRemapper.groovy
public class NashornPackageRemapper extends Remapper {
    final static Module NASHORN_MODULE = NashornScriptEngine.class.getModule();

    boolean hasRemapped = false;

    String remapNashornPackage(String name, boolean shouldStartWith) {
        if (name.length() >= 12) {
            if (shouldStartWith) {
                return switch (name.substring(0, 12)) {
                    case "jdk.nashorn." -> this.setRemapped("org.openjdk.nashorn." + name.substring(12), true);
                    case "jdk/nashorn/" -> this.setRemapped("org/openjdk/nashorn/" + name.substring(12), true);
                    default -> name;
                };
            } else if (name.contains("jdk.nashorn.")) {
                return this.setRemapped(name.replace("jdk.nashorn.", "org.openjdk.nashorn.").replace("org.openorg.openjdk.nashorn.", "org.openjdk.nashorn."), false);
            } else if (name.contains("jdk/nashorn/")) {
                return this.setRemapped(name.replace("jdk/nashorn/", "org/openjdk/nashorn/").replace("org/openorg/openjdk/nashorn/", "org/openjdk/nashorn/"), false);
            }
        }
        return name;
    }

    String setRemapped(String name, boolean shouldStartWith) {
        this.hasRemapped = true;
        if (shouldStartWith) {
            // Export packages.
            CheckedLambdaUtils.wrapFunction(name.replace("/", "."), dotName ->
                RemapperEntrance.IMPL_LOOKUP.findVirtual(Module.class, "implAddExportsToAllUnnamed", MethodType.methodType(void.class, String.class))
                    .invoke(NASHORN_MODULE, dotName.substring(0, dotName.lastIndexOf("."))));
        }
        return name;
    }

    public boolean hasRemapped() {
        return this.hasRemapped;
    }

    @Override
    public Object mapValue(final Object value) {
        if (value instanceof String) {
            return this.remapNashornPackage((String) value, false);
        }
        return value;
    }

    @Override
    public String mapSignature(final String signature, final boolean typeSignature) {
        if (Objects.equals(signature, "")) {
            return "";
        }
        return super.mapSignature(signature, typeSignature);
    }

    @Override
    public String mapPackageName(final String name) {
        return this.remapNashornPackage(name, true);
    }

    @Override
    public String map(final String internalName) {
        return this.remapNashornPackage(internalName, true);
    }
}
