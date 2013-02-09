/*******************************************************************************
 * Copyright (c) 2012 Hung Le
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
package com.le.sunriise.model.bean;

import org.apache.log4j.Logger;

public class PasswordCheckerModel {
    private static final Logger log = Logger.getLogger(PasswordCheckerModel.class);
    
    private String mnyFileName;
    private String wordListPath;
    private Integer threads = new Integer(1);
    private String status = "Idle";
    
    public PasswordCheckerModel() {        
        super();
        log.info("> PasswordCheckerModel");
    }

    public String getMnyFileName() {
        return mnyFileName;
    }

    public void setMnyFileName(String mnyFileName) {
        this.mnyFileName = mnyFileName;
    }

    public String getWordListPath() {
        return wordListPath;
    }

    public void setWordListPath(String wordListPath) {
        this.wordListPath = wordListPath;
    }

    public Integer getThreads() {
        return threads;
    }

    public void setThreads(Integer threads) {
        this.threads = threads;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
