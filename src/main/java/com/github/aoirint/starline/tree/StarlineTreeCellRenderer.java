package com.github.aoirint.starline.tree;

import com.github.aoirint.starline.node.StarlineNodeData;
import com.github.aoirint.starline.tree.dnd.DroppingLocation;
import com.github.aoirint.starline.StarlineIcon;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

public class StarlineTreeCellRenderer extends DefaultTreeCellRenderer {
    private Font cellTextFont;
    private Color cellTextColor;
    private Color selectionColor;
    private Color nonselectionColor;
    private Icon icon;

    public StarlineTreeCellRenderer() {
        initLookAndFeel();
    }

    public void initLookAndFeel() {
        cellTextFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
        cellTextColor = new Color(255, 255, 255);
        selectionColor = new Color(100, 100, 100);
        nonselectionColor = new Color(60, 60, 60);

        icon = StarlineIcon.generateCircleIcon(new Color(200, 200, 200));
    }

    private Icon getDefaultIcon(boolean expanded, boolean leaf) {
        if (leaf) {
            return this.getLeafIcon();
        }
        else if (expanded) {
            return this.getOpenIcon();
        }
        else {
            return this.getClosedIcon();
        }
    }

    public Component renderCell(JTree tree, DefaultMutableTreeNode treeNode, StarlineNodeData nodeData, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        JLabel label = (JLabel) super.getTreeCellRendererComponent(tree, treeNode, selected, expanded, leaf, row, hasFocus);

        String text = nodeData.toString();
        // Icon icon = getDefaultIcon(expanded, leaf);

        label.setForeground(cellTextColor);
        label.setFont(cellTextFont);
        label.setText(text);
        label.setIcon(icon);

        setBackgroundNonSelectionColor(nonselectionColor);
        setBackgroundSelectionColor(selectionColor);

        Border emptyBorder = BorderFactory.createEmptyBorder(2, 2, 2, 2);

        StarlineTree stree = (StarlineTree) tree;
        if (stree.dropTargetNode == treeNode) {
            if (stree.droppingLocation == DroppingLocation.CENTER) {
                Border rectBorder = BorderFactory.createMatteBorder(2, 2, 2, 2, cellTextColor);
                label.setBorder(BorderFactory.createCompoundBorder(rectBorder, emptyBorder));
            }
            else if (stree.droppingLocation == DroppingLocation.TOP) {
                Border topLineBorder = BorderFactory.createMatteBorder(2, 0, 0, 0, cellTextColor);
                label.setBorder(BorderFactory.createCompoundBorder(topLineBorder, emptyBorder));
            }
            else {
                Border bottomLineBorder = BorderFactory.createMatteBorder(0, 0, 2, 0, cellTextColor);
                label.setBorder(BorderFactory.createCompoundBorder(bottomLineBorder, emptyBorder));
            }
        }
        else {
            label.setBorder(emptyBorder);
        }

        return label;
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object treeNode, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        if (treeNode instanceof DefaultMutableTreeNode) {
            Object userObject = ((DefaultMutableTreeNode) treeNode).getUserObject();
            if (userObject instanceof StarlineNodeData) {
                return renderCell(tree, (DefaultMutableTreeNode) treeNode, (StarlineNodeData) userObject, selected, expanded, leaf, row, hasFocus);
            }
        }

        return super.getTreeCellRendererComponent(tree, treeNode, selected, expanded, leaf, row, hasFocus);
    }

}
