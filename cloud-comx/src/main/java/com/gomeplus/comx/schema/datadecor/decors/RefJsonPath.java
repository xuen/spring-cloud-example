package com.gomeplus.comx.schema.datadecor.decors;

import com.alibaba.fastjson.JSONPath;
import com.gomeplus.comx.context.Context;
import com.gomeplus.comx.utils.config.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by xue on 2/2/17.
 * TODO 验证逻辑 以及 warning
 * 应当会抛出 DecorException
 */
public interface RefJsonPath {
    String FIELD_REF_JSON_PATH      = "refJsonPath";
    String FIELD_REF_PRECONDITION   = "refPrecondition";
    default List<Object> getAllNodes(Config conf, Object data, Context context) {
        String refJsonPath = conf.str(FIELD_REF_JSON_PATH, "");
        if (refJsonPath.isEmpty()) {
            context.getLogger().debug("RefJsonPath: null refJsonPath:" + data.toString());
            return Arrays.asList(data);
        }
        try {
            Object matchedNode = JSONPath.eval(data, refJsonPath);
            if (matchedNode instanceof List) {
                context.getLogger().debug("RefJsonPath: list:" + matchedNode.toString());
                return (List)matchedNode;
            } else {
                context.getLogger().error("RefJsonPath: not list:" + matchedNode.toString());
                return Arrays.asList(data);
            }
        } catch(Exception ex){
            context.getLogger().error("Decor Eachdecor, refJsonPath error:" + ex.getMessage() + " refJsonPath:"+ refJsonPath+ ", data:" + data);
            return new ArrayList<>();
        }
    }

    default List<Object> getMatchedNodes(Config conf, Object data, Context context) {
        List<Object> allNodes = getAllNodes(conf, data, context);
        String precondition = conf.str(FIELD_REF_PRECONDITION, "");
        if (precondition.isEmpty()) return allNodes;
        List<Object> matchedNodes = new ArrayList<Object>();
        for (Object node:allNodes) {
            if (Precondition.execute(precondition, data, context, node)) matchedNodes.add(node);
        }
        return matchedNodes;
    }
}
