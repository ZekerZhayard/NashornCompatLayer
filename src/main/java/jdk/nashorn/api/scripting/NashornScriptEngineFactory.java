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

import java.util.List;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

import io.github.zekerzhayard.nashorncompatlayer.NashornCompatLayer;

public final class NashornScriptEngineFactory implements ScriptEngineFactory {
    public final org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory instance;

    public NashornScriptEngineFactory() {
        this.instance = new org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory();
    }

    @Override
    public String getEngineName() {
        return this.instance.getEngineName();
    }

    @Override
    public String getEngineVersion() {
        return this.instance.getEngineVersion();
    }

    @Override
    public List<String> getExtensions() {
        return this.instance.getExtensions();
    }

    @Override
    public String getLanguageName() {
        return this.instance.getLanguageName();
    }

    @Override
    public String getLanguageVersion() {
        return this.instance.getLanguageVersion();
    }

    @Override
    public String getMethodCallSyntax(String obj, String method, String... args) {
        return this.instance.getMethodCallSyntax(obj, method, args);
    }

    @Override
    public List<String> getMimeTypes() {
        return this.instance.getMimeTypes();
    }

    @Override
    public List<String> getNames() {
        return this.instance.getNames();
    }

    @Override
    public String getOutputStatement(String toDisplay) {
        return this.instance.getOutputStatement(toDisplay);
    }

    @Override
    public Object getParameter(String key) {
        return this.instance.getParameter(key);
    }

    @Override
    public String getProgram(String... statements) {
        return this.instance.getProgram(statements);
    }

    @Override
    public ScriptEngine getScriptEngine() {
        return NashornCompatLayer.convertScriptEngine(this.instance.getScriptEngine());
    }

    public ScriptEngine getScriptEngine(ClassFilter classFilter) {
        return NashornCompatLayer.convertScriptEngine(this.instance.getScriptEngine(classFilter::exposeToScripts));
    }
}
