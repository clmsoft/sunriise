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
package com.le.sunriise.account;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.le.sunriise.mnyobject.Account;

public class FilteredAccountVisitor extends DefaultAccountVisitor {
    private File filterFile;

    private List<Pattern> includePatterns = new ArrayList<Pattern>();
    private List<Pattern> excludePatterns = new ArrayList<Pattern>();
    private List<AccountFilter> accountFilters = new ArrayList<AccountFilter>();

    public File getFilterFile() {
        return filterFile;
    }

    public void setFilterFile(File filterFile) {
        this.filterFile = filterFile;

        // +includePattern
        // -excludePattern
        // @AccountFilterImpl
        
    }

    public FilteredAccountVisitor() {
        super();
    }

    @Override
    public void visitAccount(Account account) throws IOException {
        if (!acceptAccount(account)) {
            return;
        }
    }

    @Override
    public boolean acceptAccount(Account account) {
        boolean rv = false;

        // setup default behavior
        if (hasPattern(includePatterns) && hasPattern(excludePatterns)) {
            // has both
            // default is to reject
            rv = false;
        } else if (hasPattern(includePatterns)) {
            // has include pattern only, default to reject
            rv = false;
        } else if (hasPattern(excludePatterns)) {
            // has exclude pattern only, default to accept
            rv = true;
        } else {
            // no pattern specify, accept
            rv = true;
        }

        Matcher m = null;
        String input = account.getName();

        if (includePatterns != null) {
            for (Pattern p : includePatterns) {
                m = p.matcher(input);
                if (m.matches()) {
                    return true;
                }
            }
        }

        if (excludePatterns != null) {
            for (Pattern p : excludePatterns) {
                m = p.matcher(input);
                if (m.matches()) {
                    return false;
                }
            }
        }

        if (accountFilters != null) {
            for (AccountFilter accountFilter : accountFilters) {
                if (!accountFilter.accept(account)) {
                    return false;
                }
            }
        }

        return rv;
    }

    private boolean hasPattern(List<Pattern> patterns) {
        return (patterns != null) && (patterns.size() > 0);
    }

}
