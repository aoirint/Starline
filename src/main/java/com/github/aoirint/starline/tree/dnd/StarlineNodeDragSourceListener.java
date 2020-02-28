package com.github.aoirint.starline.tree.dnd;

import com.github.aoirint.starline.tree.StarlineTree;

import java.awt.dnd.*;

public class StarlineNodeDragSourceListener implements DragSourceListener {
    StarlineTree tree;

    public StarlineNodeDragSourceListener(StarlineTree tree) {
        this.tree = tree;
    }

    @Override
    public void dragEnter(DragSourceDragEvent dsde) {
        dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
    }

    @Override
    public void dragExit(DragSourceEvent dse) {
        dse.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
    }

    @Override
    public void dragOver(DragSourceDragEvent dsde) {

    }

    @Override
    public void dropActionChanged(DragSourceDragEvent dsde) {

    }

    @Override
    public void dragDropEnd(DragSourceDropEvent dsde) {

    }
}
