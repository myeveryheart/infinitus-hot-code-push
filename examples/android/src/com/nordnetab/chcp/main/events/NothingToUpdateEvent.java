package com.nordnetab.chcp.main.events;

import com.nordnetab.chcp.main.config.ApplicationConfig;
import com.nordnetab.chcp.main.model.ChcpError;

/**
 * Created by M on 16/9/9.
 * <p/>
 * 不需要下载更新
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
