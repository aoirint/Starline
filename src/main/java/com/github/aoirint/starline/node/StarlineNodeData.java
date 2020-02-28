package com.github.aoirint.starline.node;

import java.util.HashMap;
import java.util.Map;

public class StarlineNodeData {
    public String title = "";
    public String content = "";

    public Map serialize() {
        Map map = new HashMap();

        map.put("title", title);
        map.put("content", content);

        return map;
    }
    public void deserialize(Map map) {
        title = (String) map.get("title");
        content = (String) map.get("content");
    }

    public String toString() {
        if (title.isEmpty()) return "(No title)";
        return title;
    }

}
