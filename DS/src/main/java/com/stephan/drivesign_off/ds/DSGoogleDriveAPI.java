package com.stephan.drivesign_off.ds;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Manager object used for accessing the Google Drive API.
 *
 * @class   DSGoogleDriveAPI
 */
public class DSGoogleDriveAPI {

    /**
     * Enum for defining the operation types.
     */
    public enum OperationType {
        OperationTypeReadFile,
        OperationTypeFetchFiles,
        OperationTypeSaveFile,
        OperationTypeCreateFile
    }

    DSGoogleDriveOperation backgroundRunnable;

    /**
     * The list of operations that are being executed or need to be executed.
     *
     * @var List<DSGoogleDriveOperation>
     */
    private List<DSGoogleDriveOperation> _operations;

    /**
     * The google drive service.
     */
    private Drive _googleDriveService;

    /**
     * The google drive API singleton object.
     *
     * @var DSGoogleDriveAPI
     */
    private static DSGoogleDriveAPI _singleton;

    /**
     * Constructs a new Google Drive API manager instance with the provided account credentials.
     *
     * @param accountCredentials    The account credentials to be used for communication.
     * @param applicationName       The name of the application.
     */
    public DSGoogleDriveAPI(GoogleAccountCredential accountCredentials, String applicationName) {

        // create the array for the operations
        this._operations = new ArrayList<DSGoogleDriveOperation>();
        _singleton = this;

        // construct the drive service instance
        this._googleDriveService = new Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), accountCredentials)
                .setApplicationName(applicationName)
                .build();
    }

    /**
     * Returns the default API manager used by the application.
     *
     * @return  DSGoogleDriveAPI | null
     */
    static public DSGoogleDriveAPI defaultManager() {

        return _singleton;
    }

    /**
     * Starts the process which fetches all files compatible for being edited in the application.
     *
     * @param delegate  The delegate object which receives the notification on the main thread.
     */
    public void fetchInitialFilesWithDelegate(final DSGoogleDriveAPIDelegate delegate) {

        // create the background runnable instance which fetches the files
        backgroundRunnable = new DSFetchFilesRunnable(this._googleDriveService, new Handler(Looper.getMainLooper()) {

            @Override
            public void handleMessage(Message resultMessage) {

                switch (resultMessage.what) {

                    case DSFetchFilesRunnable.FETCH_FILES_RESULT_SUCCESS:

                        // retrieve the files when successfully
                        List<File> allFiles = (List<File>) resultMessage.obj;

                        // call the delegate with the result
                        if (delegate != null) {
                            delegate.managerDidUpdateFiles(DSGoogleDriveAPI.this, allFiles.toArray(new File[allFiles.size()]));
                        }

                        break;

                    case DSFetchFilesRunnable.FETCH_FILES_RESULT_NEED_AUTHORIZATION:

                        // retrieve the intent that the user can use to recover
                        Intent userRecoverAuthIntent = (Intent) resultMessage.obj;

                        // call the delegate when possible with the intent
                        if (delegate != null) {
                            delegate.managerDidReceiveUserRecoverableIntent(DSGoogleDriveAPI.this, userRecoverAuthIntent);
                        }

                        break;

                    case DSFetchFilesRunnable.FETCH_FILES_RESULT_ERROR:

                        // fetch the exception created
                        Exception thrownException = (Exception) resultMessage.obj;

                        // let the delegate know about the error
                        if (delegate != null) {
                            delegate.managerDidFailOperationWithTypeAndErrorDescription(DSGoogleDriveAPI.this, OperationType.OperationTypeFetchFiles, thrownException.getMessage());
                        }

                        break;

                    default:
                        break;
                }
            }
        });

        // add the operation and call the scheduling method
        this._operations.add(backgroundRunnable);
        this.performScheduling();
    }

    /**
     * Starts the process which creates a new file on the Google Drive server.
     *
     * @param fileName  The new name of the file.
     * @param delegate  The delegate on which to show the result.
     */
    public void createFileWithNameUsingDelegate(final String fileName, final DSGoogleDriveAPIDelegate delegate) {

        // construct the new google drive insert file operation
        DSGoogleDriveOperation newOperation = new DSInsertFileRunnable(this._googleDriveService, fileName, new Handler(Looper.getMainLooper()) {

            @Override
            public void handleMessage(Message resultMessage) {

                switch (resultMessage.what) {

                    // when succeeded
                    case DSInsertFileRunnable.INSERT_FILE_RESULT_SUCCESS:

                        // let the delegate know that the file has been created
                        if (delegate != null) {
                            delegate.managerDidCreateFileWithName(DSGoogleDriveAPI.this, fileName);
                        }
                        break;

                    // when an error occured
                    case DSInsertFileRunnable.INSERT_FILE_RESULT_ERROR:

                        // get the exception
                        Exception thrownException = (Exception) resultMessage.obj;

                        if (delegate != null) {
                            delegate.managerDidFailOperationWithTypeAndErrorDescription(DSGoogleDriveAPI.this, OperationType.OperationTypeCreateFile, thrownException.getMessage());
                        }
                        break;
                }
            }
        });

        // add the operation to the queue and start scheduling
        this._operations.add(newOperation);
        this.performScheduling();
    }

    /**
     * Starts reading a file at the given download url.
     *
     * @param fileDownloadURL   The download URL from which to download the Excel file.
     */
    public void readFileWithIdUsingDelegate(String fileDownloadURL, final DSGoogleDriveAPIDelegate delegate) {

        // create the new operation which reads the file contents
        DSGoogleDriveOperation newOperation = new DSDownloadFileContentsRunnable(this._googleDriveService, fileDownloadURL, new Handler(Looper.getMainLooper()) {

            @Override
            public void handleMessage(Message resultMessage) {

                // depending on the result respond
                switch (resultMessage.what) {

                    // when successfully downloaded let the delegate know
                    case DSDownloadFileContentsRunnable.DOWNLOAD_FILE_RESULT_SUCCESS:

                        // the file is successfully downloaded so the result can be extracted
                        DSExcelWorksheet downloadedWorksheet = (DSExcelWorksheet) resultMessage.obj;

                        // when the delegate is available notify that object
                        if (delegate != null) {
                            delegate.managerDidDownloadWorksheet(DSGoogleDriveAPI.this, downloadedWorksheet);
                        }

                        break;

                    // return an error when the read has failed
                    case DSDownloadFileContentsRunnable.DOWNLOAD_FILE_RESULT_ERROR:

                        // the extra object should be an exception
                        Exception thrownException = (Exception) resultMessage.obj;

                        // let the delegate know about the error
                        if (delegate != null) {
                            delegate.managerDidFailOperationWithTypeAndErrorDescription(DSGoogleDriveAPI.this, OperationType.OperationTypeReadFile, thrownException.getMessage());
                        }
                        break;
                }
            }
        });

        // add the operation to the queue and start scheduling
        this._operations.add(newOperation);
        this.performScheduling();
    }

    /**
     * Starts the save operation of the specified file.
     *
     * @param fileIdentifier    The file identifier for the file to be saved.
     * @param fileDocument      The document that contains the parseble contents.
     * @param context           The context to use.
     * @param delegate          The delegate object which receives result messages.
     */
    public void saveFileWithIdentifierAndDocumentUsingContext(final String fileIdentifier, DSSignOffDocument fileDocument, Context context, final DSGoogleDriveAPIDelegate delegate) {

        // create the save operation
        DSGoogleDriveOperation saveOperation = new DSSaveDocumentRunnable(this._googleDriveService, fileIdentifier, fileDocument, new Handler(Looper.getMainLooper()) {

            @Override
            public void handleMessage(Message resultMessage) {

                // respond depending on the result code
                switch (resultMessage.what) {

                    // when succeeded let the delegate know
                    case DSSaveDocumentRunnable.RESULT_SAVE_SUCCEEDED:

                        // call the save succeeded delegate message when available
                        if (delegate != null) {
                            delegate.managerDidFinishSavingContentsOfFileWithTitleWithResult(DSGoogleDriveAPI.this, fileIdentifier);
                        }
                        break;

                    // when failed let the delegate know about the exception
                    case DSSaveDocumentRunnable.RESULT_SAVE_FAILED:

                        // get the exception
                        Exception thrownException = (Exception) resultMessage.obj;

                        if (delegate != null) {
                            delegate.managerDidFailOperationWithTypeAndErrorDescription(DSGoogleDriveAPI.this, OperationType.OperationTypeSaveFile, thrownException.getMessage());
                        }
                        break;
                }
            }
        }, context);

        // add the operation and start scheduling
        this._operations.add(saveOperation);
        this.performScheduling();
    }

    /**
     * Starts the scheduling of the processes currently in the queue, also removes finished processes.
     */
    private void performScheduling() {

        // get iterator for the operations
        Iterator<DSGoogleDriveOperation> operationIterator = this._operations.iterator();
        DSGoogleDriveOperation currentOperation;

        // array of items to remove
        List<DSGoogleDriveOperation> operationsToRemove = new ArrayList<DSGoogleDriveOperation>();

        // loop through all the operations
        while (operationIterator.hasNext()) {

            // get the next to process operation
            currentOperation = operationIterator.next();

            // when not finished and executing spawn the new thread
            if (!currentOperation.isFinished() && !currentOperation.isExecuting()) {

                // create and spawn the thread for the operation
                Thread spawnedThread = new Thread(currentOperation);
                spawnedThread.start();
            } else if (currentOperation.isFinished()) {

                // remove the operation when finished, add to a new list to make sure the array is not appended when iterating through this list
                operationsToRemove.add(currentOperation);
            } else if (currentOperation.isRecoverable()) {

                // start the operation again when it is recoverable, this does not mean the action that recovers the operation is already executed
                Thread spawnedThread = new Thread(currentOperation);
                spawnedThread.start();
            }
        }

        // remove all operations that need to be removed
        this._operations.removeAll(operationsToRemove);
    }
}
