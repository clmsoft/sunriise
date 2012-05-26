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
package com.le.sunriise.accountviewer;

public class Frequency {
    /*
frq     cFrqInst        grftt   mMemo
-1              0       
0       1       2097152 only once
1       1       2097152 daily
2       1       2097152 weekly
2       0.5     2097152 every other week
3       2       2097152 twice a month
2       0.25    2097152 every four weeks
3       1       2097152 monthly
3       0.5     2097152 every other month
4       1       2097152 every three months
3       0.25    2097152 every four months
5       2       2097152 twice a year
5       1       2097152 yearly
5       0.5     2097152 every other year     
     */
    private Integer frq;

    private Double cFrqInst;

    private Integer grftt;
    
    public Integer getGrftt() {
        return grftt;
    }

    public void setGrftt(Integer grftt) {
        this.grftt = grftt;
    }

    public Integer getFrq() {
        return frq;
    }

    public void setFrq(Integer frq) {
        this.frq = frq;
    }

    public Double getcFrqInst() {
        return cFrqInst;
    }

    public void setcFrqInst(Double cFrqInst) {
        this.cFrqInst = cFrqInst;
    }

    public boolean isRecurring() {
        Double cFrqInst = getcFrqInst();
        if ((cFrqInst != null) && (cFrqInst > 0.0)) {
            return true;
        }
        return false;
    }
    
    public String getFrequencyString() {
        StringBuilder sb = new StringBuilder();
        switch (frq) {
        case 0:
            // 0 1 2097152 only once
            if (cFrqInst == 1) {
                sb.append("Only once");
            } else {
                sb.append("Non-recurring");
            }
            break;
        case 1:
            // 1 1 2097152 daily
            if (cFrqInst == 1) {
                sb.append("Daily");
            } else {
                sb.append("Non-recurring");
            }
            break;
        case 2:
            // 2 1 2097152 weekly
            // 2 0.5 2097152 every other week
            // 2 0.25 2097152 every four weeks
            if (cFrqInst == 1) {
                sb.append("Weekly");
            } else if (cFrqInst == 0.5) {
                sb.append("Every other week");
            } else if (cFrqInst == 0.25) {
                sb.append("Every four weeks");
            } else {
                sb.append("Non-recurring");
            }
            break;
        case 3:
            // 3 2 2097152 twice a month
            // 3 1 2097152 monthly
            // 3 0.5 2097152 every other month
            // 3 0.25 2097152 every four months
            if (cFrqInst == 2) {
                sb.append("Twice a month");
            } else if (cFrqInst == 1) {
                sb.append("Monthly");
            } else if (cFrqInst == 0.5) {
                sb.append("every other month");
            } else if (cFrqInst == 0.25) {
                sb.append("Every four months");
            } else {
                sb.append("Non-recurring");
            }
            break;
        case 4:
            // 4 1 2097152 every three months
            if (cFrqInst == 1) {
                sb.append("Every three months");
            } else {
                sb.append("Non-recurring");
            }
            break;
        case 5:
            // 5 2 2097152 twice a year
            // 5 1 2097152 yearly
            // 5 0.5 2097152 every other year
            if (cFrqInst == 2) {
                sb.append("Twice a year");
            } else if (cFrqInst == 1) {
                sb.append("Yearly");
            } else if (cFrqInst == 0.5) {
                sb.append("every other year");
            } else {
                sb.append("Non-recurring");
            }
            break;
        default:
            sb.append("Non-recurring");
            break;
        }
        return sb.toString();
    }
}
