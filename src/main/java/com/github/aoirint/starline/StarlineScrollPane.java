package com.github.aoirint.starline;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;

public class StarlineScrollPane extends JScrollPane {
    Color backgroundColor;
    Color foregroundColor;
    Color scrollBackgroundColor;
    Color scrollForegroundColor;

    public StarlineScrollPane(Component view) {
        super(view);
        initLookAndFeel();

        setBackground(backgroundColor);

        JScrollBar vScrollBar = getVerticalScrollBar();
        initScrollBar(vScrollBar, false);

        JScrollBar hScrollBar = getHorizontalScrollBar();
        initScrollBar(hScrollBar, true);
    }

    public void initLookAndFeel() {
        backgroundColor = new Color(60, 60, 60);
        foregroundColor = new Color(255, 255, 255);
        scrollBackgroundColor = new Color(40, 40, 40);
        scrollForegroundColor = new Color(120, 120, 120);
    }

    public void initScrollBar(JScrollBar scrollBar, boolean horizontal) {
        scrollBar.setBackground(backgroundColor);
        scrollBar.setForeground(foregroundColor);
        scrollBar.setPreferredSize(new Dimension(horizontal ? 0 : 10, horizontal ? 10 : 0));
        scrollBar.setUI(new BasicScrollBarUI() {
            @Override
            protected JButton createDecreaseButton(int orientation) {
                JButton dummy = new JButton();
                dummy.setSize(0, 0);
                dummy.setBorder(null);
                return dummy;
            }
            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createDecreaseButton(orientation);
            }
            @Override
            protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
                g.setColor(scrollBackgroundColor);
                g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
            }

            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                final int p = 2;
                final int r = 2;
                g.setColor(scrollForegroundColor);
                g.fillRoundRect(thumbBounds.x+p, thumbBounds.y+p, thumbBounds.width-1-p*2, thumbBounds.height-1-p*2, r, r);
            }
        });

    }

}
