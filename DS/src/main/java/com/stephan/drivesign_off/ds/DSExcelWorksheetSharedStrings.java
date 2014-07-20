package com.stephan.drivesign_off.ds;

import java.util.ArrayList;
import java.util.List;

/**
 * Object which contains the parsed shared strings values of an Excel document.
 *
 * @class   DSExcelWorksheetsharedStrings
 */
public class DSExcelWorksheetSharedStrings {

    /**
     * The list of actual shared strings in the document.
     *
     * @var List<String>
     */
    private List<String> _sharedStrings;

    /**
     * Default constructor for the shared strings.
     */
    public DSExcelWorksheetSharedStrings() {

        // create the list of strings
        this._sharedStrings = new ArrayList<String>();
    }

    /**
     * Returns the shared string used for the index provided.
     *
     * @param index The index of the string to look up.
     * @return      the shared string for the index.
     */
    public String sharedStringAtIndex(int index) {

        // return the shared string for provided index
        return this._sharedStrings.get(index);
    }

    /**
     * Adds a new shared string to the receiver.
     *
     * @param newString The new shared string.
     */
    public void addSharedString(String newString) {

        // add the new shared string
        this._sharedStrings.add(newString);
    }
}
