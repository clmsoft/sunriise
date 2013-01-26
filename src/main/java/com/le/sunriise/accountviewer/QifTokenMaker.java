package com.le.sunriise.accountviewer;

import javax.swing.text.Segment;

import org.apache.log4j.Logger;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.modes.PlainTextTokenMaker;

final class QifTokenMaker extends PlainTextTokenMaker {
    private static final Logger log = Logger.getLogger(QifTokenMaker.class);

    @Override
    public Token getTokenList(Segment text, int startTokenType, final int startOffset) {
        if (log.isDebugEnabled()) {
            log.debug("getTokenList, text=" + text.toString());
            log.debug("  startTokenType=" + startTokenType);
            log.debug("  startOffset=" + startOffset);
        }
        return super.getTokenList(text, startTokenType, startOffset);
    }

}