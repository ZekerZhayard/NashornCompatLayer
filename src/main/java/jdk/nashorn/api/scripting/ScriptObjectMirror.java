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

import java.nio.ByteBuffer;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.script.Bindings;

import io.github.zekerzhayard.nashorncompatlayer.NashornCompatLayer;
import org.openjdk.nashorn.api.scripting.AbstractJSObject;

/**
 * Mirror object that wraps a given Nashorn Script object.
 *
 * @since 1.8u40
 */
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

    /**
     * Call member function
     * @param functionName function name
     * @param args         arguments
     * @return return value of function
     */
    public Object callMember(final String functionName, final Object... args) {
        return NashornCompatLayer.convertScriptObjectMirror(this.instance.callMember(functionName, args));
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

    /**
     * Nashorn extension: setIndexedPropertiesToExternalArrayData.
     * set indexed properties be exposed from a given nio ByteBuffer.
     *
     * @param buf external buffer - should be a nio ByteBuffer
     */
    public void setIndexedPropertiesToExternalArrayData(final ByteBuffer buf) {
        this.instance.setIndexedPropertiesToExternalArrayData(buf);
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

    /**
     * Delete a property from this object.
     *
     * @param key the property to be deleted
     *
     * @return if the delete was successful or not
     */
    public boolean delete(final Object key) {
        return this.instance.delete(key);
    }

    @Override
    public int size() {
        return this.instance.size();
    }

    @Override
    public Collection<Object> values() {
        return this.instance.values();
    }

    // Support for ECMAScript Object API on mirrors

    /**
     * Return the __proto__ of this object.
     * @return __proto__ object.
     */
    public Object getProto() {
        return this.instance.getProto();
    }

    /**
     * Set the __proto__ of this object.
     * @param proto new proto for this object
     */
    public void setProto(final Object proto) {
        this.instance.setProto(proto);
    }

    /**
     * ECMA 8.12.1 [[GetOwnProperty]] (P)
     *
     * @param key property key
     *
     * @return Returns the Property Descriptor of the named own property of this
     * object, or undefined if absent.
     */
    public Object getOwnPropertyDescriptor(final String key) {
        return this.instance.getOwnPropertyDescriptor(key);
    }

    /**
     * return an array of own property keys associated with the object.
     *
     * @param all True if to include non-enumerable keys.
     * @return Array of keys.
     */
    public String[] getOwnKeys(final boolean all) {
        return this.instance.getOwnKeys(all);
    }

    /**
     * Flag this script object as non extensible
     *
     * @return the object after being made non extensible
     */
    public ScriptObjectMirror preventExtensions() {
        return (ScriptObjectMirror) NashornCompatLayer.convertScriptObjectMirror(this.instance.preventExtensions());
    }

    /**
     * Check if this script object is extensible
     * @return true if extensible
     */
    public boolean isExtensible() {
        return this.instance.isExtensible();
    }

    /**
     * ECMAScript 15.2.3.8 - seal implementation
     * @return the sealed script object
     */
    public ScriptObjectMirror seal() {
        return (ScriptObjectMirror) NashornCompatLayer.convertScriptObjectMirror(this.instance.seal());
    }

    /**
     * Check whether this script object is sealed
     * @return true if sealed
     */
    public boolean isSealed() {
        return this.instance.isSealed();
    }

    /**
     * ECMA 15.2.39 - freeze implementation. Freeze this script object
     * @return the frozen script object
     */
    public ScriptObjectMirror freeze() {
        return (ScriptObjectMirror) NashornCompatLayer.convertScriptObjectMirror(this.instance.freeze());
    }

    /**
     * Check whether this script object is frozen
     * @return true if frozen
     */
    public boolean isFrozen() {
        return this.instance.isFrozen();
    }

    /**
     * Utility to check if given object is ECMAScript undefined value
     *
     * @param obj object to check
     * @return true if 'obj' is ECMAScript undefined value
     */
    public static boolean isUndefined(final Object obj) {
        return org.openjdk.nashorn.api.scripting.ScriptObjectMirror.isUndefined(obj);
    }

    /**
     * Utility to convert this script object to the given type.
     *
     * @param <T> destination type to convert to
     * @param type destination type to convert to
     * @return converted object
     */
    public <T> T to(final Class<T> type) {
        return this.instance.to(type);
    }

    /**
     * Make a script object mirror on given object if needed.
     *
     * @param obj object to be wrapped/converted
     * @param homeGlobal global to which this object belongs.
     * @return wrapped/converted object
     */
    public static Object wrap(final Object obj, final Object homeGlobal) {
        return NashornCompatLayer.convertScriptObjectMirror(org.openjdk.nashorn.api.scripting.ScriptObjectMirror.wrap(NashornCompatLayer.convertScriptObjectMirror(obj), NashornCompatLayer.convertScriptObjectMirror(homeGlobal)));
    }

    /**
     * Make a script object mirror on given object if needed. The created wrapper will implement
     * the Java {@code List} interface if {@code obj} is a JavaScript {@code Array} object;
     * this is compatible with Java JSON libraries expectations. Arrays retrieved through its
     * properties (transitively) will also implement the list interface.
     *
     * @param obj object to be wrapped/converted
     * @param homeGlobal global to which this object belongs.
     * @return wrapped/converted object
     */
    public static Object wrapAsJSONCompatible(final Object obj, final Object homeGlobal) {
        return NashornCompatLayer.convertScriptObjectMirror(org.openjdk.nashorn.api.scripting.ScriptObjectMirror.wrap(NashornCompatLayer.convertScriptObjectMirror(obj), NashornCompatLayer.convertScriptObjectMirror(homeGlobal)));
    }

    /**
     * Unwrap a script object mirror if needed.
     *
     * @param obj object to be unwrapped
     * @param homeGlobal global to which this object belongs
     * @return unwrapped object
     */
    public static Object unwrap(final Object obj, final Object homeGlobal) {
        return org.openjdk.nashorn.api.scripting.ScriptObjectMirror.unwrap(obj, homeGlobal);
    }

    /**
     * Wrap an array of object to script object mirrors if needed.
     *
     * @param args array to be unwrapped
     * @param homeGlobal global to which this object belongs
     * @return wrapped array
     */
    public static Object[] wrapArray(final Object[] args, final Object homeGlobal) {
        return org.openjdk.nashorn.api.scripting.ScriptObjectMirror.wrapArray(args, homeGlobal);
    }

    /**
     * Unwrap an array of script object mirrors if needed.
     *
     * @param args array to be unwrapped
     * @param homeGlobal global to which this object belongs
     * @return unwrapped array
     */
    public static Object[] unwrapArray(final Object[] args, final Object homeGlobal) {
        return org.openjdk.nashorn.api.scripting.ScriptObjectMirror.unwrapArray(args, homeGlobal);
    }

    /**
     * Are the given objects mirrors to same underlying object?
     *
     * @param obj1 first object
     * @param obj2 second object
     * @return true if obj1 and obj2 are identical script objects or mirrors of it.
     */
    public static boolean identical(final Object obj1, final Object obj2) {
        return org.openjdk.nashorn.api.scripting.ScriptObjectMirror.identical(NashornCompatLayer.convertScriptObjectMirror(obj1), NashornCompatLayer.convertScriptObjectMirror(obj2));
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
