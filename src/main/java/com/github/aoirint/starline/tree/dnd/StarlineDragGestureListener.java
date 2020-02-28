package com.github.aoirint.starline.tree.dnd;

import com.github.aoirint.starline.node.StarlineNode;
import com.github.aoirint.starline.tree.StarlineTree;

import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;

public class StarlineDragGestureListener implements DragGestureListener {
    StarlineTree tree;

    public StarlineDragGestureListener(StarlineTree tree) {
        this.tree = tree;
    }

    @Override
    public void dragGestureRecognized(DragGestureEvent dge) {
        Point dragOrigin = dge.getDragOrigin();
        TreePath draggingPath = tree.getPathForLocation(dragOrigin.x, dragOrigin.y);

        if (draggingPath == null || draggingPath.getParentPath() == null) {
            return;
        }

        StarlineNode draggingNode = (StarlineNode) draggingPath.getLastPathComponent();
        Transferable transferable = new StarlineNodeTransferable(draggingNode);

        tree.draggingNode = draggingNode;

        DragSource.getDefaultDragSource().startDrag(dge, Cursor.getDefaultCursor(), transferable, new StarlineNodeDragSourceListener(tree));

    }

}
