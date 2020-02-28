package com.github.aoirint.starline.tree;

import com.github.aoirint.starline.node.StarlineNode;

import javax.swing.tree.TreePath;
import java.awt.event.MouseEvent;

public interface StarlineNodeTreeDelegate {
    void onNodeActivated(StarlineTree tree, StarlineNode newSelectedNode, StarlineNode oldSelectedNode);
    void onNodeRightClicked(StarlineTree tree, TreePath cursorPath, StarlineNode cursorNode, MouseEvent event);

}
