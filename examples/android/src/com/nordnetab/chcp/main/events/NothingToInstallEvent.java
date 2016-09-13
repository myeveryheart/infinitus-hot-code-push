package com.nordnetab.chcp.main.events;

import com.nordnetab.chcp.main.config.ApplicationConfig;
import com.nordnetab.chcp.main.model.ChcpError;

/**
 * Created by M on 16/9/9.
 * <p/>
 * 不需要安装更新
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
