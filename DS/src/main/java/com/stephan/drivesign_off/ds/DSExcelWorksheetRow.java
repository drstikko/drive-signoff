package com.stephan.drivesign_off.ds;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A single row of an Excel document parsed in the object format.
 *
 * @class   DSExcelWorksheetRow
 */
public class DSExcelWorksheetRow {

    /**
     * The id of the row.
     *
     * @var String
     */
    private int _rowIndex;

    /**
     * All contained cells in the row.
     *
     * @var List<DSExcelWorksheetCell>
     */
    private List<DSExcelWorksheetCell> _cells;

    /**
     * Creates a new worksheet row for the given index in the worksheet.
     *
     * @param rowIndex  The corresponding index of the worksheet row.
     */
    public DSExcelWorksheetRow(int rowIndex) {

        // set the index
        this._rowIndex = rowIndex;
        this._cells = new ArrayList<DSExcelWorksheetCell>();
    }

    /**
     * Returns the corresponding row index of the receiver.
     *
     * @return  int
     */
    public int rowIndex() {

        return this._rowIndex;
    }

    /**
     * Returns the cell iterator of the receiver.
     *
     * @return  Iterator<DSExcelWorksheetCell>
     */
    public Iterator<DSExcelWorksheetCell> cellIterator() {

        // return the corresponding iterator
        return this._cells.iterator();
    }

    /**
     * Adds a new worksheet cell to the receivers content.
     *
     * @param worksheetCell The new worksheet cell.
     */
    public void addWorksheetCell(DSExcelWorksheetCell worksheetCell) {

        // add the cell
        this._cells.add(worksheetCell);
    }
}
