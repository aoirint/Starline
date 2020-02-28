package com.github.aoirint.starline.tree.dnd;

import com.github.aoirint.starline.tree.StarlineTree;

import javax.swing.*;

public class StarlineTreeTransferHandler extends TransferHandler {
    StarlineTree tree;

    public StarlineTreeTransferHandler(StarlineTree tree) {
        this.tree = tree;
    }

}
