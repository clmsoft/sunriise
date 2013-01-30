package com.le.sunriise.mnyobject.impl;

import java.math.BigDecimal;
import java.util.Map;

import com.le.sunriise.mnyobject.Account;

public class AccountImplUtil {

    public static Account getAcccount(Map<String, Object> row) {
        Account account = null;
        String name = (String) row.get("szFull");
        if (name == null) {
            return account;
        }
        if (name.length() == 0) {
            return account;
        }
    
        account = new AccountImpl();
    
        account.setName(name);
    
        Integer hacct = (Integer) row.get("hacct");
        account.setId(hacct);
    
        Integer hacctRel = (Integer) row.get("hacctRel");
        account.setRelatedToAccountId(hacctRel);
    
        Integer type = (Integer) row.get("at");
        account.setType(type);
    
        Boolean closed = (Boolean) row.get("fClosed");
        account.setClosed(closed);
    
        BigDecimal amtOpen = (BigDecimal) row.get("amtOpen");
        account.setStartingBalance(amtOpen);
    
        Integer currencyId = (Integer) row.get("hcrnc");
        account.setCurrencyId(currencyId);
    
        Boolean retirement = (Boolean) row.get("fRetirement");
        account.setRetirement(retirement);
    
        // 0: 403(b)
        // 1: 401k
        // 2: IRA
        // 3: Keogh
        Integer investmentSubType = (Integer) row.get("uat");
        account.setInvestmentSubType(investmentSubType);
    
        // amtLimit
        BigDecimal amountLimit = (BigDecimal) row.get("amtLimit");
        account.setAmountLimit(amountLimit);
    
        return account;
    }

}
