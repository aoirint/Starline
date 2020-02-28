package com.github.aoirint.starline;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class StarlineMenuBar extends JMenuBar {
    Color menuBackgroundColor;
    Color menuForegroundColor;
    Font menuFont;

    JMenu fileMenu;
    JMenuItem fileNewMenuItem;
    JMenuItem fileOpenMenuItem;
    JMenuItem fileSaveMenuItem;
    JMenuItem fileSaveAsMenuItem;
    JMenuItem fileExitMenuItem;

    StarlineMenuBarDelegate delegate;

    public StarlineMenuBar() {
        menuBackgroundColor = new Color(80, 80, 80);
        menuForegroundColor = new Color(255, 255, 255);
        menuFont = new Font(Font.SANS_SERIF, Font.PLAIN, 14);

        setBackground(menuBackgroundColor);
        setForeground(menuForegroundColor);
        setBorder(null);
        setBorderPainted(false);
        setFont(menuFont);

        JMenu fileMenu = initFileMenu();
        add(fileMenu);
    }


    private void initNewFileMenuItem() {
        JMenuItem menuItem = createMenuItem("New");
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                if (delegate != null) delegate.onNewFileMenu();
            }
        });

        fileMenu.add(menuItem);
        fileNewMenuItem = menuItem;
    }

    private void initOpenFileMenuItem() {
        JMenuItem menuItem = createMenuItem("Open");
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                if (delegate != null) delegate.onOpenFileMenu();
            }
        });

        fileMenu.add(menuItem);
        fileOpenMenuItem = menuItem;
    }

    private void initSaveFileMenuItem() {
        JMenuItem menuItem = createMenuItem("Save");
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (delegate != null) delegate.onSaveFileMenu();
            }
        });

        fileMenu.add(menuItem);
        fileSaveMenuItem = menuItem;
    }

    private void initSaveAsFileMenuItem() {
        JMenuItem menuItem = createMenuItem("Save As");
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (delegate != null) delegate.onSaveAsFileMenu();
            }
        });

        fileMenu.add(menuItem);
        fileSaveAsMenuItem = menuItem;
    }

    private void initExitFileMenuItem() {
        JMenuItem menuItem = createMenuItem("Exit");
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (delegate != null) delegate.onExitFileMenu();
            }
        });

        fileMenu.add(menuItem);
        fileExitMenuItem = menuItem;
    }

    private JMenu initFileMenu() {
        fileMenu = new JMenu("File");
        fileMenu.setBackground(menuBackgroundColor);
        fileMenu.setForeground(menuForegroundColor);
        fileMenu.setFont(menuFont);

        initNewFileMenuItem();
        initOpenFileMenuItem();
        initSaveFileMenuItem();
        initSaveAsFileMenuItem();
        initExitFileMenuItem();

        return fileMenu;
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
