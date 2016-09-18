package com.nordnetab.hcp.main.events;

import com.nordnetab.hcp.main.config.ApplicationConfig;
import com.nordnetab.hcp.main.model.HcpError;

/**
 * Created by M on 16/9/9.
 * <p/>
 * 不需要下载更新
 */
public class NothingToUpdateEvent extends WorkerEvent {

    public static final String EVENT_NAME = "hcp_nothingToUpdate";

    /**
     * Class constructor.
     *
     * @param config application config that was used for update download
     */
    public NothingToUpdateEvent(ApplicationConfig config) {
        super(EVENT_NAME, HcpError.NOTHING_TO_UPDATE, config);
    }

}
