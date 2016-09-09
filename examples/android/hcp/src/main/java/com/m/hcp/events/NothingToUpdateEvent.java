package com.m.hcp.events;

import com.m.hcp.config.ApplicationConfig;
import com.m.hcp.model.ChcpError;

/**
 * Created by Nikolay Demyankov on 25.08.15.
 * <p/>
 * Event is send when there is nothing new to download from server.
 */
public class NothingToUpdateEvent extends WorkerEvent {

    public static final String EVENT_NAME = "chcp_nothingToUpdate";

    /**
     * Class constructor.
     *
     * @param config application config that was used for update download
     */
    public NothingToUpdateEvent(ApplicationConfig config) {
        super(EVENT_NAME, ChcpError.NOTHING_TO_UPDATE, config);
    }

}
