package com.github.aoirint.starline.node;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.util.*;

public class StarlineNode extends DefaultMutableTreeNode {
    public StarlineNodeData nodeData;
    public boolean expanded;

    public StarlineNode() {
        nodeData = new StarlineNodeData();

        setUserObject(nodeData);
    }

    public int countDescendants() {
        if (children == null) return 0;

        int count = 0;
        for (TreeNode treeNode: children) {
            StarlineNode childNode = (StarlineNode) treeNode;

            count += 1;
            count += childNode.countDescendants();
        }

        return count;
    }


    public Map serialize() {
        Map map = new LinkedHashMap();

        if (getParent() != null) // not root
            map.put("expanded", expanded);

        Map serializedNodeData = nodeData.serialize();
        map.put("data", serializedNodeData);

        if (children != null) {
            List nodeList = new ArrayList();

            for (TreeNode treeNode: children) {
                StarlineNode childNode = (StarlineNode) treeNode;

                Map serializedChildNode = childNode.serialize();
                nodeList.add(serializedChildNode);
            }

            map.put("children", nodeList);
        }

        return map;
    }
    public void deserialize(Map map) {
        removeAllChildren();

        expanded = (boolean) map.getOrDefault("expanded", true);

        Map serializedNodeData = (Map) map.get("data");
        nodeData.deserialize(serializedNodeData);

        if (map.containsKey("children")) {
            List nodeList = (List) map.get("children");
            for (Object childNodeDataObj: nodeList) {
                Map serializedChildNode = (Map) childNodeDataObj;

                StarlineNode childNode = new StarlineNode();
                childNode.deserialize(serializedChildNode);

                add(childNode);
            }
        }

    }

}
