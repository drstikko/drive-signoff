package com.stephan.drivesign_off.ds;

import android.os.Handler;
import android.os.Message;

import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.FileList;

/**
 * Background runnable which is used for fetching the list of documents that can be edited.
 *
 * @class   DSFetchfilesRunnable
 */
public class DSFetchFilesRunnable extends DSGoogleDriveOperation {

    // static int variables used for identifying results
    static final int FETCH_FILES_RESULT_SUCCESS = 1001;
    static final int FETCH_FILES_RESULT_NEED_AUTHORIZATION = 1002;
    static final int FETCH_FILES_RESULT_ERROR = 1003;

    /**
     * The service on which to access the Google Drive API.
     *
     * @var Drive
     */
    private Drive _googleDriveService;

    /**
     * the handler in which to call the result.
     *
     * @var Handler
     */
    private Handler _mainHandler;

    /**
     * Constructs a new instance of the background files fetch runnable
     *
     * @param googleDriveService    The service on which to invoke the request.
     * @param mainHandler           The handler on which to call the result.
     */
    public DSFetchFilesRunnable(Drive googleDriveService, Handler mainHandler) {

        // set private variables
        this._googleDriveService = googleDriveService;
        this._mainHandler = mainHandler;
    }

    /**
     * Starts the background run of the receiver.
     */
    public void run() {

        // do nothing when already executing
        if (this.isExecuting()) {
            return;
        }

        // create the message to be sent
        Message resultMessage;

        try {

            // create a new request which finds all spreadsheets on the users Google Drive.
            Drive.Files.List queryRequest = this._googleDriveService.files().list();
            //queryRequest.setQ("mimeType='application/vnd.google-apps.spreadsheet' and properties has {key='"+ DSInsertFileRunnable.SPREADSHEET_FILE_PROPERTY_KEY + "' and value='" + DSInsertFileRunnable.SPREADSHEET_FILE_PROPERTY_VALUE + "' and visibility='PRIVATE'}");

            // execute the request and copy all files to a Java array list
           // FileList listOfFiles = queryRequest.execute();
            FileList listOfFiles=this._googleDriveService.files().list().setQ("mimeType='application/vnd.google-apps.folder'").execute();
           // for(File fl: folders.getItems()){
           //     Log.v(TAG+" fOLDER name:",fl.getTitle());
           // }
            // create the result message for all files found
            resultMessage = this._mainHandler.obtainMessage(FETCH_FILES_RESULT_SUCCESS, listOfFiles.getItems());
            this.setFinished(true);
        } catch (UserRecoverableAuthIOException exception) {

            // create a message which makes the user recover the auth exception
            resultMessage = this._mainHandler.obtainMessage(FETCH_FILES_RESULT_NEED_AUTHORIZATION, exception.getIntent());
            this.setRecoverable(true);
        } catch (Exception exception) {

            // create a message with the unknown error
            resultMessage = this._mainHandler.obtainMessage(FETCH_FILES_RESULT_ERROR, exception);
            this.setFinished(true);
        }

        // send the resulting message to its target
        this.setExecuting(false);
        resultMessage.sendToTarget();
    }
}
