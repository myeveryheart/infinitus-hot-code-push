package com.m.hcp.events;

import com.m.hcp.config.ApplicationConfig;
import com.m.hcp.model.ChcpError;

/**
 * Created by Nikolay Demyankov on 25.08.15.
 * <p/>
 * Event is send when there is nothing to install.
 */
public class NothingToInstallEvent extends WorkerEvent {

    public static final String EVENT_NAME = "chcp_nothingToInstall";

    /**
     * Class constructor
     *
     * @param config Application config that was used for installation.
     */
    public NothingToInstallEvent(ApplicationConfig config) {
        super(EVENT_NAME, ChcpError.NOTHING_TO_INSTALL, config);
    }
}
