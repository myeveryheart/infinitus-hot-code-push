package com.nordnetab.chcp.main.events;

import com.nordnetab.chcp.main.config.ApplicationConfig;
import com.nordnetab.chcp.main.model.ChcpError;

/**
 * Created by M on 16/9/9.
 * <p/>
 * 安装过程出错
 */
public class UpdateInstallationErrorEvent extends WorkerEvent {

    public static final String EVENT_NAME = "chcp_updateInstallFailed";

    /**
     * Class constructor.
     *
     * @param error  错误
     * @param config 使用的config
     */
    public UpdateInstallationErrorEvent(ChcpError error, ApplicationConfig config) {
        super(EVENT_NAME, error, config);
    }
}
