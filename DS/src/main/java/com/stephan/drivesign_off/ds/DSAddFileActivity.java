package com.stephan.drivesign_off.ds;

import android.app.Activity;
import android.app.AlertDialog;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

/**
 * Activity which can be used for creating a new file in the user's Google Drive.
 *
 * @class   DSAddFileActivity
 */
public class DSAddFileActivity extends Activity {

    // file name of the new file to be created by the activity parent
    static String ACTIVITY_RESULT_FILENAME  =   "FileName";

    /**
     * The text view for the file name.
     *
     * @var TextView
     */
    private TextView _fileNameTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dsadd_file);

        // set the text view
        this._fileNameTextView = (TextView) this.findViewById(R.id.editFileName);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.dsadd_file, menu);
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
     * Gets called when the user clicks the confirm button on the view.
     *
     * @param view  The corresponding view.
     */
    public void onConfirmFileButtonClicked(View view) {

        // verify that the text is between 5 and 50 characters
        CharSequence fileName = this._fileNameTextView.getText();
        if (fileName.length() >= 5 && fileName.length() <= 50) {

            // put the resulting extra content
            this.getIntent().putExtra(ACTIVITY_RESULT_FILENAME, fileName.toString());

            // go to parent activity
            this.setResult(RESULT_OK, this.getIntent());
            this.finish();
        } else {

            // create a message explaining the error
            AlertDialog alertMessage = new AlertDialog.Builder(this)
                    .setMessage("Use at least 5 characters and at most 50 characters for a file name.")
                    .setTitle("Invalid filename").create();

            // show the message
            alertMessage.show();
        }
    }
}
