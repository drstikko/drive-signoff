package com.stephan.drivesign_off.ds;

import android.app.Activity;
import android.app.AlertDialog;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;


public class DSAddStudentActivity extends Activity {

    static String ACTIVITY_RESULT_STUDENT_NAME  =   "StudentName";
    static String ACTIVITY_RESULT_STUDENT_ID    =   "StudentIdentifier";

    /**
     * the edit text field for the student id.
     *
     * @var EditText
     */
    private EditText _studentId;

    /**
     * The edit text field for the student name.
     *
     * @var EditText
     */
    private EditText _studentName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dsadd_student);

        // set the custom title
        this.setTitle("Add student");

        // fetch ui objects from the view
        this._studentId = (EditText) this.findViewById(R.id.editTextStudentId);
        this._studentName = (EditText) this.findViewById(R.id.editTextStudentName);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.dsadd_student, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Gets called when the user presses the add student button.
     *
     * @param view  The view that was tapped.
     */
    public void onAddStudentButtonClicked(View view) {

        // retrieve the student name
        String studentName = this._studentName.getText().toString();
        String studentId = this._studentId.getText().toString();

        // make sure the student id and name are valid
        if (this.isValidStudentId(studentId) && studentName.length() > 5 && studentName.length() <= 75) {

            // put the resulting extra content
            this.getIntent().putExtra(ACTIVITY_RESULT_STUDENT_ID, studentId);
            this.getIntent().putExtra(ACTIVITY_RESULT_STUDENT_NAME, studentName);

            // go to parent activity
            this.setResult(RESULT_OK, this.getIntent());
            this.finish();
        } else {

            // create a message explaining the error
            AlertDialog alertMessage = new AlertDialog.Builder(this)
                    .setMessage("The student id or name is not valid.")
                    .setTitle("Invalid data").create();

            // show the message
            alertMessage.show();
        }
    }

    /**
     * Validates whether the student id is valid.
     *
     * @param studentId The string to validate.
     * @return          True when valid otherwise false.
     */
    private boolean isValidStudentId(String studentId) {

        // set the default return value
        boolean returnValue = true;

        // the length must be exactly 8 characters
        if (studentId.length() == 8) {

            for (int i = 0; i < 8; i++) {

                int currentCodePoint = studentId.codePointAt(i);

                if (currentCodePoint < 0 || currentCodePoint > 10) {
                    break;
                }
            }
        } else {
            returnValue = false;
        }

        return returnValue;
    }
}
