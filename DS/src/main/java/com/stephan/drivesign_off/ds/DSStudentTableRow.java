package com.stephan.drivesign_off.ds;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.widget.TableRow;
import android.widget.TextView;

/**
 * A specpfic table row used for showing the information about a student.
 *
 * @class   DSStudentTableRow
 */
public class DSStudentTableRow extends TableRow {

    /**
     * The student name text view.
     *
     * @var TextView
     */
    private TextView _studentTextView;

    /**
     * The student id text view.
     *
     * @var TextView
     */
    private TextView _studentIdTextView;

    /**
     * The associated student for the row.
     *
     * @var DSStudent
     */
    private DSStudent _student;

    /**
     * Constructs a new student cell from the context and the student object.
     *
     * @param context   The android context from which to create.
     * @param student   The associated student with the cell.
     */
    public DSStudentTableRow(Context context, DSStudent student) {

        // instantiate super with context
        super(context);
        this._student = student;

        // set background color
        this.setBackgroundColor(Color.LTGRAY);

        // load the layout and add the view
        LayoutInflater inflater = LayoutInflater.from(context);
        this.addView(inflater.inflate(R.layout.view_tablerow, null, false));

        // configure the text views for the student
        this._studentTextView = (TextView) this.findViewById(R.id.view_tablerow_title);
        this._studentIdTextView = (TextView) this.findViewById(R.id.view_tablerow_subtitle);
        this._studentTextView.setText(this.student().studentName());
        this._studentIdTextView.setText(this.student().studentIdentifier());
    }

    /**
     * Returns the student associated with the cell.
     *
     * @return  DSStudent
     */
    public DSStudent student() {

        return this._student;
    }
}
