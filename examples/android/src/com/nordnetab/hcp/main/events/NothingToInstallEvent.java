package com.nordnetab.hcp.main.events;

import com.nordnetab.hcp.main.config.ApplicationConfig;
import com.nordnetab.hcp.main.model.HCPError;

/**
 * Created by M on 16/9/9.
 * <p/>
 * 不需要安装更新
 */
public class NothingToInstallEvent extends WorkerEvent {

    public static final String EVENT_NAME = "hcp_nothingToInstall";

    /**
     * Class constructor
     *
     * @param config Application config that was used for installation.
     */
    public NothingToInstallEvent(ApplicationConfig config) {
        super(EVENT_NAME, HCPError.NOTHING_TO_INSTALL, config);
    }
}
