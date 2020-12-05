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

import javax.script.ScriptEngine;

import jdk.nashorn.api.scripting.NashornScriptEngine;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

public class NashornCompatLayer {

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
}
