package com.infinitus.hcp.events;

import com.infinitus.hcp.config.ApplicationConfig;
import com.infinitus.hcp.model.HCPError;

/**
 * Created by M on 16/9/9.
 * <p/>
 * 下载文件出错
 */
public class FetchUpdateErrorEvent extends WorkerEvent {

    public static final String EVENT_NAME = "hcp_fetchUpdateFailed";

    /**
     * Class constructor.
     *
     * @param error  错误
     * @param config 使用的config
     */
    public FetchUpdateErrorEvent(HCPError error, ApplicationConfig config) {
        super(EVENT_NAME, error, config);
    }
}
