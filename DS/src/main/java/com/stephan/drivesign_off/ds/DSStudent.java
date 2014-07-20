package com.stephan.drivesign_off.ds;

import android.os.Bundle;

import java.util.HashMap;
import java.util.Map;

/**
 * A class encapsulating a student row in the sign off document.
 *
 * @class   DSStudent
 */
public class DSStudent {

    /**
     * The identifier of the student.
     *
     * @var String
     */
    private String _studentIdentifier;

    /**
     * The name of the student.
     *
     * @var String
     */
    private String _studentName;

    /**
     * The map containing each signed off property, the boolean indicates whether it is done yes or no.
     *
     * @var Map<String, Integer>
     */
    private Map<String, String> _signOffInfo;

    /**
     * Creates a new student model from the identifier and name.
     */
    public DSStudent() {

        // create the map
        this._signOffInfo = new HashMap<String, String>();
    }

    /**
     * Returns the student identifier of the reciever.
     *
     * @return  String
     */
    public String studentIdentifier() {

        return this._studentIdentifier;
    }

    /**
     * Returns the name of the student.
     *
     * @return  String
     */
    public String studentName() {

        return this._studentName;
    }

    /**
     * Sets the signed off value for a specified assignment key.
     *
     * @param value         The string value of the signed off assignment. (Usually a week identifier)
     * @param signOffType   The assignment name.
     */
    public void setSignedOffValueForType(String value, String signOffType) {

        // when a null value will be inserted make it an empty string
        if (value == null) {
            value = "";
        }

        // add the value which may replace existing ones
        this._signOffInfo.put(signOffType, value);
    }

    /**
     * Returns the value description for the given assignment.
     *
     * @param signOffType   The assignment name for which to fetch the value.
     * @return              String
     */
    public String valueForAssignment(String signOffType) {

        if (this._signOffInfo.containsKey(signOffType)) {
            return this._signOffInfo.get(signOffType);
        } else {
            return "";
        }
    }

    /**
     * Returns a bundle with all assignment info of the receiver.
     *
     * @return  Bundle
     */
    public Bundle studentAssignmentInfoBundle() {

        // create the new bundle for the assignment info
        Bundle assignmentBundle = new Bundle();

        // process all values in the set
        for (String currentAssignmentName : this._signOffInfo.keySet()) {

            // add the current assignment name and value to the bundle
            assignmentBundle.putString(currentAssignmentName, this._signOffInfo.get(currentAssignmentName));
        }

        return assignmentBundle;
    }

    /**
     * Sets the student identifier of the receiver.
     *
     * @param studentId The new student id.
     */
    public void setStudentId(String studentId) {

        this._studentIdentifier = studentId;
    }

    /**
     * Sets the student name of the receiver.
     *
     * @param name  The new student name.
     */
    public void setStudentName(String name) {

        this._studentName = name;
    }
}
