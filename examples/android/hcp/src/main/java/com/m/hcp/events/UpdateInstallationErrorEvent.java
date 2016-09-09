package com.m.hcp.events;

import com.m.hcp.config.ApplicationConfig;
import com.m.hcp.model.ChcpError;

/**
 * Created by Nikolay Demyankov on 25.08.15.
 * <p/>
 * Event is send when some error has happened during the installation process.
 */
public class UpdateInstallationErrorEvent extends WorkerEvent {

    public static final String EVENT_NAME = "chcp_updateInstallFailed";

    /**
     * Class constructor.
     *
     * @param error  error details
     * @param config application config that was used for installation process
     */
    public UpdateInstallationErrorEvent(ChcpError error, ApplicationConfig config) {
        super(EVENT_NAME, error, config);
    }
}
