package com.stephan.drivesign_off.ds;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.app.ActionBar;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;

import java.util.Collections;

//import android.support.v7.app.ActionBarActivity;
//import android.transition.Visibility;

/**
 * The initial activity of the application.
 *
 * @class   DSLoginActivity
 */
public class DSLoginActivity extends Activity implements DSGoogleDriveAPIDelegate {

    /**
     * Login activity identifier.
     *
     * @var GOOGLE_API_LOGIN_ACTIVITY
     */
    static int ACTIVITY_AUTHORIZATION_INTENT    =   1;
    static int INTENT_ACCOUNT_CHOOSER           =   2;
    static int ACTIVITY_CREATE_FILE             =   3;

    Intent accountSelectionIntent;
    /**
     * The table view holding all the different files that can be edited by the app.
     *
     * @var TableLayout
     */
    private TableLayout _filesTableView;

    /**
     * The progress bar indicating refreshing activity.
     *
     * @var ProgressBar
     */
    private ProgressBar _progressBar;

    /**
     * The API instance used for fetching info from Google Drive.
     *
     * @var DSGoogleDriveAPI
     */
    private DSGoogleDriveAPI _googleDriveAPI;

    /**
     * For debugging.
     *
     * @var String
     */
    private String _accountName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dslogin);

        // retrieve the table layout for the files overview
        this._filesTableView = (TableLayout) this.findViewById(R.id.fileTableView);
        this._progressBar = (ProgressBar) this.findViewById(R.id.progressBarRefreshActivity);
        this._progressBar.setVisibility(View.VISIBLE);
        this._accountName = "Unknown";

        // create a new intent which allows the user to select an account
        accountSelectionIntent = AccountManager.newChooseAccountIntent(null, null, new String[] {"com.google"}, false, null, null, null, null);
        this.startActivityForResult(accountSelectionIntent, INTENT_ACCOUNT_CHOOSER);
    }

    /**
     * Called when the api has updated all files.
     *
     * @param apiInstance   The manager that updated.
     * @param allFiles      The new list of files.
     */
    public void managerDidUpdateFiles(DSGoogleDriveAPI apiInstance, final com.google.api.services.drive.model.File[] allFiles) {

        // remove all subviews and current files from the table
        this._filesTableView.removeAllViews();
        this._progressBar.setVisibility(View.GONE);

        // create the margin for table rows for the layout
        TableLayout.LayoutParams layoutParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 5, 0, 5);

        // add a new row for each metadata object
        for (int i = 0; i < allFiles.length; i++) {

            // inflate a new view from the
            View newView = new DSFileDescriptionView(this, allFiles[i]);
            newView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // continue when the object is valid
                    if (view instanceof DSFileDescriptionView) {

                        // retrieve the metadata
                        File fileMetadata = ((DSFileDescriptionView) view).fileMetadata();
                        Intent editIntent = new Intent(DSLoginActivity.this, DSEditFileActivity.class);

                        // configure and show the edit intent
                        editIntent.putExtra(DSEditFileActivity.DRIVE_ID_EXTRA_IDENTIFIER, fileMetadata.getId());
                        editIntent.putExtra(DSEditFileActivity.FILE_NAME_EXTRA_IDENTIFIER, fileMetadata.getTitle());
                        editIntent.putExtra(DSEditFileActivity.FILE_DOWNLOAD_URL_IDENTIFIER, fileMetadata.getExportLinks().get("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
                        DSLoginActivity.this.startActivity(editIntent);
                    }
                }
            });

            // add the view with layout
            this._filesTableView.addView(newView, layoutParams);
        }
    }

    /**
     * Gets called when the API discovers a user recoverable error.
     *
     * @param apiInstance   The instance that received the intent.
     * @param intent        The intent that can be shown.
     */
    public void managerDidReceiveUserRecoverableIntent(DSGoogleDriveAPI apiInstance, Intent intent) {

        System.out.println("Starting intent...");

        // start the activity to resolve the authorization failure
        this.startActivityForResult(intent, ACTIVITY_AUTHORIZATION_INTENT);
    }

    /**
     * When a worksheet is downloaded respond to it.
     *
     * @param apiInstance   The API instance that executed the request.
     * @param worksheet     The downloaded worksheet.
     */
    public void managerDidDownloadWorksheet(DSGoogleDriveAPI apiInstance, DSExcelWorksheet worksheet) {

        // do nothing in this activity
    }

    /**
     * Delegate message for when a file has been saved.
     *
     * @param apiInstance   The instance that started the save.
     * @param fileTitle     The title of the file that failed to be saved.
     */
    public void managerDidFinishSavingContentsOfFileWithTitleWithResult(DSGoogleDriveAPI apiInstance, String fileTitle) {

        // show message that save succeeded
        AlertDialog alertdialog = new AlertDialog.Builder(this)
                .setTitle("Save finished")
                .setMessage("Successfully saved the contents of the file!")
                .create();

        // show the dialog
        alertdialog.show();
    }

    /**
     * When an operation has failed.
     *
     * @param apiInstance       The instance that created the error.
     * @param operationType     The type of operation that failed.
     * @param errorMessage      The error message describing the failure.
     */
    public void managerDidFailOperationWithTypeAndErrorDescription(DSGoogleDriveAPI apiInstance, DSGoogleDriveAPI.OperationType operationType, String errorMessage) {

        // the builder that will create the alert dialog
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // depending on the failed type respond and change the title of the alert dialog
        switch (operationType) {

            case OperationTypeCreateFile:
                alertDialogBuilder.setTitle("Failed to create file");
                break;

            case OperationTypeFetchFiles:
                alertDialogBuilder.setTitle("Failed to fetch files");
                this._progressBar.setVisibility(View.GONE);
                break;

            case OperationTypeSaveFile:

                // mark that the operation is not saving anymore
                alertDialogBuilder.setTitle("Failed to save file");
                break;
        }

        // set the error message and create the alert dialog
        AlertDialog alertDialog = alertDialogBuilder.setMessage(this._accountName + ", Error: " + errorMessage).create();
        alertDialog.show();
    }

    /**
     * Refresh the files when a new file is created.
     *
     * @param apiInstance   The instance that created the file.
     * @param fileName      The name of the file.
     */
    public void managerDidCreateFileWithName(DSGoogleDriveAPI apiInstance, String fileName) {

        // create the message
        AlertDialog alertdialog = new AlertDialog.Builder(this)
                .setTitle("File created")
                .setMessage(fileName + " has been created on your Google Drive.")
                .create();

        // show the alert dialog
        alertdialog.show();

        // refresh the files
        this._googleDriveAPI.fetchInitialFilesWithDelegate(this);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        if (requestCode == 2) {
            boolean mResolvingError = false;
            if (resultCode == RESULT_OK) {
                AlertDialog alertDialog = alertDialogBuilder.setMessage("zou ingelogd moeten zijn").create();
                alertDialog.show();
            }
        }


        if (requestCode == ACTIVITY_CREATE_FILE && resultCode == RESULT_OK) {

            // start the creation of the new file
            String fileName = data.getStringExtra(DSAddFileActivity.ACTIVITY_RESULT_FILENAME);
            this._googleDriveAPI.createFileWithNameUsingDelegate(fileName, this);
        } else if (requestCode == INTENT_ACCOUNT_CHOOSER && resultCode == RESULT_OK) {

            this._accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);

            // the account has been chosen successfully so connect to the API
            GoogleAccountCredential credentials = GoogleAccountCredential.usingOAuth2(this.getApplicationContext(), Collections.singleton(DriveScopes.DRIVE));
            credentials.setSelectedAccountName(data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME));

            // create the google drive api instance and start the initial fetch of required files
            this._googleDriveAPI = new DSGoogleDriveAPI(credentials, this.getString(R.string.app_name));
            this.startRefreshingfiles();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.dslogin, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // default return value
        boolean returnValue = false;

        // depending on the id of the button do appropriate action
        switch (item.getItemId()) {

            case R.id.action_settings:
                break;

            case R.id.action_sync:

                // start file refresh
                this.startRefreshingfiles();
                break;

            case R.id.action_add:

                // create a new intent for file adding activity
                Intent createFileIntent = new Intent(this, DSAddFileActivity.class);
                this.startActivityForResult(createFileIntent, ACTIVITY_CREATE_FILE);
                returnValue = true;
                break;

            default:

                // let it be handled by the super class
                returnValue =  super.onOptionsItemSelected(item);
        }

        return returnValue;
    }

    /**
     * starts the background fetch operation for retrieving all the files.
     */
    public void startRefreshingfiles() {

        // start synchronizing the files, reload the list
        this._googleDriveAPI.fetchInitialFilesWithDelegate(this);

        // clear the table view and start the progress bar
        this._filesTableView.removeAllViews();
        this._progressBar.setVisibility(View.VISIBLE);
    }
}
