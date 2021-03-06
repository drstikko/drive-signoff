package com.stephan.drivesign_off.ds;

import android.app.Activity;
import android.app.AlertDialog;
//import android.support.v7.app.ActionBarActivity;
import android.app.ActionBar;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;


/**
 * Activity which can be used for adding a new assignment to students.
 *
 * @class   DSAddAssignmentActivity
 */
public class DSAddAssignmentActivity extends Activity {

    /**
     * The resulting name of the assignment tht should be created.
     *
     * @var ACTIVITY_RESULT_ASSIGNMENT_NAME
     */
    static String ACTIVITY_RESULT_ASSIGNMENT_NAME   =   "AssignmentName";

    /**
     * The text edit view for the assignment name.
     *
     * @var EditText
     */
    private EditText _assignmentNameEditText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dsadd_assignment);

        // match the assignment name text edit view
        this._assignmentNameEditText = (EditText) this.findViewById(R.id.add_assignment_name_id);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.dsadd_assignment, menu);
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
     *  Gets called after the create button was pressed.
     *
     * @param view  The corresponding calling view.
     */
    public void onAddAssignmentConfirmClicked(View view) {

        // convert the assignment name to a string
        String newAssignmentName = this._assignmentNameEditText.getText().toString();

        if (newAssignmentName.length() >= 3 && newAssignmentName.length() <= 25) {

            // add the extra content result
            this.getIntent().putExtra(ACTIVITY_RESULT_ASSIGNMENT_NAME, newAssignmentName);
            this.setResult(RESULT_OK, this.getIntent());
            this.finish();

        } else {

            // construct the error dialog
            AlertDialog errorDialog = new AlertDialog.Builder(this)
                    .setTitle("Error creating assignment")
                    .setMessage("The assignment name must be between 3 & 25 characters.")
                    .create();

            // show the alert
            errorDialog.show();
        }
    }
}
