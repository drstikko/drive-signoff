package com.stephan.drivesign_off.ds;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import com.google.api.services.drive.model.File;
import java.util.Iterator;


public class DSEditFileActivity extends Activity implements DSGoogleDriveAPIDelegate {

    /**
     * The identifier in the extras of the receiver which returns the DriveID.
     *
     * @var String
     */
    static String DRIVE_ID_EXTRA_IDENTIFIER   =   "DriveIdIdentifier";

    /**
     * The title of the file key.
     *
     * @var String
     */
    static String FILE_NAME_EXTRA_IDENTIFIER    =   "FileNameIdentifier";
    static String FILE_DOWNLOAD_URL_IDENTIFIER  =   "FileDownloadURL";

    /**
     * Identifier for the add student intent.
     *
     * @var int
     */
    static int ACTIVITY_ADD_STUDENT_REQUEST_CODE    =   5;
    static int ACTIVITY_ADD_ASSIGNMENT_REQUEST_CODE =   6;
    static int ACTIVITY_EDIT_STUDENT_REQUEST_CODE   =   7;

    /**
     * The activity indicator which shows file download process.
     *
     * @var ProgressBar
     */
    private ProgressBar _activityIndicator;

    private TableLayout _studentLayout;

    /**
     * The document containing the parsed student info.
     *
     * @var DSSignOffDocument
     */
    private DSSignOffDocument _studentsDocument;

    /**
     * The current student that is being edited.
     *
     * @var DSStudent | null
     */
    private DSStudent _currentStudentBeingEdited;

    /**
     * The file identifier of the drive file.
     *
     * @var String
     */
    private String _fileIdentifier;

    /**
     * Whether the data is currently being saved.
     *
     * @var boolean
     */
    private boolean _isSavingInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dsedit_file);

        // fetch the student table view
        this._studentLayout = (TableLayout) this.findViewById(R.id.student_table_layout);
        this._isSavingInfo = false;

        String activityTitle = this.getIntent().getStringExtra(FILE_NAME_EXTRA_IDENTIFIER);
        String downloadUrl = this.getIntent().getStringExtra(FILE_DOWNLOAD_URL_IDENTIFIER);
        this._fileIdentifier = this.getIntent().getStringExtra(DRIVE_ID_EXTRA_IDENTIFIER);
        this.setTitle(activityTitle);

        // make sure the download URL for the file is available and not empty
        if (downloadUrl != null && downloadUrl.length() > 0) {
            DSGoogleDriveAPI.defaultManager().readFileWithIdUsingDelegate(downloadUrl, this);
        }

        // set the private progress bar
        this._activityIndicator = (ProgressBar) this.findViewById(R.id.progressBar);
        this._activityIndicator.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.dsedit_file, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_add_student) {

            // a new student should be added to the model, create the intent and start the activity
            Intent addStudentIntent = new Intent(this, DSAddStudentActivity.class);
            this.startActivityForResult(addStudentIntent, ACTIVITY_ADD_STUDENT_REQUEST_CODE);
            return true;
        } else if (id == R.id.action_add_assignment) {

            // create the intent for adding a new assignment
            Intent addAssignmentIntent = new Intent(this, DSAddAssignmentActivity.class);
            this.startActivityForResult(addAssignmentIntent, ACTIVITY_ADD_ASSIGNMENT_REQUEST_CODE);
            return true;
        } else if ((id == R.id.action_save || id == android.R.id.home) && !this._isSavingInfo && this._studentsDocument != null) {

            // when the save button is directly pushed make this the delegate, otherwise initially null
            DSGoogleDriveAPIDelegate delegate = (id == R.id.action_save) ? this : null;

            // perform save of the document
            DSGoogleDriveAPI.defaultManager().saveFileWithIdentifierAndDocumentUsingContext(this._fileIdentifier, this._studentsDocument, this.getApplicationContext(), delegate);
            this._isSavingInfo = true;
        }

        // process by the super class
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        // only save when the document is available
        if (this._studentsDocument != null && !this._isSavingInfo) {

            // perform save of the document
            DSGoogleDriveAPI.defaultManager().saveFileWithIdentifierAndDocumentUsingContext(this._fileIdentifier, this._studentsDocument, this.getApplicationContext(), null);
            this._isSavingInfo = true;
        }

        // now finish the activity
        super.onBackPressed();
    }

    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {

        if (requestCode == ACTIVITY_ADD_STUDENT_REQUEST_CODE && resultCode == RESULT_OK) {

            // retrieve the string values for creating the new student
            String studentName = data.getStringExtra(DSAddStudentActivity.ACTIVITY_RESULT_STUDENT_NAME);
            String studentId = data.getStringExtra(DSAddStudentActivity.ACTIVITY_RESULT_STUDENT_ID);

            // check the values and create and add the student top the document
            if (studentName != null && studentId != null) {

                // create and configure the student
                DSStudent newStudent = new DSStudent();
                newStudent.setStudentId("#" + studentId);
                newStudent.setStudentName(studentName);

                // set default values for the assignments
                for (String assignmentName : this._studentsDocument.allAssignments()) {
                    newStudent.setSignedOffValueForType("", assignmentName);
                }

                // add the student to the document and refresh the contents of the page
                this._studentsDocument.addStudent(newStudent);
                this.refreshStudentTableLayout();
            }
        } else if (requestCode == ACTIVITY_ADD_ASSIGNMENT_REQUEST_CODE && resultCode == RESULT_OK) {

            // retrieve the name of the new assignment
            String assignmentName = data.getStringExtra(DSAddAssignmentActivity.ACTIVITY_RESULT_ASSIGNMENT_NAME);

            // check the value and add the name
            if (assignmentName != null) {
                this._studentsDocument.addAssignmentWithName(assignmentName);

                // set a default value for all assignments and all students
                Iterator<DSStudent> studentIterator = this._studentsDocument.studentIterator();

                while (studentIterator.hasNext()) {
                    studentIterator.next().setSignedOffValueForType("", assignmentName);
                }
            }
        } else if (requestCode == ACTIVITY_EDIT_STUDENT_REQUEST_CODE && resultCode == RESULT_OK) {

            // a bundle is included which includes all new values for the current student
            Bundle resultBundle = data.getBundleExtra(DSEditStudentAssignmentsActivity.RESULT_EXTRA_CONTENT_ASSIGNMENTS);

            if (resultBundle != null && this._currentStudentBeingEdited != null) {

                // remove the student name from the bundle just to be sure it is not contained in the result
                resultBundle.remove(DSEditStudentAssignmentsActivity.ASSIGNMENT_BUNDLE_STUDENT_NAME);

                // process each value to the current student
                for (String currentAssignment : resultBundle.keySet()) {
                    this._currentStudentBeingEdited.setSignedOffValueForType(resultBundle.getString(currentAssignment), currentAssignment);
                }
            }
        }

        // make sure the student is set to null
        this._currentStudentBeingEdited = null;
    }

    /**
     * Refreshes all contents in the students table layout.
     */
    private void refreshStudentTableLayout() {

        // clear the table layout of its views
        this._studentLayout.removeAllViews();

        // fetch all students from the created document
        Iterator<DSStudent> studentIterator = this._studentsDocument.studentIterator();

        // create the margin for table rows for the layout
        TableLayout.LayoutParams layoutParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 5, 0, 5);

        // process each student
        while (studentIterator.hasNext()) {

            // fetch the current student
            final DSStudent currentStudent = studentIterator.next();

            // create the table row for the next student and add it to the table layout
            final DSStudentTableRow currentRow = new DSStudentTableRow(this, currentStudent);
            currentRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // when the table row is tapped
                    if (view instanceof DSStudentTableRow) {

                        // create the next intent which creates the edit student activity, also remember the current student being edited
                        Intent editAssignmentsIntent = new Intent(DSEditFileActivity.this, DSEditStudentAssignmentsActivity.class);
                        DSEditFileActivity.this._currentStudentBeingEdited = currentStudent;

                        // configure with the associated student and
                        Bundle configurationBundle = currentStudent.studentAssignmentInfoBundle();
                        configurationBundle.putString(DSEditStudentAssignmentsActivity.ASSIGNMENT_BUNDLE_STUDENT_NAME, currentStudent.studentName());
                        editAssignmentsIntent.putExtra(DSEditStudentAssignmentsActivity.ACTIVITY_EXTRA_CONTENT_ASSIGNMENTS, configurationBundle);

                        // start the next intent with associated request code
                        startActivityForResult(editAssignmentsIntent, ACTIVITY_EDIT_STUDENT_REQUEST_CODE);
                    }
                }
            });

            // add the view with layout
            this._studentLayout.addView(currentRow, layoutParams);
        }
    }


    public void managerDidUpdateFiles(DSGoogleDriveAPI apiInstance, File[] allFiles) {

    }

    /**
     * Called when the file has been saved.
     *
     * @param apiInstance   The instance that started the save.
     * @param fileTitle     The title of the file that failed to be saved.
     */
    public void managerDidFinishSavingContentsOfFileWithTitleWithResult(DSGoogleDriveAPI apiInstance, String fileTitle) {

        // show message that save succeeded
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Save finished")
                .setMessage("Successfully saved the contents of the file!")
                .create();

        // show the dialog
        if (!this.isFinishing()) {
            alertDialog.show();
        }

        // mark save has succeeded
        this._isSavingInfo = false;
    }

    /**
     * Gets called when a user recoverable intent should be started.
     *
     * @param apiInstance   The instance that encountered the exception.
     * @param intent        The intent that can recover the exception.
     */
    public void managerDidReceiveUserRecoverableIntent(DSGoogleDriveAPI apiInstance, Intent intent) {

        // show the recoverable intent
        this.startActivity(intent);
    }

    /**
     * Gets called when an error has occured.
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

            case OperationTypeReadFile:
                alertDialogBuilder.setTitle("Failed to read file");
                break;

            case OperationTypeSaveFile:

                // mark that the operation is not saving anymore
                alertDialogBuilder.setTitle("Failed to save file");
                this._isSavingInfo = false;
                break;

            default:
                alertDialogBuilder.setTitle("Unknown error");
                break;
        }

        // set the error message and create the alert dialog
        AlertDialog alertDialog = alertDialogBuilder.setMessage(errorMessage).create();
        if (!this.isFinishing()) {
            alertDialog.show();
        }
    }

    /**
     * Do nothing when a new file has been created.
     *
     * @param apiInstance   The instance that created the file.
     * @param fileName      The name of the file.
     */
    public void managerDidCreateFileWithName(DSGoogleDriveAPI apiInstance, String fileName) {

    }



    /**
     * When the worksheet is finished downloading parse it to show a list of students.
     *
     * @param apiInstance   The API instance that finished the download.
     * @param worksheet     The resulting worksheet
     */
    public void managerDidDownloadWorksheet(DSGoogleDriveAPI apiInstance, DSExcelWorksheet worksheet) {

        // hide the activity indicator
        this._activityIndicator.setVisibility(View.GONE);
        this._studentsDocument = DSSignOffDocument.documentFromExcelWorksheet(worksheet);

        // refresh the table layout
        this.refreshStudentTableLayout();
    }
}
