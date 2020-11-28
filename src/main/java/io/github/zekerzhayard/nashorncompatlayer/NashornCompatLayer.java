package io.github.zekerzhayard.nashorncompatlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.script.ScriptEngine;

import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.ITransformer;
import jdk.nashorn.api.scripting.NashornScriptEngine;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

// Make sure this mod will displayed in crash reports.
public class NashornCompatLayer implements ITransformationService {
    public static ScriptEngine convertScriptEngine(ScriptEngine engine) {
        if (engine instanceof org.openjdk.nashorn.api.scripting.NashornScriptEngine nse) {
            try {
                return new NashornScriptEngine(nse);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        } else if (engine instanceof NashornScriptEngine nse) {
            return nse.instance;
        }
        return engine;
    }

    public static Object convertScriptObjectMirror(Object object) {
        if (object instanceof org.openjdk.nashorn.api.scripting.ScriptObjectMirror som) {
            try {
                return new ScriptObjectMirror(som);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        } else if (object instanceof ScriptObjectMirror som) {
            return som.instance;
        }
        return object;
    }

    @Nonnull
    @Override
    public String name() {
        return "NashornCompatLayer";
    }

    @Override
    public void initialize(IEnvironment environment) {

    }

    @Override
    public void beginScanning(IEnvironment environment) {

    }

    @Override
    public void onLoad(IEnvironment env, Set<String> otherServices) {

    }

    @Nonnull
    @Override
    @SuppressWarnings("rawtypes")
    public List<ITransformer> transformers() {
        return new ArrayList<>();
    }
}
