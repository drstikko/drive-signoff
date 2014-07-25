package com.stephan.drivesign_off.ds;

/**
 * A cell of an Excel document which is parsed.
 *
 * @class   DSExcelWorksheetCell
 */
public class DSExcelWorksheetCell {

    /**
     * The identifier of the cell, i.e. A1 or C5
     *
     * @var _cellIdentifier
     */
    private String _cellIdentifier;

    /**
     * The type of value being stored in the cell.
     *
     * @var _valueType
     */
    private String _valueType;

    /**
     * The actual value of the cell.
     *
     * @var _cellValue
     */
    private String _cellValue;

    /**
     * Constructs a new cell from the identifier and value.
     *
     * @param cellIdentifier    The identifier of the cell (A1, B5 etc.)
     * @param valueType         The value type of the cell.
     */
    public DSExcelWorksheetCell(String cellIdentifier, String valueType) {

        // set private variables
        this._cellIdentifier = cellIdentifier;
        this._valueType = valueType;
    }

    /**
     * Returns whether the shared string of the receiver should be lookup when merging.
     *
     * @return  True when should be merged, otherwise false.
     */
    public boolean shouldLookupSharedString() {

        // returns whether to lookup the shared string of this value
        return ((this._valueType != null) && (this._valueType.equals("s")));
    }

    /**
     * Returns the value of the cell.
     *
     * @return  String
     */
    public String cellValue() {

        return this._cellValue;
    }

    /**
     * Set the value of the cell.
     *
     * @param cellValue The new string value.
     */
    public void setCellValue(String cellValue) {

        this._cellValue = cellValue;
    }

    public String cellIdentifier() {

        return this._cellIdentifier;
    }
}
