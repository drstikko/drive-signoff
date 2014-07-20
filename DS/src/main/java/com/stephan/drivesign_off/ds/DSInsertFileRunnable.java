package com.stephan.drivesign_off.ds;

import android.os.Handler;
import android.os.Message;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Property;

import java.util.ArrayList;
import java.util.List;

/**
 * Background runnable used for creating a new file in the user's Google Drive.
 *
 * @class   DSInsertFileRunnable
 */
public class DSInsertFileRunnable extends DSGoogleDriveOperation {

    // result codes for the class
    static final int INSERT_FILE_RESULT_SUCCESS =   1004;
    static final int INSERT_FILE_RESULT_ERROR = 1006;

    // file custom properties used
    static String SPREADSHEET_FILE_PROPERTY_KEY     =   "SignOffCustomPropertyIdentifier";
    static String SPREADSHEET_FILE_PROPERTY_VALUE   =   "True";

    /**
     * The drive service which to be used for API calls.
     *
     * @var Drive
     */
    private Drive _driveService;

    /**
     * The name of the file to be created.
     *
     * @var String
     */
    private String _fileName;

    /**
     * The handler to use for main thread callbacks.
     *
     * @var Handler
     */
    private Handler _mainHandler;

    /**
     * Default constructor for the ad file runnable.
     *
     * @param driveService  The google drive service to be used.
     * @param fileName      The file name to be created.
     * @param mainHandler   The handler to use for callbacks.
     */
    public DSInsertFileRunnable(Drive driveService, String fileName, Handler mainHandler) {

        // set properties
        this._driveService = driveService;
        this._fileName = fileName;
        this._mainHandler = mainHandler;
    }

    /**
     * The background runnable code.
     */
    public void run() {

        // create a new file
        File newFile = new File();
        Message resultMessage;
        this.setExecuting(true);

        // configure the file name and mime type before creating it
        newFile.setTitle(this._fileName);
        newFile.setMimeType("application/vnd.google-apps.spreadsheet");

        // create a new list for the properties
        List<Property> propertyList = new ArrayList<Property>();
        Property customProperty = new Property();

        // set the key and value of the property, also make sure that the property is only visible for this application
        customProperty.setKey(SPREADSHEET_FILE_PROPERTY_KEY);
        customProperty.setValue(SPREADSHEET_FILE_PROPERTY_VALUE);
        customProperty.setVisibility("PRIVATE");

        // add the property and add these new properties to the file
        propertyList.add(customProperty);
        newFile.setProperties(propertyList);

        try {

            // execute the request to insert the file
            File insertResult = this._driveService.files().insert(newFile).execute();

            // create the message for the result of the operation
            resultMessage = this._mainHandler.obtainMessage(INSERT_FILE_RESULT_SUCCESS, insertResult);

        } catch (Exception exception) {

            // create the message with an exception
            resultMessage = this._mainHandler.obtainMessage(INSERT_FILE_RESULT_ERROR, exception);
        }

        // send message to the target loop
        this.setExecuting(false);
        this.setFinished(true);
        resultMessage.sendToTarget();
    }
}
