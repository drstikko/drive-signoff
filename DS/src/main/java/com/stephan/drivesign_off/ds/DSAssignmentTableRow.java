package com.stephan.drivesign_off.ds;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.widget.TableRow;
import android.widget.TextView;

/**
 * This table row is used for describing a assignment for a student with its current value.
 *
 * @class   DSAssignmentTableRow
 */
public class DSAssignmentTableRow extends TableRow {

    /**
     * The assignment name text view.
     *
     * @var TextView
     */
    private TextView _assignmentTitleView;

    /**
     * The assignment value description text view.
     *
     * @var TextView
     */
    private TextView _assignmentValueDescriptionTextView;

    /**
     * The associated assignment for the row.
     *
     * @var String
     */
    private String _assignment;

    /**
     * The assignment value for the student.
     *
     * @var Integer
     */
    private String _assignmentValue;

    /**
     * Constructs a new assignment cell from the context and the assignment object.
     *
     * @param context   The android context from which to create.
     * @param assignment   The associated assignment name with the cell.
     */
    public DSAssignmentTableRow(Context context, String assignment, String currentValue) {

        // instantiate super with context
        super(context);
        this._assignment = assignment;
        this._assignmentValue = currentValue;

        // set background color
        this.setBackgroundColor(Color.WHITE);

        // load the layout and add the view
        LayoutInflater inflater = LayoutInflater.from(context);
        this.addView(inflater.inflate(R.layout.view_tablerow, null, false));

        // configure the text views for the student
        this._assignmentTitleView = (TextView) this.findViewById(R.id.view_tablerow_title);
        this._assignmentValueDescriptionTextView = (TextView) this.findViewById(R.id.view_tablerow_subtitle);
        this._assignmentTitleView.setText(this._assignment);

        // update the value description text
        this.updateAssignmentValueText();
    }

    /**
     * Updates the text in the value description.
     */
    public void updateAssignmentValueText() {

        // when the string equals to "0.0" string or has 0 characters set not completed
        if (this._assignmentValue.equals("0.0") || this._assignmentValue.length() == 0) {
            this._assignmentValueDescriptionTextView.setText("Not completed yet");
        } else {
            this._assignmentValueDescriptionTextView.setText(this._assignmentValue);
        }
    }

    /**
     * Returns the student associated with the cell.
     *
     * @return  String
     */
    public String assignmentName() {

        return this._assignment;
    }

    public TextView get_assignmentTitleView() {

        return this._assignmentValueDescriptionTextView;
    }
}
