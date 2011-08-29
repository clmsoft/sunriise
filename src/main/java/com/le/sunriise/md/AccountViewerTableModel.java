package com.le.sunriise.md;

import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

public class AccountViewerTableModel extends AbstractTableModel {
    private final Account account;

    private Map<Integer, Payee> payees;

    private Map<Integer, Category> categories;

    private List<Account> accounts;

    public AccountViewerTableModel(Account account) {
        this.account = account;
    }

    @Override
    public int getRowCount() {
        if (account != null) {
            return account.getTransactions().size();
        } else {
            return 0;
        }
    }

    @Override
    public int getColumnCount() {
        return 7;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Object value = null;
        List<Transaction> transactions = account.getTransactions();
        Transaction transaction = transactions.get(rowIndex);
        switch (columnIndex) {
        case 0:
            value = transaction.getId();
            break;
        case 1:
            value = transaction.getDate();
            break;
        case 2:
            Integer payeeId = transaction.getPayeeId();
            String payeeName = getPayeeName(payeeId);
            value = payeeName;
            break;
        case 3:
            Integer transferredAccountId = transaction.getTransferredAccountId();
            if (transferredAccountId != null) {
                if (accounts != null) {
                    for (Account a : accounts) {
                        Integer id = a.getId();
                        if (id.equals(transferredAccountId)) {
                            value = "Transfer to " + a.getName();
                            break;
                        }
                    }
                }
            } else {
                Integer categoryId = transaction.getCategoryId();
                String categoryName = getCategoryName(categoryId);
                value = categoryName;
            }

            if (value == null) {
                List<TransactionSplit> splits = transaction.getSplits();
                if ((splits != null) && (splits.size() > 0)) {
                    value = "(" + splits.size() + ") Split Transaction";
                }
            }
            break;
        case 4:
            value = transaction.getAmount();
            break;
        case 5:
            value = AccountUtil.getRunningBalance(rowIndex, account);
            break;
        case 6:
            value = transaction.isVoid();
            break;
        // case 7:
        // value = transaction.isRecurring();
        // break;
        default:
            value = null;
            break;
        }

        return value;
    }

    private String getCategoryName(Integer categoryId) {
        String categoryName = null;
        if (categoryId != null) {
            if (categories != null) {
                Category category = categories.get(categoryId);
                if (category != null) {
                    Integer parentId = category.getParentId();
                    categoryName = category.getName();
                    if (parentId != null) {
                        String parentName = getCategoryName(parentId);
                        categoryName = parentName + " : " + categoryName;
                    }
                }
            }
        }
        return categoryName;
    }

    private String getPayeeName(Integer payeeId) {
        String payeeName = null;
        if (payeeId != null) {
            if (payees != null) {
                Payee payee = payees.get(payeeId);
                if (payee != null) {
                    payeeName = payee.getName();
                }
            }
        }
        if (payeeName == null) {
            payeeName = payeeId.toString();
        }
        return payeeName;
    }

    @Override
    public String getColumnName(int column) {
        String value = null;
        switch (column) {
        case 0:
            value = "ID";
            break;
        case 1:
            value = "Date";
            break;
        case 2:
            value = "Payee";
            break;
        case 3:
            value = "Category";
            break;
        case 4:
            value = "Amount";
            break;
        case 5:
            value = "Balance";
            break;
        case 6:
            value = "Voided";
            break;
        // case 7:
        // value = "Recurring";
        // break;
        default:
            value = null;
            break;
        }

        return value;
    }

    public Map<Integer, Payee> getPayees() {
        return payees;
    }

    public void setPayees(Map<Integer, Payee> payees) {
        this.payees = payees;
    }

    public Map<Integer, Category> getCategories() {
        return categories;
    }

    public void setCategories(Map<Integer, Category> categories) {
        this.categories = categories;
    }

    public Account getAccount() {
        return account;
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }
}