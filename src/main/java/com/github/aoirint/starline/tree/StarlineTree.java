package com.github.aoirint.starline.tree;

import com.github.aoirint.starline.tree.dnd.StarlineDragGestureListener;
import com.github.aoirint.starline.tree.dnd.StarlineDropTargetListener;
import com.github.aoirint.starline.node.StarlineNode;
import com.github.aoirint.starline.node.StarlineNodeData;
import com.github.aoirint.starline.tree.dnd.DroppingLocation;
import com.github.aoirint.starline.tree.dnd.StarlineTreeTransferHandler;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StarlineTree extends JTree implements TreeSelectionListener, TreeExpansionListener {
    public StarlineNodeTreeDelegate delegate;

    public DragGestureRecognizer dragGestureRecognizer;
    public DropTarget dropTarget;

    public StarlineNode draggingNode;
    public StarlineNode dropTargetNode;
    public DroppingLocation droppingLocation;

    public StarlineTree() {
        super(new StarlineNode());

        setDragEnabled(true);
        setDropMode(DropMode.ON_OR_INSERT);
        setTransferHandler(new StarlineTreeTransferHandler(this));
        addTreeSelectionListener(this);
        addTreeExpansionListener(this);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getButton() == MouseEvent.BUTTON3) {
                    int row = getClosestRowForLocation(event.getX(), event.getY());
                    TreePath path = getPathForRow(row);

                    StarlineNode treeNode = (StarlineNode) path.getLastPathComponent();
                    StarlineNodeData nodeData = (StarlineNodeData) treeNode.getUserObject();

                    if (delegate != null) delegate.onNodeRightClicked(StarlineTree.this, path, treeNode, event);
                }
            }
        });

        dragGestureRecognizer = DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_MOVE, new StarlineDragGestureListener(this));
        dropTarget = new DropTarget(this, new StarlineDropTargetListener(this));
    }

    public void expandRootNode() {
        expandPath(new TreePath(getRootNode()));
    }

    public TreePath node2path(StarlineNode node) {
        return new TreePath(node.getPath());
    }
    public void expandNode(StarlineNode node) {
        expandPath(node2path(node));
    }
    public void collapseNode(StarlineNode node) {
        collapsePath(node2path(node));
    }
    public boolean isExpanded(StarlineNode node) {
        return isExpanded(node2path(node));
    }

    public void loadNodeStates() {
        StarlineNode rootNode = getRootNode();
        loadDescendantsStates(rootNode);
    }
    public void loadDescendantsStates(StarlineNode node) {
        if (node.expanded) {
            expandNode(node);
        }
        else {
            collapseNode(node);
        }

        for (TreeNode child: Collections.list(node.children())) {
            StarlineNode childNode = (StarlineNode) child;
            loadDescendantsStates(childNode);
        }
    }

    public void setRootNode(StarlineNode node) {
        DefaultTreeModel treeModel = new DefaultTreeModel(node);
        setModel(treeModel);

        updateUI();
    }

    public StarlineNode getRootNode() {
        DefaultTreeModel treeModel = (DefaultTreeModel) this.getModel();
        StarlineNode rootNode = (StarlineNode) treeModel.getRoot();

        return rootNode;
    }
    public void reloadModel() {
        DefaultTreeModel treeModel = (DefaultTreeModel) this.getModel();
        treeModel.reload();
    }
    public void nodeUpdated(TreeNode treeNode) {
        DefaultTreeModel treeModel = (DefaultTreeModel) this.getModel();
        treeModel.nodeChanged(treeNode);
    }

    public List<StarlineNode> getSelectedNodes() {
        final List<StarlineNode> selectedNodes = new ArrayList<>();

        TreePath[] selectionPaths = getSelectionPaths();
        for (TreePath selectionPath: selectionPaths) {
            StarlineNode selectionNode = (StarlineNode) selectionPath.getLastPathComponent();
            selectedNodes.add(selectionNode);
        }

        return selectedNodes;
    }

    @Override
    public void valueChanged(TreeSelectionEvent event) {
        TreePath oldSelectionPath = event.getOldLeadSelectionPath();
        StarlineNode oldTreeNode = oldSelectionPath != null ? (StarlineNode) oldSelectionPath.getLastPathComponent() : null;

        TreePath newSelectionPath = event.getNewLeadSelectionPath();
        StarlineNode newTreeNode = newSelectionPath != null ? (StarlineNode) newSelectionPath.getLastPathComponent() : null;

        if (delegate != null) delegate.onNodeActivated(this, newTreeNode, oldTreeNode);
    }

    @Override
    public void treeExpanded(TreeExpansionEvent event) {
        TreePath treePath = event.getPath();
        StarlineNode treeNode = (StarlineNode) treePath.getLastPathComponent();

        treeNode.expanded = true;
    }

    @Override
    public void treeCollapsed(TreeExpansionEvent event) {
        TreePath treePath = event.getPath();
        StarlineNode treeNode = (StarlineNode) treePath.getLastPathComponent();

        treeNode.expanded = false;
    }

}
