package com.github.aoirint.starline.tree.dnd;

import com.github.aoirint.starline.node.StarlineNode;
import com.github.aoirint.starline.tree.StarlineTree;

import javax.swing.tree.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.*;
import java.util.Arrays;

public class StarlineDropTargetListener implements DropTargetListener {
    StarlineTree tree;

    public StarlineDropTargetListener(StarlineTree tree) {
        this.tree = tree;
    }

    DroppingLocation getDroppingLocationForLocation(TreePath cursorPath, Point cursorPoint) {
        Rectangle cursorBounds = tree.getPathBounds(cursorPath);

        if (cursorPath.getLastPathComponent() != tree.getRootNode()) {
            if (cursorPoint.y < cursorBounds.y + cursorBounds.height/3) {
                return DroppingLocation.TOP;
            }
            else if (cursorPoint.y > cursorBounds.y + cursorBounds.height*2/3) {
                return DroppingLocation.BOTTOM;
            }
        }

        return DroppingLocation.CENTER;
    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {
        DataFlavor[] f = dtde.getCurrentDataFlavors();
        boolean isDataFlavorSupported = f[0].getHumanPresentableName().equals(StarlineNodeTransferable.NAME_NODE);
        if (! isDataFlavorSupported) {
            tree.dropTargetNode = null;
            dtde.rejectDrag();
            tree.repaint();
            return;
        }

        Point cursorPoint = dtde.getLocation();
        TreePath cursorPath = tree.getPathForLocation(cursorPoint.x, cursorPoint.y);
        if (cursorPath == null) {
            tree.dropTargetNode = null;
            dtde.rejectDrag();
            tree.repaint();
            return;
        }

        DroppingLocation droppingLocation = getDroppingLocationForLocation(cursorPath, cursorPoint);
        StarlineNode targetNode = (StarlineNode) cursorPath.getLastPathComponent();
        StarlineNode draggingNode = (StarlineNode) tree.getSelectionPath().getLastPathComponent();

        TreeNode targetParent = targetNode.getParent();
        if (targetParent instanceof StarlineNode) {
            StarlineNode targetParentNode = (StarlineNode) targetParent;

            // Dropped parent into child
            if (Arrays.asList(targetParentNode.getPath()).contains(draggingNode)) {
                tree.dropTargetNode = null;
                dtde.rejectDrag();
                tree.repaint();
                return;
            }
        }

        tree.dropTargetNode = targetNode;
        tree.droppingLocation = droppingLocation;
        dtde.acceptDrag(dtde.getDropAction());
        tree.repaint();
    }


    @Override
    public void drop(DropTargetDropEvent dtde) {
        Object draggingObject = tree.getSelectionPath().getLastPathComponent();
        if (! (draggingObject instanceof StarlineNode)) {
            dtde.dropComplete(false);

            tree.dropTargetNode = null;
            tree.draggingNode = null;
            tree.repaint();
            return;
        }

        Point cursorPoint = dtde.getLocation();
        TreePath cursorPath = tree.getPathForLocation(cursorPoint.x, cursorPoint.y);
        if (cursorPath == null) {
            dtde.dropComplete(false);

            tree.dropTargetNode = null;
            tree.draggingNode = null;
            tree.repaint();
            return;
        }

        DroppingLocation droppingLocation = getDroppingLocationForLocation(cursorPath, cursorPoint);
        StarlineNode targetNode = (StarlineNode) cursorPath.getLastPathComponent();

        StarlineNode draggingNode = (StarlineNode) draggingObject;
        if (draggingNode == targetNode) {
            dtde.dropComplete(false);

            tree.dropTargetNode = null;
            tree.draggingNode = null;
            tree.repaint();
            return;
        }
        dtde.acceptDrop(DnDConstants.ACTION_MOVE);

        DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
        treeModel.removeNodeFromParent(draggingNode);

        TreeNode targetParent = targetNode.getParent();
        if (targetParent instanceof StarlineNode && droppingLocation != DroppingLocation.CENTER) {
            if (droppingLocation == DroppingLocation.TOP) {
                treeModel.insertNodeInto(draggingNode, (StarlineNode) targetParent, targetParent.getIndex(targetNode));
            }
            else {
                treeModel.insertNodeInto(draggingNode, (StarlineNode) targetParent, targetParent.getIndex(targetNode)+1);
            }
        }
        else {
            treeModel.insertNodeInto(draggingNode, targetNode, targetNode.getChildCount());
            tree.expandPath(cursorPath);
        }
        dtde.dropComplete(true);

        tree.dropTargetNode = null;
        tree.draggingNode = null;
        tree.repaint();
    }

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {
    }

    @Override
    public void dragExit(DropTargetEvent dte) {
    }

}
