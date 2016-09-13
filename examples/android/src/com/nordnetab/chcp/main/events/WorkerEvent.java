package com.nordnetab.chcp.main.events;

import com.nordnetab.chcp.main.config.ApplicationConfig;
import com.nordnetab.chcp.main.model.ChcpError;

import java.util.Map;

/**
 * Created by M on 16/9/9.
 * <p/>
 * Base class for events, that generated by installation/download workers.
 *
 * @see com.nordnetab.chcp.main.updater.InstallationWorker
 * @see com.nordnetab.chcp.main.updater.UpdateLoaderWorker
 * @see IPluginEvent
 */
public class WorkerEvent extends PluginEventImpl {

    private static final String CONFIG_KEY = "config";

    /**
     * Class constructor
     *
     * @param eventName string identifier of the event
     * @param error     error information
     * @param appConfig application config which this worker used
     * @see ChcpError
     * @see ApplicationConfig
     */
    protected WorkerEvent(String eventName, ChcpError error, ApplicationConfig appConfig) {
        super(eventName, error);

        if (appConfig != null) {
            data().put(CONFIG_KEY, appConfig);
        }
    }

    /**
     * Getter for application config, attached to this event.
     * This config was used to perform download/installation work.
     *
     * @return application config
     */
    public ApplicationConfig applicationConfig() {
        final Map<String, Object> eventData = data();
        if (!eventData.containsKey(CONFIG_KEY)) {
            return null;
        }

        return (ApplicationConfig) eventData.get(CONFIG_KEY);
    }
}