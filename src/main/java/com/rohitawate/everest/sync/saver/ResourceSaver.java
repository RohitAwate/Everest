package com.rohitawate.everest.sync.saver;

/**
 * Saves a certain resource to their respective source on a background thread.
 */
public interface ResourceSaver extends Runnable {
    /**
     * Sets the resource to be saved.
     * The saving process should be carried out in run().
     * @param resource the resource to be saved
     */
    void setResource(Object resource);
}
