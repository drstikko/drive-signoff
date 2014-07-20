package com.stephan.drivesign_off.ds;

import android.content.Intent;

import com.google.api.services.drive.model.File;

/**
 * Delegate interface for objects that want to respond to callbacks from the Google Drive API manager.
 *
 * @abstract    DSGoogleDriveAPIDelegate
 */
public interface DSGoogleDriveAPIDelegate {

    /**
     * this callback is activated when the API manager fetched a new list of files to be shown.
     *
     * @param apiInstance   The instance that instantiated the request.
     * @param allFiles      The new array of files that are downloaded.
     */
    public void managerDidUpdateFiles(DSGoogleDriveAPI apiInstance, File[] allFiles);

    /**
     * Method is called when a user recoverable intent is thrown from an exception.
     *
     * @param apiInstance   The instance that encountered the exception.
     * @param intent        The intent that can recover the exception.
     */
    public void managerDidReceiveUserRecoverableIntent(DSGoogleDriveAPI apiInstance, Intent intent);

    /**
     * Method is called when a downloaded worksheet is completly available to be used.
     *
     * @param apiInstance   The instance that downloaded the file.
     * @param worksheet     The result worksheet.
     */
    public void managerDidDownloadWorksheet(DSGoogleDriveAPI apiInstance, DSExcelWorksheet worksheet);

    /**
     * This method is called when an error has occured during a background running process.
     *
     * @param apiInstance       The instance that created the error.
     * @param operationType     The type of operation that failed.
     * @param errorMessage      The error message describing the failure.
     */
    public void managerDidFailOperationWithTypeAndErrorDescription(DSGoogleDriveAPI apiInstance, DSGoogleDriveAPI.OperationType operationType, String errorMessage);

    /**
     * this method is called when a file that has been requested to be saved has finished its saving process, this could be a failure.
     *
     * @param apiInstance   The instance that started the save.
     * @param fileTitle     The title of the file that failed to be saved.
     */
    public void managerDidFinishSavingContentsOfFileWithTitleWithResult(DSGoogleDriveAPI apiInstance, String fileTitle);

    /**
     * Gets called when a new file has successfully been created.
     *
     * @param apiInstance   The instance that created the file.
     * @param fileName      The name of the file.
     */
    public void managerDidCreateFileWithName(DSGoogleDriveAPI apiInstance, String fileName);
}
