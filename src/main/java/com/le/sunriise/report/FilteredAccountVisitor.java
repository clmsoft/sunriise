package com.le.sunriise.report;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.le.sunriise.accountviewer.Account;

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
