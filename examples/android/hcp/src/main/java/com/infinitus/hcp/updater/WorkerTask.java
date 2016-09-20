package com.infinitus.hcp.updater;

import com.infinitus.hcp.events.WorkerEvent;

/**
 * Created by M on 16/9/9.
 * <p/>
 * update Interface
 */
//interface WorkerTask extends Runnable {
interface WorkerTask {
    /**
     * Get event, that describes the result of the task execution.
     */
    WorkerEvent result();

}
