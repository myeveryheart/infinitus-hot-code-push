package com.nordnetab.hcp.main.events;

import com.nordnetab.hcp.main.model.HcpError;

import java.util.Map;

/**
 * Created by M on 16/9/9.
 * <p/>
 * Interface describes hcp specific events.
 */
public interface HCPEvent {

    /**
     * String identifier of the event.
     * Used for dispatching same event in JavaScript.
     */
    String name();

    /**
     * Error information, that is attached to the event
     *
     * @see HcpError
     */
    HcpError error();

    /**
     * Additional user information, attached to the event
     *
     * @return map with additional event data
     */
    Map<String, Object> data();

}
