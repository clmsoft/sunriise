package com.le.sunriise.md;

public class InvestmentActivity {
    // TRN.act

    private final Integer flag;
    private String string;

    // Buy: act=1, grftt=18, grftf=0
    // Sell: act=2, grftt=18, grftf=0
    // Dividend: act=3, grftt=18, grftf=0
    // Interest: act=4, grftt=18, grftf=0
    // Return of Capital: act=8
    // Reinvest Dividend: act=9, grftt=16, grftf=0
    // Reinvest Interest: act=10, grftt=16, grftf=0
    // Remove Shares: act=13
    // Add Shares: act=12, grftt=16, grftf=0
    // S-Term Cap Gains Dist: act=24, grftt=18, grftf=0
    // L-Term Cap Gains Dist: act=26, grftt=18, grftf=0
    // Reinvest S-Term CG Dist: act=27, grftt=16, grftf=0
    // Reinvest L-Term CG Dist: act=29, grftt=16, grftf=0
    // Transfer Shares (in): act=32
    // Transfer Shares (out): act=33

    private boolean added = true;

    public InvestmentActivity(Integer flag) {
        super();
        this.flag = flag;
        initialize();
    }

    public Integer getFlag() {
        return flag;
    }

    @Override
    public String toString() {
        return string;
    }

    private void initialize() {
        this.added = true;
        String str = "ACTIVITY_UNKNOWN";
        switch (flag) {
        case 1:
            str = "Buy";
            break;
        case 2:
            str = "Sell";
            this.added = false;
            break;
        case 3:
            str = "Dividend";
            break;
        case 4:
            str = "Interest";
            break;
        case 8:
            str = "Return of Capital";
            break;
        case 9:
            str = "Reinvest Dividend";
            break;
        case 10:
            str = "Reinvest Interest";
            break;
        case 13:
            str = "Remove Shares";
            this.added = false;
            break;
        case 16:
            str = "Add Shares";
            break;
        case 24:
            str = "S-Term Cap Gains Dist";
            break;
        case 26:
            str = "L-Term Cap Gains Dist";
            break;
        case 27:
            str = "Reinvest S-Term CG Dist";
            break;
        case 29:
            str = "Reinvest L-Term CG Dist";
            break;
        case 32:
            str = "Transfer Shares (in)";
            break;
        case 33:
            str = "Transfer Shares (out)";
            this.added = false;
            break;
        default:
            str = "ACTIVITY_UNKNOWN";
            break;
        }

        this.string = str;
    }

    public boolean isAdded() {
        return added;
    }
}
