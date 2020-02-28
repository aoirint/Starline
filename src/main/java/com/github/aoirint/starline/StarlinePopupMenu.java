package com.github.aoirint.starline;

import com.github.aoirint.starline.node.StarlineNode;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class StarlinePopupMenu extends JPopupMenu {
    Color backgroundColor;
    Color foregroundColor;
    Color menuBackgroundColor;
    Color menuForegroundColor;
    Font menuFont;

    JMenuItem addMenuItem;
    JMenuItem deleteMenuItem;

    StarlinePopupMenuDelegate delegate;
    TreePath cursorPath;
    StarlineNode cursorNode;

    public StarlinePopupMenu() {
        backgroundColor = new Color(40, 40, 40);
        foregroundColor = new Color(255, 255, 255);
        menuBackgroundColor = new Color(80, 80, 80);
        menuForegroundColor = new Color(255, 255, 255);
        menuFont = new Font(Font.SANS_SERIF, Font.PLAIN, 14);

        setBackground(backgroundColor);
        setForeground(foregroundColor);

        initAddMenuItem();
        initDeleteMenuItem();
    }

    public void initAddMenuItem() {
        JMenuItem menuItem = createMenuItem("Add");
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (delegate != null) delegate.onAddPopupMenu(StarlinePopupMenu.this);
            }
        });

        addMenuItem = menuItem;
        add(menuItem);
    }

    public void initDeleteMenuItem() {
        JMenuItem menuItem = createMenuItem("Delete");
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (delegate != null) delegate.onDeletePopupMenu(StarlinePopupMenu.this);
            }
        });

        deleteMenuItem = menuItem;
        add(menuItem);
    }


    public JMenuItem createMenuItem(String text) {
        JMenuItem menuItem = new JMenuItem(text);
        menuItem.setBackground(menuBackgroundColor);
        menuItem.setForeground(menuForegroundColor);
        menuItem.setFont(menuFont);
        menuItem.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        return menuItem;
    }

}
