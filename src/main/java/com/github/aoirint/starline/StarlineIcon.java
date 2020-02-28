package com.github.aoirint.starline;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class StarlineIcon {
    public static Icon generateCircleIcon(Color color) {
        BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics g = image.getGraphics();

        final int b = 2;
        final int p = 8;
        g.setColor(color.darker());
        g.fillOval(p-b, p-b, image.getWidth()-(p-b)*2, image.getHeight()-(p-b)*2);
        g.setColor(color);
        g.fillOval(p, p, image.getWidth()-p*2, image.getHeight()-p*2);

        Image scaledImage = image.getScaledInstance(16, 16, Image.SCALE_SMOOTH);

        ImageIcon icon = new ImageIcon(scaledImage);
        return icon;
    }

}
