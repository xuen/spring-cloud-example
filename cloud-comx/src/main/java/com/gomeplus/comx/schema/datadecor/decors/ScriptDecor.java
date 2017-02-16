package com.gomeplus.comx.schema.datadecor.decors;

import com.gomeplus.comx.boot.ComxConfLoader;
import com.gomeplus.comx.context.Context;
import com.gomeplus.comx.schema.datadecor.DecorException;
import com.gomeplus.comx.utils.config.Config;
import groovy.lang.Binding;
import groovy.lang.GroovyObject;
import groovy.lang.GroovyShell;
import groovy.util.GroovyScriptEngine;

import java.io.IOException;
import java.util.*;


/**
 * http://wiki.jikexueyuan.com/project/groovy-introduction/integrating-groovy-applications.html
 * Created by xue on 1/17/17.
 * TODO 勉强能用 需要测试用例
 */
public class ScriptDecor extends AbstractDecor implements RefJsonPath{
    public ScriptDecor(Config conf) {
        super(conf);
    }
    public String getType() {
        return AbstractDecor.TYPE_SCRIPT;
    }

    static GroovyScriptEngine groovyScriptEngine;

    static {
        try {
            String groovyHome = ComxConfLoader.getComxHome() + "/groovy-scripts/";
            groovyScriptEngine = new GroovyScriptEngine(groovyHome);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void doDecorate(Object data, Context context) throws DecorException{
        context.getLogger().error("Decor ScriptDecor: none:" + conf.rawData());
        List matchedNodes = getMatchedNodes(conf, data, context);
        context.getLogger().debug("Decor ScriptDecor: matched nodes:" + matchedNodes.toString());

        try {
            String scriptName = conf.str("jscript", "");
            String lambda     = conf.str("jlambda", "");
            if (!scriptName.isEmpty()) {
                Class scriptClass = groovyScriptEngine.loadScriptByName(scriptName + ".groovy");
                GroovyObject scriptInstance = (GroovyObject) scriptClass.newInstance();
                for (Object ref: matchedNodes) {
                    Object params = new Object[] {data, context, ref};
                    scriptInstance.invokeMethod("callback", params);
                }
            } else if(!lambda.isEmpty()) {
                for (Object ref: matchedNodes) {
                    HashMap<String, Object> variables = new HashMap<>();
                    variables.put("context", context);
                    variables.put("data", data);
                    variables.put("ref", ref);
                    Binding binding = new Binding(variables);

                    GroovyShell shell = new GroovyShell(binding);
                    shell.evaluate(lambda);
                }
            } else {
                context.getLogger().error("Decor, ScriptDecor jscript or jlambda empty");
            }
            // do nothing
        } catch (Exception ex) {
            // 有以下错误
            //(ResourceException | ScriptException | InstantiationException | IllegalAccessException e1)
            throw new DecorException(ex);
        }
    }
}
