package com.stephan.drivesign_off.ds;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a parsed Excel document which contains all rows and cells of the document with values.
 *
 * @class   DSExcelWorksheet
 */
public class DSExcelWorksheet {

    /**
     * A list of all the worksheet rows in the receiver.
     *
     * @var List<DSExcelWorksheetRow>
     */
    private List<DSExcelWorksheetRow> _worksheetRows;

    /**
     * Default constructor override.
     */
    public DSExcelWorksheet() {

        // create the empty list of rows
        this._worksheetRows = new ArrayList<DSExcelWorksheetRow>();
    }

    /**
     * Returns all current worksheet rows contained in the receiver.
     *
     * @return  DSExcelWorksheetRow[]
     */
    public DSExcelWorksheetRow[] worksheetRows() {

        // return a copied array
        return this._worksheetRows.toArray(new DSExcelWorksheetRow[this._worksheetRows.size()]);
    }

    /**
     * Adds a new row to the receiver.
     *
     * @param worksheetRow  The row to add.
     */
    public void addWorksheetRow(DSExcelWorksheetRow worksheetRow) {

        this._worksheetRows.add(worksheetRow);
    }

    /**
     * Returns the iterator for all rows of the receiver.
     *
     * @return  Iterator<DSExcelWorksheetRow>
     */
    public Iterator<DSExcelWorksheetRow> rowIterator() {

        // return the row iterator
        return this._worksheetRows.iterator();
    }

    /**
     * Merges the receiver with all strings contained in the shared strings instance.
     *
     * @param sharedStrings The shared strings object.
     */
    public void mergeWorksheetWithSharedStrings(DSExcelWorksheetSharedStrings sharedStrings) {

        // get the iterator for all worksheet rows
        Iterator<DSExcelWorksheetRow> worksheetRowIterator = this._worksheetRows.iterator();
        DSExcelWorksheetRow currentWorksheetRow;
        DSExcelWorksheetCell currentWorksheetCell;
        int currentIndex = 0;

        // process rows
        //comment  John visser
        while (worksheetRowIterator.hasNext()) {

            // get the row to be processed
            currentWorksheetRow = worksheetRowIterator.next();
            Iterator<DSExcelWorksheetCell> worksheetCellIterator = currentWorksheetRow.cellIterator();

            // process the cells of the current row
            while (worksheetCellIterator.hasNext()) {

                // get the next cell to process
                currentWorksheetCell = worksheetCellIterator.next();

                // when the cell has "s"  as the value type the shared string should be looked up
                if (currentWorksheetCell.shouldLookupSharedString()) {
                    currentWorksheetCell.setCellValue(sharedStrings.sharedStringAtIndex(currentIndex));
                    currentIndex++;
                }
            }
        }
    }
}
