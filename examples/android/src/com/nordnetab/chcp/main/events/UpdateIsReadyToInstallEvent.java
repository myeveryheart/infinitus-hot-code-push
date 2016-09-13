package com.nordnetab.chcp.main.events;

import com.nordnetab.chcp.main.config.ApplicationConfig;

/**
 * Created by M on 16/9/9.
 * <p/>
 * 更新文件可以安装
 */
public class UpdateIsReadyToInstallEvent extends WorkerEvent {

    public static final String EVENT_NAME = "chcp_updateIsReadyToInstall";

    /**
     * Class constructor
     *
     * @param config 使用的config
     */
    public UpdateIsReadyToInstallEvent(ApplicationConfig config) {
        super(EVENT_NAME, null, config);
    }
}
