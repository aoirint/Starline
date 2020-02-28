package com.github.aoirint.starline;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class LimitedTextDocument extends PlainDocument {
    private int limit;

    public LimitedTextDocument(int limit) {
        super();
        this.limit = limit;
    }

    @Override
    public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
        if (str == null) return;

        int currentLength = getLength();
        int insertingLength = str.length();
        if (this.limit < currentLength + insertingLength) return;

        super.insertString(offs, str, a);
    }

}
