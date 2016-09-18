package com.nordnetab.chcp.main.updater;

import com.nordnetab.chcp.main.events.WorkerEvent;

/**
 * Created by M on 16/9/9.
 * <p/>
 * update Interface
 */
interface WorkerTask extends Runnable {

    /**
     * Get event, that describes the result of the task execution.
     */
    WorkerEvent result();

}
