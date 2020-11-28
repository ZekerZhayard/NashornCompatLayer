/*
 * Copyright (c) 2010, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package jdk.nashorn.api.scripting;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.script.Bindings;

import io.github.zekerzhayard.nashorncompatlayer.NashornCompatLayer;
import org.openjdk.nashorn.api.scripting.AbstractJSObject;

public final class ScriptObjectMirror extends AbstractJSObject implements Bindings {
    public final org.openjdk.nashorn.api.scripting.ScriptObjectMirror instance;

    public ScriptObjectMirror(org.openjdk.nashorn.api.scripting.ScriptObjectMirror instance) {
        this.instance = instance;
    }

    @Override
    public boolean equals(final Object other) {
        return this.instance.equals(NashornCompatLayer.convertScriptObjectMirror(other));
    }

    @Override
    public int hashCode() {
        return this.instance.hashCode();
    }

    @Override
    public String toString() {
        return this.instance.toString();
    }

    // JSObject methods

    @Override
    public Object call(final Object thiz, final Object... args) {
        return NashornCompatLayer.convertScriptObjectMirror(this.instance.call(NashornCompatLayer.convertScriptObjectMirror(thiz), args));
    }

    @Override
    public Object newObject(final Object... args) {
        return NashornCompatLayer.convertScriptObjectMirror(this.instance.newObject(args));
    }

    @Override
    public Object eval(final String s) {
        return NashornCompatLayer.convertScriptObjectMirror(this.instance.eval(s));
    }

    @Override
    public Object getMember(final String name) {
        return NashornCompatLayer.convertScriptObjectMirror(this.instance.getMember(name));
    }

    @Override
    public Object getSlot(final int index) {
        return NashornCompatLayer.convertScriptObjectMirror(this.instance.getSlot(index));
    }

    @Override
    public boolean hasMember(final String name) {
        return this.instance.hasMember(name);
    }

    @Override
    public boolean hasSlot(final int slot) {
        return this.instance.hasSlot(slot);
    }

    @Override
    public void removeMember(final String name) {
        this.instance.removeMember(name);
    }

    @Override
    public void setMember(final String name, final Object value) {
        this.instance.setMember(name, NashornCompatLayer.convertScriptObjectMirror(value));
    }

    @Override
    public void setSlot(final int index, final Object value) {
        this.instance.setSlot(index, NashornCompatLayer.convertScriptObjectMirror(value));
    }

    @Override
    public boolean isInstance(final Object instance) {
        return this.instance.isInstance(NashornCompatLayer.convertScriptObjectMirror(instance));
    }

    @Override
    public String getClassName() {
        return this.instance.getClassName();
    }

    @Override
    public boolean isFunction() {
        return this.instance.isFunction();
    }

    @Override
    public boolean isStrictFunction() {
        return this.instance.isStrictFunction();
    }

    @Override
    public boolean isArray() {
        return this.instance.isArray();
    }

    // javax.script.Bindings methods

    @Override
    public void clear() {
        this.instance.clear();
    }

    @Override
    public boolean containsKey(final Object key) {
        return this.instance.containsKey(NashornCompatLayer.convertScriptObjectMirror(key));
    }

    @Override
    public boolean containsValue(final Object value) {
        return this.instance.containsValue(NashornCompatLayer.convertScriptObjectMirror(value));
    }

    @Override
    public Set<Map.Entry<String, Object>> entrySet() {
        Set<Map.Entry<String, Object>> entries = new LinkedHashSet<>();
        for (Map.Entry<String, Object> entry : this.instance.entrySet()) {
            entries.add(new AbstractMap.SimpleImmutableEntry<>(entry.getKey(), NashornCompatLayer.convertScriptObjectMirror(entry.getValue())));
        }
        return Collections.unmodifiableSet(entries);
    }

    @Override
    public Object get(final Object key) {
        return NashornCompatLayer.convertScriptObjectMirror(this.instance.get(NashornCompatLayer.convertScriptObjectMirror(key)));
    }

    @Override
    public boolean isEmpty() {
        return this.instance.isEmpty();
    }

    @Override
    public Set<String> keySet() {
        return this.instance.keySet();
    }

    @Override
    public Object put(final String key, final Object value) {
        return NashornCompatLayer.convertScriptObjectMirror(this.instance.put(key, NashornCompatLayer.convertScriptObjectMirror(value)));
    }

    @Override
    public void putAll(final Map<? extends String, ? extends Object> map) {
        this.instance.putAll(map);
    }

    @Override
    public Object remove(final Object key) {
        return NashornCompatLayer.convertScriptObjectMirror(this.instance.remove(NashornCompatLayer.convertScriptObjectMirror(key)));
    }

    @Override
    public int size() {
        return this.instance.size();
    }

    @Override
    public Collection<Object> values() {
        return this.instance.values();
    }

    @Override @Deprecated
    public double toNumber() {
        return this.instance.toNumber();
    }

    @Override
    public Object getDefaultValue(final Class<?> hint) {
        return NashornCompatLayer.convertScriptObjectMirror(this.instance.getDefaultValue(hint));
    }
}
