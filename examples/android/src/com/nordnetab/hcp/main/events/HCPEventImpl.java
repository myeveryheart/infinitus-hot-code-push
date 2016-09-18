package com.nordnetab.hcp.main.events;

import com.nordnetab.hcp.main.model.HcpError;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by M on 16/9/9.
 * <p/>
 * Implementation of the IHCPEvent interface.
 * Also, base class for all hcp specific events.
 * All events are dispatched and captured through EventBus.
 *
 * @see de.greenrobot.event.EventBus
 */
class HCPEventImpl implements HCPEvent {

    private final HcpError error;
    private final String eventName;
    private final Map<String, Object>data;

    /**
     * Class constructor
     *
     * @param eventName string identifier of the event
     * @param error     error information
     *
     * @see HcpError
     */
    protected HCPEventImpl(String eventName, HcpError error) {
        this.eventName = eventName;
        this.error = error;
        this.data = new HashMap<String, Object>();
    }

    @Override
    public String name() {
        return eventName;
    }

    @Override
    public HcpError error() {
        return error;
    }

    @Override
    public Map<String, Object> data() {
        return data;
    }
}