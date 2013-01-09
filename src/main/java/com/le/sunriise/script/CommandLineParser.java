/*******************************************************************************
 * Copyright (c) 2010 Hung Le
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *******************************************************************************/
package com.le.sunriise.script;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

public class CommandLineParser {
    public CommandLineParser(String text) {
        if (text == null) {
            throw new IllegalArgumentException("Text cannot be null.");
        }
        this.text = text;
    }

    public List<String> parseText() {
        List<String> result = new ArrayList<String>();

        boolean returnTokens = true;
        String currentDelims = fWHITESPACE_AND_QUOTES;
        StringTokenizer parser = new StringTokenizer(text, currentDelims, returnTokens);

        String token = null;
        while (parser.hasMoreTokens()) {
            token = parser.nextToken(currentDelims);
            if (!isDoubleQuote(token)) {
                addNonTrivialWordToResult(token, result);
            } else {
                currentDelims = flipDelimiters(currentDelims);
            }
        }
        return result;
    }

    // PRIVATE //
    private String text;
    private static final Set<String> fCOMMON_WORDS = new HashSet<String>();
    private static final String fDOUBLE_QUOTE = "\"";

    // the parser flips between these two sets of delimiters
    private static final String fWHITESPACE_AND_QUOTES = " \t\r\n\"";
    private static final String fQUOTES_ONLY = "\"";

    /**
     * Very common words against which searches will not be performed.
     */
    static {
        // fCOMMON_WORDS.add("a");
        // fCOMMON_WORDS.add("and");
        // fCOMMON_WORDS.add("be");
        // fCOMMON_WORDS.add("for");
        // fCOMMON_WORDS.add("from");
        // fCOMMON_WORDS.add("has");
        // fCOMMON_WORDS.add("i");
        // fCOMMON_WORDS.add("in");
        // fCOMMON_WORDS.add("is");
        // fCOMMON_WORDS.add("it");
        // fCOMMON_WORDS.add("of");
        // fCOMMON_WORDS.add("on");
        // fCOMMON_WORDS.add("to");
        // fCOMMON_WORDS.add("the");
    }

    /**
     * Use to determine if a particular word entered in the search box should be
     * discarded from the search.
     */
    private boolean isCommonWord(String aSearchTokenCandidate) {
        return fCOMMON_WORDS.contains(aSearchTokenCandidate);
    }

    private boolean textHasContent(String aText) {
        return (aText != null) && (!aText.trim().equals(""));
    }

    private void addNonTrivialWordToResult(String aToken, List<String> aResult) {
        if (textHasContent(aToken) && !isCommonWord(aToken.trim())) {
            aResult.add(aToken.trim());
        }
    }

    private boolean isDoubleQuote(String token) {
        return token.equals(fDOUBLE_QUOTE);
    }

    private String flipDelimiters(String delims) {
        String result = null;
        if (delims.equals(fWHITESPACE_AND_QUOTES)) {
            result = fQUOTES_ONLY;
        } else {
            result = fWHITESPACE_AND_QUOTES;
        }
        return result;
    }
}
