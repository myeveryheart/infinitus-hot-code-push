package com.infinitus.hcp.events;

import com.infinitus.hcp.config.ApplicationConfig;
import com.infinitus.hcp.model.HCPError;

/**
 * Created by M on 16/9/9.
 * <p/>
 * 安装过程出错
 */
public class UpdateInstallationErrorEvent extends WorkerEvent {

    public static final String EVENT_NAME = "hcp_updateInstallFailed";

    /**
     * Class constructor.
     *
     * @param error  错误
     * @param config 使用的config
     */
    public UpdateInstallationErrorEvent(HCPError error, ApplicationConfig config) {
        super(EVENT_NAME, error, config);
    }
}
