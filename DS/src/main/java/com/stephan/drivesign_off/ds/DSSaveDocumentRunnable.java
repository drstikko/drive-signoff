package com.stephan.drivesign_off.ds;

import android.os.Handler;
import android.os.Message;

import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

/**
 * Background runnable to use for saving the contents of a document.
 *
 * @class   DSSaveDocumentRunnable
 */
public class DSSaveDocumentRunnable extends DSGoogleDriveOperation {

    // result codes for the receiver
    static final int RESULT_SAVE_FAILED         =   12;
    static final int RESULT_SAVE_SUCCEEDED      =   15;

    /**
     * The service used for saving the data.
     *
     * @var Drive
     */
    private Drive _driveService;

    /**
     * The document that will be processed and saved.
     *
     * @var DSSignOffDocument
     */
    private DSSignOffDocument _documentToSave;

    /**
     * The main runloop handler for the result callback.
     *
     * @var Handler
     */
    private Handler _mainHandler;

    /**
     * the file identifier which will be updated.
     *
     * @var String
     */
    private String _fileIdentifier;

    /**
     * The context used for file locations.
     *
     * @var Context
     */
    private android.content.Context _context;

    /**
     * Constructs a new save file runnable which saves the content to the server.
     *
     * @param driveService      The service being used to save.
     * @param fileIdentifier    The file ientifier to update.
     * @param documentToSave    The document to be saved.
     * @param mainHandler       The handler used for callbacks.
     * @param context           The context used for file locations.
     */
    public DSSaveDocumentRunnable(Drive driveService, String fileIdentifier, DSSignOffDocument documentToSave, Handler mainHandler, android.content.Context context) {

        // set private variables
        this._documentToSave = documentToSave;
        this._driveService = driveService;
        this._mainHandler = mainHandler;
        this._fileIdentifier = fileIdentifier;
        this._context = context;
    }

    /**
     * Starts the background execution of the runnable.
     */
    public void run() {

        // set the runnable as executing
        this.setExecuting(true);
        Message resultMessage;

        // create the string for file contents
        StringBuilder fileContentsStringBuilder = new StringBuilder();

        // initially write the student name and id to the output
        fileContentsStringBuilder.append("\"Studentnummer\",\"Naam\"");
        for (String currentAssignmentName : this._documentToSave.allAssignments()) {
            fileContentsStringBuilder.append(",\"" + currentAssignmentName + "\"");
        }

        // write new line to begin students info
        fileContentsStringBuilder.append("\n");

        // get student iterator for information to write
        Iterator<DSStudent> studentIterator = this._documentToSave.studentIterator();
        DSStudent currentStudent;

        // process each
        while (studentIterator.hasNext()) {

            // get the current student and write the initial id and name to the file
            currentStudent = studentIterator.next();
            fileContentsStringBuilder.append("\"" + currentStudent.studentIdentifier() + "\",\"" + currentStudent.studentName() + "\"");

            // then write all assignment values for the current student
            for (String currentAssignment : this._documentToSave.allAssignments()) {
                fileContentsStringBuilder.append(",\"" + currentStudent.valueForAssignment(currentAssignment) + "\"");
            }

            // finally add a newline
            fileContentsStringBuilder.append("\n");
        }

        try {

            // create the temp file
            java.io.File fileWithCSVContents = new java.io.File(this._context.getFilesDir(), "TEMP-" + this._fileIdentifier);
            File fileToUpdate = this._driveService.files().get(this._fileIdentifier).execute();

            // create a temp output stream for the file in which the comma seperated file will be written to
            FileOutputStream tempFileOutputStream = new FileOutputStream(fileWithCSVContents);
            tempFileOutputStream.write(fileContentsStringBuilder.toString().getBytes());
            tempFileOutputStream.close();

            // create the content for the request
            FileContent mediaContent = new FileContent("text/csv", fileWithCSVContents);

            // start the file upload and delete the temp file
            this._driveService.files().update(this._fileIdentifier, fileToUpdate, mediaContent).execute();

            // make sure the file is deleted before finishing the operation, do not respond on error
            fileWithCSVContents.delete();
            resultMessage = this._mainHandler.obtainMessage(RESULT_SAVE_SUCCEEDED);

        } catch (IOException exception) {

            // create a failure message
            resultMessage = this._mainHandler.obtainMessage(RESULT_SAVE_FAILED, exception);
        }

        // mark the runnable as finished
        this.setFinished(true);
        this.setExecuting(false);
        resultMessage.sendToTarget();
    }
}
