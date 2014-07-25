package com.stephan.drivesign_off.ds;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Activity which can be used for editing the assignment values of a specified student.
 *
 * @class   DSEditStudentAssignmentActivity
 */
public class DSEditStudentAssignmentsActivity extends Activity {

    /**
     * The assignments string array extra identifier.
     *
     * @var String
     */
    static String ACTIVITY_EXTRA_CONTENT_ASSIGNMENTS    =   "AllAssignments";
    static String ASSIGNMENT_BUNDLE_STUDENT_NAME        =   "StudentNameKey";

    /**
     * The result key for the bundle of the edited assignment info for the student.
     *
     * @var String
     */
    static String RESULT_EXTRA_CONTENT_ASSIGNMENTS      =   "EditedAssignmentsBundleKey";

    /**
     * The student being edited in the activity.
     *
     * @var DSStudent
     */
    private Bundle _assignmentBundle;

    /**
     * The table layout that shows the rows.
     *
     * @var TableLayout
     */
    private TableLayout _assignmentsTableLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dsedit_student_assignments);

        // fetch and set the private student object, set the title of the activity to the name of the student
        this._assignmentBundle = this.getIntent().getBundleExtra(ACTIVITY_EXTRA_CONTENT_ASSIGNMENTS);

        // set the student name from the bundle info when existing
        if (this._assignmentBundle != null && this._assignmentBundle.containsKey(ASSIGNMENT_BUNDLE_STUDENT_NAME)) {
            this.setTitle(this._assignmentBundle.getString(ASSIGNMENT_BUNDLE_STUDENT_NAME));
        }

        // get the table layout to be edited
        this._assignmentsTableLayout = (TableLayout) this.findViewById(R.id.assignment_table_layout);
        this.refreshAssignmentsTableLayout();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.dsedit_student_assignments, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_save || id == android.R.id.home) {

            // notify the creator of the edit assignments activity that the contents will be saved
            this.getIntent().putExtra(RESULT_EXTRA_CONTENT_ASSIGNMENTS, this._assignmentBundle);
            this.setResult(RESULT_OK, this.getIntent());
            this.finish();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        // notify the creator of the edit assignments activity that the contents will be saved
        this.getIntent().putExtra(RESULT_EXTRA_CONTENT_ASSIGNMENTS, this._assignmentBundle);
        this.setResult(RESULT_OK, this.getIntent());
        this.finish();
    }

    public static Date getDateFromCOMDate(float comtime) {
        String floatstr = String.valueOf(comtime);
        String [] ss = floatstr.split("\\.");
      //  long nulltime = -2209183200000L;
        long nulltime = -2209161600000L;
        long dayms = 86400000;
        int days = Integer.valueOf(ss[0]);
        float prop = comtime - days;
        long cdayms = Math.round(dayms * prop);
        long time = nulltime + days*dayms + cdayms;
        Date d = new Date(time);
        return d;
    }
    /**
     * Refreshes all assignments views in the table layout.
     */
    private void refreshAssignmentsTableLayout() {

        // clear the table layout of its views
        this._assignmentsTableLayout.removeAllViews();

        // create the margin for table rows for the layout
        TableLayout.LayoutParams layoutParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 5, 0, 5);

        //final DSAssignmentTableRow currentRow = new DSAssignmentTableRow(this, "OpdrachtNaam");
        TableRow header = new TableRow(this);
        header.setBackgroundColor(Color.GREEN);
        TextView tmpView = new TextView(this);
        tmpView.setText("Opdrachtnaam");
        header.addView(tmpView);
        boolean noWeekHeader = false;
        this._assignmentsTableLayout.addView(header);

        // process each student
        for (final String currentAssignment : this._assignmentBundle.keySet()) {

            // continue for the student name
            if (currentAssignment.equals(ASSIGNMENT_BUNDLE_STUDENT_NAME)) {
                continue;
            }



            // create the table row for the next student and add it to the table layout
            String date_ass = "";

            if(this._assignmentBundle.getString(currentAssignment).equals("0.0")) {
                date_ass = "0.0";
            }else if(this._assignmentBundle.getString(currentAssignment).equals("1.0")){
                date_ass = this._assignmentBundle.getString(currentAssignment);
            }else {
                if (this._assignmentBundle.getString(currentAssignment).contains("-")) {
                    date_ass = this._assignmentBundle.getString(currentAssignment);
                } else {
                    try{
                        date_ass = new SimpleDateFormat("yyyy-MM-dd").format(getDateFromCOMDate(Float.valueOf(this._assignmentBundle.getString(currentAssignment))));
                        this._assignmentBundle.putString(currentAssignment, date_ass);
                    }
                    catch (NumberFormatException e){
                        date_ass = this._assignmentBundle.getString(currentAssignment);
                    }

                }
            }

            if (currentAssignment.startsWith("Week") && !noWeekHeader){
                header = new TableRow(this);
                header.setBackgroundColor(Color.YELLOW);
                tmpView = new TextView(this);
                tmpView.setText("Aanwezigheid");
                header.addView(tmpView);
                noWeekHeader = true;
                this._assignmentsTableLayout.addView(header);
            }

            final DSAssignmentTableRow currentRow = new DSAssignmentTableRow(this, currentAssignment, date_ass);
            final String assignmentName = currentAssignment;

            if(currentAssignment.startsWith("Week")){
                //currentRow.get_assignmentTitleView().setVisibility(View.INVISIBLE);
            }
            
            //add checkbox to table row layout
            CheckBox chk=new CheckBox(this);
           // chk.setText("Check for finished");

            if(!this._assignmentBundle.getString(currentAssignment).equals("0.0")){
                chk.setChecked(true);
            }

            currentRow.addView(chk);

            chk.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {

                    // create text input for the alert
                    final EditText textInputField = new EditText(DSEditStudentAssignmentsActivity.this);
                    textInputField.setInputType(InputType.TYPE_CLASS_TEXT);

                    String date = null;

                    if(currentAssignment.startsWith("Week")) {
                        date = "1.0";
                    }
                    else{
                        date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                    }


                    if(!_assignmentBundle.getString(assignmentName).equals("0.0")){
                        date = "0.0";
                    }

                    // overwrite the new value for the assignment selected

                    DSEditStudentAssignmentsActivity.this._assignmentBundle.putString(assignmentName, date);
                    DSEditStudentAssignmentsActivity.this.refreshAssignmentsTableLayout();
                    /*
                    // build a dialog in which the user can set the value of th assignment
                    AlertDialog alertDialog = new AlertDialog.Builder(DSEditStudentAssignmentsActivity.this)
                            .setTitle("Edit the assignment")
                            .setMessage("Enter a value for the assignment:")
                            .setView(textInputField)
                            .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    // overwrite the new value for the assignment selected
                                    DSEditStudentAssignmentsActivity.this._assignmentBundle.putString(assignmentName, textInputField.getText().toString());
                                    DSEditStudentAssignmentsActivity.this.refreshAssignmentsTableLayout();
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    // do nothing, automatically hides the view
                                }
                            })
                            .create();

                    // show the alert
                    alertDialog.show();*/
                }
            });

            // add the view with layout
            this._assignmentsTableLayout.addView(currentRow, layoutParams);
        }
    }
}
