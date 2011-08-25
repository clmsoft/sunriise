package com.le.sunriise.liveid;

import java.io.IOException;

import javax.xml.soap.SOAPException;

import org.apache.log4j.Logger;

import com.jp.windows.live.LogonManager;
import com.jp.windows.live.LogonManagerException;
import com.jp.windows.live.SecurityToken;

public class LoginToLiveId {
    private static final Logger log = Logger.getLogger(LoginToLiveId.class);

    /**
     * @param args
     */
    public static void main(String[] args) {
        String endpointReference = "live.com";
        String userName = null;
        String password = null;

        if (args.length == 2) {
            userName = args[0];
            password = args[1];
        } else if (args.length == 3) {
            userName = args[0];
            password = args[1];
            endpointReference = args[2];
        } else {
            Class<LoginToLiveId> clz = LoginToLiveId.class;
            System.out.println("Usage: java " + clz.getName() + " userName password [endpointReference]");
            System.exit(1);
        }

        log.info("userName=" + userName);
        log.info("endpointReference=" + endpointReference);

        LoginToLiveId liveId = new LoginToLiveId();
        try {
            liveId.login(endpointReference, userName, password);
        } catch (LogonManagerException e) {
            log.error(e, e);
        } catch (IOException e) {
            log.error(e, e);
        } catch (SOAPException e) {
            log.error(e, e);
        }
    }

    private void login(String endpointReference, String userName, String password) throws LogonManagerException, IOException, SOAPException {
        SecurityToken securityToken = new LogonManager().logon(endpointReference, userName, password);
        log.info("Logon succeeded!");
        log.info("Passport Token: " + securityToken.getBinarySecurityToken());
        log.info("Issue Date: " + securityToken.getIssueDate());
        log.info("Expire Date: " + securityToken.getExpireDate());
    }
}
