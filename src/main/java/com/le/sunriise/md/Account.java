package com.le.sunriise.md;

import java.math.BigDecimal;
import java.util.List;

public class Account implements Comparable<Account>{
    private Integer id;
    
    private Integer relatedToAccountId;
    
    private String name;
    
    private Integer type;
    
    private boolean closed;
    
    private BigDecimal startingBalance;
    
    private List<Transaction> transactions;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public BigDecimal getStartingBalance() {
        return startingBalance;
    }

    public void setStartingBalance(BigDecimal startingBalance) {
        this.startingBalance = startingBalance;
    }

    public Integer getRelatedToAccountId() {
        return relatedToAccountId;
    }

    public void setRelatedToAccountId(Integer relatedToAccountId) {
        this.relatedToAccountId = relatedToAccountId;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public int compareTo(Account o) {
        return id.compareTo(o.getId());
    }
}
