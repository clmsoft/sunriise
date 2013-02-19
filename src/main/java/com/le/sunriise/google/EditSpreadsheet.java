package com.le.sunriise.google;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetFeed;
import com.google.gdata.util.ServiceException;

public class EditSpreadsheet {
    private static final Logger log = Logger.getLogger(EditSpreadsheet.class);

    // https://developers.google.com/google-apps/spreadsheets/

    // private static final String DEFAULT_KEY =
    // "https://docs.google.com/spreadsheet/pub?key=0AuxTv-k5qT8DdGxBT2xiTXJpNXBNX2tPVmlESVFTdEE&output=html";
    private static final String DEFAULT_KEY = "0AuxTv-k5qT8DdGxBT2xiTXJpNXBNX2tPVmlESVFTdEE";
    private static final String DEFAULT_URL = "https://spreadsheets.google.com/feeds/worksheets/" + DEFAULT_KEY + "/public/full";

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            SpreadsheetService service = new SpreadsheetService("SunriiseTest-v1");
            String userName = null;
            String password = null;
            userName = "hleofxquotes";
            password = "D363736f";
            userName = "hle999";
            password = "S252625d";
            if (userName != null) {
                service.setUserCredentials(userName, password);
            }

            // Define the URL to request. This should never change.
            URL SPREADSHEET_FEED_URL = new URL("https://spreadsheets.google.com/feeds/spreadsheets/private/full");

            SPREADSHEET_FEED_URL = null;
            if (SPREADSHEET_FEED_URL != null) {
                // Make a request to the API and get all spreadsheets.
                SpreadsheetFeed feed = service.getFeed(SPREADSHEET_FEED_URL, SpreadsheetFeed.class);
                List<SpreadsheetEntry> spreadsheets = feed.getEntries();

                // Iterate through all of the spreadsheets returned
                for (SpreadsheetEntry spreadsheet : spreadsheets) {
                    // Print the title of this spreadsheet to the screen
                    log.info("title: " + spreadsheet.getTitle().getPlainText());
                    log.info("  " + spreadsheet.getHtmlLink().getHref());
                    log.info("  " + spreadsheet.getKey());
                }
            }

            // http://spreadsheets.google.com/feeds/feed/key/worksheet/public/basic?alt=json-in-script&callback=myFunc
            URL entryUrl = new URL("http://spreadsheets.google.com/feeds/0AuxTv-k5qT8DdGxBT2xiTXJpNXBNX2tPVmlESVFTdEE/worksheet/public/basic");
            
            SpreadsheetEntry spreadsheet = service.getEntry(entryUrl, SpreadsheetEntry.class);
            log.info("title: " + spreadsheet.getTitle().getPlainText());
            log.info("  " + spreadsheet.getHtmlLink().getHref());
            log.info("  " + spreadsheet.getKey());
            log.info("  " + spreadsheet.getId());
            log.info("  " + spreadsheet.getKind());

        } catch (MalformedURLException e) {
            log.error(e, e);
        } catch (IOException e) {
            log.error(e, e);
        } catch (ServiceException e) {
            log.error(e, e);
        }

    }

}
