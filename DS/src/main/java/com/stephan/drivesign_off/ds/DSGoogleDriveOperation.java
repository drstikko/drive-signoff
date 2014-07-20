package com.stephan.drivesign_off.ds;

/**
 * Abstract operation type that is used by the Google Drive API manager for executing long during tasks.
 *
 * @abstract    DSGoogleDriveOperation
 */
public abstract class DSGoogleDriveOperation implements Runnable {

    /**
     * Specifies whether the operation has finished.
     *
     * @var _finished
     */
    private boolean _finished = false;

    /**
     * Specifies whether the operation should be retried after a recoverable action
     *
     * @var _recoverable
     */
    private boolean _recoverable = false;

    /**
     * Specifies whether the receiver is executing.
     *
     * @var _executing
     */
    private boolean _executing = false;

    /**
     * Returns whether the receiver is finished.
     *
     * @return boolean
     */
    public boolean isFinished() {

        return this._finished;
    }

    /**
     * Returns whether the receiver can be recovered.
     *
     * @return  boolean
     */
    public boolean isRecoverable() {

        return this._recoverable;
    }

    /**
     * Returns whether the receiver is executing
     *
     * @return  True when executing otherwise false.
     */
    public boolean isExecuting() {

        return this._executing;
    }

    /**
     * Sets whether the receiver is finished.
     *
     * @param value A boolean value.
     */
    public void setFinished(boolean value) {
        this._finished = value;
    }

    /**
     * Sets whether the receiver is recoverable.
     *
     * @param value A boolean value.
     */
    public void setRecoverable(boolean value) {
        this._recoverable = value;
    }

    /**
     * Sets whether the receiver is executing.
     *
     * @param value A boolean value.
     */
    public void setExecuting(boolean value) {
        this._executing = value;
    }
}
