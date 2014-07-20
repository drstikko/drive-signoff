package com.stephan.drivesign_off.ds;

import android.os.Handler;
import android.os.Message;
import android.util.Xml;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.services.drive.Drive;

import org.xmlpull.v1.XmlPullParser;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Background operation which is used to download and parse contents for a file to be edited in the application.
 *
 * @class   DSDownloadFileContentsRunnable
 */
public class DSDownloadFileContentsRunnable extends DSGoogleDriveOperation {

    // expectad excel xml data format names
    static String EXCEL_XML_FORMAT_ROW_TAG  =   "row";
    static String EXCEL_XML_FORMAT_ROW_ID_ATTRIBUTE   =   "r";

    // excel format tags
    static String EXCEL_XML_FORMAT_CELL_TAG =   "c";
    static String EXCEL_XML_FORMAT_CELL_ID_ATTRIBUTE  =   "r";
    static String EXCEL_XML_FORMAT_CELL_TYPE_ATTRIBUTE  =   "t";
    static String EXCEL_XML_FORMAT_CELL_VALUE_TAG   =   "v";

    // shared strings xml tag values
    static String EXCEL_FORMAT_SHARED_STRING_TAG    =   "si";
    static String EXCEL_FORMAT_SHARED_STRING_VALUE_TAG  =   "t";

    // int results for the finished operation
    static final int DOWNLOAD_FILE_RESULT_SUCCESS   =   1006;
    static final int DOWNLOAD_FILE_RESULT_ERROR     =   1007;

    /**
     * The google drive service object to be used.
     *
     * @var _driveService
     */
    private Drive _driveService;

    /**
     * The URL string of the file to be downloaded.
     *
     * @var _downloadURL
     */
    private String _downloadURL;

    /**
     * The runloop handler on which to create messages for the callback.
     *
     * @var _mainHandler
     */
    private Handler _mainHandler;

    /**
     * The worksheet that is being parsed.
     *
     * @var DSExcelWorksheet
     */
    private DSExcelWorksheet _worksheet;

    /**
     * The current worksheet row being created.
     *
     * @var _currentWorksheetRow
     */
    private DSExcelWorksheetRow _currentWorksheetRow;

    /**
     * The current worksheet cell being edited.
     *
     * @var _currentWorksheetCell
     */
    private DSExcelWorksheetCell _currentWorksheetCell;

    /**
     * Constructs a new download contents runnabe object.
     *
     * @param driveService  The service to use for the connection.
     * @param downloadURL   The URL of the file to be downloaded.
     * @param mainHandler   The handler to use for messages and callbacks.
     */
    public DSDownloadFileContentsRunnable(Drive driveService, String downloadURL, Handler mainHandler) {

        // create initial values
        this._driveService = driveService;
        this._downloadURL = downloadURL;
        this._mainHandler = mainHandler;
        this._worksheet = new DSExcelWorksheet();
    }

    /**
     * Starts the operation in the background.
     */
    public void run() {

        // set to executing and create default empty message
        Message resultMessage;
        this.setExecuting(true);

        try {

            // create the request and fetch the result for the download file
            HttpResponse response = this._driveService.getRequestFactory().buildGetRequest(new GenericUrl(_downloadURL)).execute();
            InputStream inputStream = response.getContent();

            // create the zip input stream which reads the zip file
            ZipInputStream zipInputStream = new ZipInputStream(inputStream);
            ZipEntry currentZipEntry;
            DSExcelWorksheetSharedStrings sharedStrings = null;

            // parse each file in the zip
            while ((currentZipEntry = zipInputStream.getNextEntry()) != null) {

                // check whether the file is the first sheet xml file, when so start the parsing of this file and check the result
                if (currentZipEntry.getName().contains("sheet1.xml") && !this.parseXMLDataFromWorksheet(zipInputStream)) {

                    // throw exception explaining the error
                    throw new Exception("Failed to parse the XML worksheet, the data format could not be valid to be able to be parsed by this application.");
                } else if (currentZipEntry.getName().contains("sharedStrings.xml")) {

                    // start the parse operation for the shared strings document
                    sharedStrings = this.parseXMLDataFromSharedStrings(zipInputStream);
                }

                // close the current entry
                zipInputStream.closeEntry();
            }

            // make sure the shared string contents is parsed before merging the strings with the worksheet
            if (sharedStrings != null) {
                this._worksheet.mergeWorksheetWithSharedStrings(sharedStrings);
            } else {

                // throw exception because the file is not available
                throw new Exception("Failed to parse the sharedStrings XML document from the Excel file.");
            }

            // create a message that will be consumed by the handler
            resultMessage = this._mainHandler.obtainMessage(DOWNLOAD_FILE_RESULT_SUCCESS, this._worksheet);
            zipInputStream.close();

        } catch (Exception exception) {

            // create the message with the exception
            resultMessage = this._mainHandler.obtainMessage(DOWNLOAD_FILE_RESULT_ERROR, exception);
        }

        // mark as finished and not executing
        this.setFinished(true);
        this.setExecuting(false);

        // send the message to the target
        resultMessage.sendToTarget();
    }

    /**
     * PArses the XML as a shared string resource and returns the apprppriate object result.
     *
     * @param xmlInputStream    The input stream containing the XML bytes.
     * @return                  The resulting parsed object or null when an exception occured.
     */
    private DSExcelWorksheetSharedStrings parseXMLDataFromSharedStrings(InputStream xmlInputStream) {

        // create the result
        DSExcelWorksheetSharedStrings sharedStrings = new DSExcelWorksheetSharedStrings();

        try {

            // construct the new XML pull parser object
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(xmlInputStream, null);

            // start the first event
            int event = parser.getEventType();
            boolean isParsingSharedStringTag = false;

            // keep parsing events until the end of the document
            while (event != XmlPullParser.END_DOCUMENT) {

                // get the name of the current tag being edited
                String currentName = parser.getName();
                switch (event) {

                    // on the starting tag
                    case XmlPullParser.START_TAG:

                        // check for the start of a shared string
                        if (currentName.equals(EXCEL_FORMAT_SHARED_STRING_VALUE_TAG)) {
                            isParsingSharedStringTag = true;
                        }
                        break;

                    // on the ending tag
                    case XmlPullParser.END_TAG:

                        // check for the start of a shared string
                        if (currentName.equals(EXCEL_FORMAT_SHARED_STRING_VALUE_TAG)) {
                            isParsingSharedStringTag = false;
                        }
                        break;

                    // when text in a tag is received
                    case XmlPullParser.TEXT:

                        // add the new shared string when needed
                        if (isParsingSharedStringTag) {
                            sharedStrings.addSharedString(parser.getText());
                        }

                        break;
                }

                // start the next
                event = parser.next();
            }
        } catch (Exception exception) {

            // set the shared strings object to null
            sharedStrings = null;
        }

        // return the created shared strings
        return sharedStrings;
    }

    /**
     * Parses the input provided into a worksheet and creates the classes private worksheet.
     *
     * @param xmlInputStream    The input stream which contains the XML data.
     */
    private boolean parseXMLDataFromWorksheet(InputStream xmlInputStream) {

        // set default return value
        boolean returnValue = true;

        try {

            // construct the new XML pull parser object
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(xmlInputStream, null);

            int event = parser.getEventType();

            // parse until the end of the document
            while (event != XmlPullParser.END_DOCUMENT) {

                String currentName = parser.getName();

                switch (event) {

                    // on the start of a tag
                    case XmlPullParser.START_TAG:

                        // check for a row tag
                        if (currentName.equals(EXCEL_XML_FORMAT_ROW_TAG)) {

                            try {

                                // parse the integer and add the worksheet
                                int rowIndex = Integer.parseInt(parser.getAttributeValue(null, EXCEL_XML_FORMAT_ROW_ID_ATTRIBUTE));
                                this._currentWorksheetRow = new DSExcelWorksheetRow(rowIndex);

                            } catch (NumberFormatException formatException) {

                            }

                        } else if (currentName.equals(EXCEL_XML_FORMAT_CELL_TAG)) {

                            // get all attribute values and add a new cell object to the worksheet row currently edited
                            String cellId = parser.getAttributeValue(null, EXCEL_XML_FORMAT_CELL_ID_ATTRIBUTE);
                            String cellType = parser.getAttributeValue(null, EXCEL_XML_FORMAT_CELL_TYPE_ATTRIBUTE);
                            this._currentWorksheetCell = new DSExcelWorksheetCell(cellId, cellType);

                        }
                        break;

                    // on text received in a tag value
                    case XmlPullParser.TEXT:
                        this._currentWorksheetCell.setCellValue(parser.getText());
                        break;

                    // on the end of a tag
                    case XmlPullParser.END_TAG:

                        // check for a row tag
                        if (currentName.equals(EXCEL_XML_FORMAT_ROW_TAG)) {

                            // add the row to the worksheet and reset the value
                            this._worksheet.addWorksheetRow(this._currentWorksheetRow);
                            this._currentWorksheetRow = null;
                        } else if (currentName.equals(EXCEL_XML_FORMAT_CELL_TAG)) {

                            // add the cell to the row and reset the private instance
                            this._currentWorksheetRow.addWorksheetCell(this._currentWorksheetCell);
                            this._currentWorksheetCell = null;
                        }
                        break;
                }

                // get the event result for the next tag
                event = parser.next();
            }

        } catch (Exception exception) {

            // mark as failed
            returnValue = false;
        }

        return returnValue;
    }
}
