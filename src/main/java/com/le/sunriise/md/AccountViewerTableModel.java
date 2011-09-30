package com.le.sunriise.md;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class AccountViewerTableModel extends AbstractAccountViewerTableModel {
    private static final Logger log = Logger.getLogger(AccountViewerTableModel.class);

    private static final int COLUMN_ID = 0;
    private static final int COLUMN_DATE = COLUMN_ID + 1;
    private static final int COLUMN_PAYEE = COLUMN_DATE + 1;
    private static final int COLUMN_CATEGORY = COLUMN_PAYEE + 1;
    // private static final int COLUMN_CLASSIFICATION = 4;
    private static final int COLUMN_AMOUNT = COLUMN_CATEGORY + 1;
    private static final int COLUMN_BALANCE = COLUMN_AMOUNT + 1;
    private static final int COLUMN_VOIDED = COLUMN_BALANCE + 1;

    public AccountViewerTableModel(Account account) {
        super(account);
    }

    @Override
    public int getRowCount() {
        if (getAccount() != null) {
            return getAccount().getTransactions().size();
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
        List<Transaction> transactions = getAccount().getTransactions();
        Transaction transaction = transactions.get(rowIndex);
        switch (columnIndex) {
        case COLUMN_ID:
            value = transaction.getId();
            break;
        case COLUMN_DATE:
            value = transaction.getDate();
            break;
        case COLUMN_PAYEE:
            value = toPayeeString(transaction);
            break;
        case COLUMN_CATEGORY:
            value = toCategoryString(transaction);
            break;
        // case COLUMN_CLASSIFICATION:
        // value = "classification";
        // break;
        case COLUMN_AMOUNT:
            value = transaction.getAmount();
            break;
        case COLUMN_BALANCE:
            value = AccountUtil.getRunningBalance(rowIndex, getAccount());
            break;
        case COLUMN_VOIDED:
            value = transaction.isVoid();
            break;

        default:
            value = null;
            break;
        }

        return value;
    }

    private Object toPayeeString(Transaction transaction) {
        Object value;
        Integer payeeId = transaction.getPayeeId();
        String payeeName = null;
        if (payeeId != null) {
            Map<Integer, Payee> payees = getMnyContext().getPayees();
            if (payees != null) {
                Payee payee = payees.get(payeeId);
                if (payee != null) {
                    payeeName = payee.getName();
                }
            }
        }
        if (payeeName == null) {
            payeeName = "";
        }
        value = payeeName;

        if (transaction.isInvestment()) {
            value = "UNKNOWN";
            InvestmentActivity investmentActivity = transaction.getInvestmentActivity();
            if (investmentActivity != null) {
                value = investmentActivity.toString();
            }
        }

        return value;
    }

    private Object toCategoryString(Transaction transaction) {
        Object value = null;
        Integer transferredAccountId = transaction.getTransferredAccountId();
        if (transferredAccountId != null) {
            List<Account> accounts = getMnyContext().getAccounts();
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

        if (transaction.isInvestment()) {
            Integer securityId = transaction.getSecurityId();
            if (securityId != null) {
                Map<Integer, Security> securities = getMnyContext().getSecurities();
                Security security = securities.get(securityId);
                if (security != null) {
                    value = security.getName();
                } else {
                    value = securityId.toString();
                }
            }
        }
        return value;
    }

    private String getCategoryName(Integer categoryId) {
        String categoryName = null;
        if (categoryId != null) {
            Map<Integer, Category> categories = getMnyContext().getCategories();
            if (categories != null) {
                Category category = categories.get(categoryId);
                if (category != null) {
                    Integer parentId = category.getParentId();
                    categoryName = category.getName();
                    if (parentId != null) {
                        String parentName = getCategoryName(parentId);
                        categoryName = parentName + ":" + categoryName;
                    }
                }
            }
        }
        return categoryName;
    }

    @Override
    public String getColumnName(int column) {
        String value = null;
        switch (column) {
        case COLUMN_ID:
            value = "ID";
            break;
        case COLUMN_DATE:
            value = "Date";
            break;
        case COLUMN_PAYEE:
            value = "Payee";
            break;
        case COLUMN_CATEGORY:
            value = "Category";
            break;
        // case COLUMN_CLASSIFICATION:
        // value = "Classification";
        // break;
        case COLUMN_AMOUNT:
            value = "Amount";
            break;
        case COLUMN_BALANCE:
            value = "Balance";
            break;
        case COLUMN_VOIDED:
            value = "Voided";
            break;
        default:
            value = null;
            break;
        }

        return value;
    }
}