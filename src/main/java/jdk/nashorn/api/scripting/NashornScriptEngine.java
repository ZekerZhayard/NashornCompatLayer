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

import java.io.Reader;
import javax.script.AbstractScriptEngine;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;

import io.github.zekerzhayard.nashorncompatlayer.NashornCompatLayer;

/**
 * JSR-223 compliant script engine for Nashorn. Instances are not created directly, but rather returned through
 * {@link NashornScriptEngineFactory#getScriptEngine()}. Note that this engine implements the {@link Compilable} and
 * {@link Invocable} interfaces, allowing for efficient precompilation and repeated execution of scripts.
 * @see NashornScriptEngineFactory
 *
 * @since 1.8u40
 */
public final class NashornScriptEngine extends AbstractScriptEngine implements Compilable, Invocable {
    /**
     * Key used to associate Nashorn global object mirror with arbitrary Bindings instance.
     */
    public static final String NASHORN_GLOBAL = "nashorn.global";

    public final org.openjdk.nashorn.api.scripting.NashornScriptEngine instance;

    public NashornScriptEngine(org.openjdk.nashorn.api.scripting.NashornScriptEngine instance) {
        this.instance = instance;
    }

    @Override
    public void setContext(ScriptContext ctxt) {
        this.instance.setContext(ctxt);
    }

    @Override
    public ScriptContext getContext() {
        return this.instance.getContext();
    }

    @Override
    public Bindings getBindings(int scope) {
        return (Bindings) NashornCompatLayer.convertScriptObjectMirror(this.instance.getBindings(scope));
    }

    @Override
    public void setBindings(Bindings bindings, int scope) {
        this.instance.setBindings((Bindings) NashornCompatLayer.convertScriptObjectMirror(bindings), scope);
    }

    @Override
    public void put(String key, Object value) {
        this.instance.put(key, NashornCompatLayer.convertScriptObjectMirror(value));
    }

    @Override
    public Object get(String key) {
        return NashornCompatLayer.convertScriptObjectMirror(this.instance.get(key));
    }

    @Override
    public Object eval(Reader reader, Bindings bindings) throws ScriptException {
        return NashornCompatLayer.convertScriptObjectMirror(this.instance.eval(reader, bindings));
    }

    @Override
    public Object eval(String script, Bindings bindings) throws ScriptException {
        return NashornCompatLayer.convertScriptObjectMirror(this.instance.eval(script, bindings));
    }

    @Override
    public Object eval(Reader reader) throws ScriptException {
        return NashornCompatLayer.convertScriptObjectMirror(this.instance.eval(reader));
    }

    @Override
    public Object eval(String script) throws ScriptException {
        return NashornCompatLayer.convertScriptObjectMirror(this.instance.eval(script));
    }

    @Override
    public Object eval(final Reader reader, final ScriptContext ctxt) throws ScriptException {
        return NashornCompatLayer.convertScriptObjectMirror(this.instance.eval(reader, ctxt));
    }

    @Override
    public Object eval(final String script, final ScriptContext ctxt) throws ScriptException {
        return NashornCompatLayer.convertScriptObjectMirror(this.instance.eval(script, ctxt));
    }

    @Override
    public ScriptEngineFactory getFactory() {
        return this.instance.getFactory();
    }

    @Override
    public Bindings createBindings() {
        return this.instance.createBindings();
    }

    // Compilable methods

    @Override
    public CompiledScript compile(final Reader reader) throws ScriptException {
        return this.instance.compile(reader);
    }

    @Override
    public CompiledScript compile(final String str) throws ScriptException {
        return this.instance.compile(str);
    }

    // Invocable methods

    @Override
    public Object invokeFunction(final String name, final Object... args)
        throws ScriptException, NoSuchMethodException {
        return NashornCompatLayer.convertScriptObjectMirror(this.instance.invokeFunction(name, args));
    }

    @Override
    public Object invokeMethod(final Object thiz, final String name, final Object... args)
        throws ScriptException, NoSuchMethodException {
        return NashornCompatLayer.convertScriptObjectMirror(this.instance.invokeMethod(thiz, name, args));
    }

    @Override
    public <T> T getInterface(final Class<T> clazz) {
        return this.instance.getInterface(clazz);
    }

    @Override
    public <T> T getInterface(final Object thiz, final Class<T> clazz) {
        return this.instance.getInterface(thiz, clazz);
    }
}
