package com.github.aoirint.starline.tree.dnd;

import com.github.aoirint.starline.node.StarlineNode;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class StarlineNodeTransferable implements Transferable {
    public static final String NAME_NODE = "StarlineNode";
    public static final DataFlavor FLAVOR_NODE = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, NAME_NODE);

    StarlineNode node;

    public StarlineNodeTransferable(StarlineNode node) {
        this.node = node;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] { FLAVOR_NODE, };
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor.getHumanPresentableName().equals(NAME_NODE);
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (isDataFlavorSupported(flavor)) {
            return node;
        }
        throw new UnsupportedFlavorException(flavor);
    }
}
