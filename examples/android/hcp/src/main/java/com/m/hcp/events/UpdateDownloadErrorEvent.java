package com.m.hcp.events;

import com.m.hcp.config.ApplicationConfig;
import com.m.hcp.model.ChcpError;

/**
 * Created by Nikolay Demyankov on 25.08.15.
 * <p/>
 * Event is send when some error happened during the update download.
 */
public class UpdateDownloadErrorEvent extends WorkerEvent {

    public static final String EVENT_NAME = "chcp_updateLoadFailed";

    /**
     * Class constructor.
     *
     * @param error  error information
     * @param config application config that was used for update download
     */
    public UpdateDownloadErrorEvent(ChcpError error, ApplicationConfig config) {
        super(EVENT_NAME, error, config);
    }
}
