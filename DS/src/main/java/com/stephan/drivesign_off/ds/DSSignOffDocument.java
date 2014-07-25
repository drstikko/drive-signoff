package com.stephan.drivesign_off.ds;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * The finished parsed document which contains students and assignments for the current document.
 *
 * @class   DSSignOffDocument
 */
public class DSSignOffDocument {

    /**
     * The list of students.
     *
     * @var List<DSStudent>
     */
    private List<DSStudent> _students;

    /**
     * The list of assignments for this practice.
     *
     * @var List<String>
     */
    private List<String> _assignments;

    /**
     * Constructs a new document from the provided students.
     *
     * @param students  All students to add to the document.
     */
    public DSSignOffDocument(DSStudent[] students, String[] assignments) {

        // create the list of students from the Java array
        this._students = new ArrayList<DSStudent>(Arrays.asList(students));
        this._assignments = new ArrayList<String>(Arrays.asList(assignments));
    }

    /**
     * Returns the iterator for all current students.
     *
     * @return  Iterator<DSStudent>
     */
    public Iterator<DSStudent> studentIterator() {

        return this._students.iterator();
    }

    /**
     * Returns the iterator for all assignments.
     *
     * @return  String[]
     */
    public String[] allAssignments() {

        // return a new string array from the assignments
        return this._assignments.toArray(new String[this._assignments.size()]);
    }

    /**
     * Returns an initialized document from the provided excel worksheet data.
     *
     * @param excelWorksheet    The worksheet to be converted to the document.
     * @return                  DSSignOffDocument
     */
    static DSSignOffDocument documentFromExcelWorksheet(DSExcelWorksheet excelWorksheet) {
        int Cols = 0;
        // get the row iterator from the worksheet
        Iterator<DSExcelWorksheetRow> rowIterator = excelWorksheet.rowIterator();
        ArrayList<DSStudent> allStudents = new ArrayList<DSStudent>();
        ArrayList<String> allAssignments = new ArrayList<String>();

        // the first row should contain the titles of the assignements and the id and name of the student
        DSExcelWorksheetRow currentWorkSheetRow = (rowIterator.hasNext()) ? rowIterator.next() : null;

        // validate the first row
        if (currentWorkSheetRow != null) {

            // get the cell iterator which fetches all titles
            Iterator<DSExcelWorksheetCell> firstRowCellsIterator = currentWorkSheetRow.cellIterator();
            DSExcelWorksheetCell currentCell;

            // process the cells of the first row
            while (firstRowCellsIterator.hasNext()) {

                // process the next assignment
                currentCell = firstRowCellsIterator.next();

                // continue on the student id and name column identifier
                if (!currentCell.cellValue().equals("Studentnummer") && !currentCell.cellValue().equals("Naam")) {

                    // add the assignment type
                    allAssignments.add(currentCell.cellValue());
                }
                Cols++;
            }
        }

        // now continue processing each row which represents a student
        while (rowIterator.hasNext()) {

            // get the next row and retrieve the cell iterator
            currentWorkSheetRow = rowIterator.next();
            Iterator<DSExcelWorksheetCell> cellIterator = currentWorkSheetRow.cellIterator();
            int currentCellIndex = 0;
            DSStudent currentStudent = new DSStudent();
            DSExcelWorksheetCell currentCell;

            int tmpCols = Cols;
            while(tmpCols>2) {
                currentStudent.setSignedOffValueForType("0.0", allAssignments.get(tmpCols - 1 - 2));
                tmpCols--;
            }

            while (cellIterator.hasNext()) {

                // set the current cell
                currentCell = cellIterator.next();
                currentCellIndex = currentCell.cellIdentifier().charAt(0)-64-1; //fix for misplacing values
                switch (currentCellIndex) {

                    // the student id case
                    case 0:
                        currentStudent.setStudentId(currentCell.cellValue());
                        break;

                    // the student name case
                    case 1:
                        currentStudent.setStudentName(currentCell.cellValue());
                        break;

                    default:

                        // set the value for the assignment for the associated assignment
                        currentStudent.setSignedOffValueForType(currentCell.cellValue(), allAssignments.get(currentCellIndex - 2));
                }

                // increment counter
                //currentCellIndex++;
            }

            // add the parsed student to the list
            allStudents.add(currentStudent);
        }

        // return the concrete sign off document
        return new DSSignOffDocument(allStudents.toArray(new DSStudent[allStudents.size()]), allAssignments.toArray(new String[allAssignments.size()]));
    }

    /**
     * Adds a new student to the receiver.
     *
     * @param newStudent    The new student object.
     */
    public void addStudent(DSStudent newStudent) {

        this._students.add(newStudent);
    }

    /**
     * Adds a new assignment to the receiver.
     *
     * @param assignmentName    The assignment name.
     */
    public void addAssignmentWithName(String assignmentName) {

        this._assignments.add(assignmentName);
    }
}
