package com.m.hcp.updater;

import com.m.hcp.events.WorkerEvent;

/**
 * Created by Nikolay Demyankov on 29.12.15.
 * <p/>
 * Interface describes update tasks.
 */
interface WorkerTask extends Runnable {

    /**
     * Get event, that describes the result of the task execution.
     */
    WorkerEvent result();

}
