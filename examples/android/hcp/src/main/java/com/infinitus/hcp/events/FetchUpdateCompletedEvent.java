package com.infinitus.hcp.events;

import com.infinitus.hcp.config.ApplicationConfig;

/**
 * Created by M on 16/9/9.
 * <p/>
 * 安装成功
 */
public class FetchUpdateCompletedEvent extends WorkerEvent {

    public static final String EVENT_NAME = "hcp_fetchUpdateCompleted";

    /**
     * Class constructor.
     *
     * @param config 使用的config
     */
    public FetchUpdateCompletedEvent(ApplicationConfig config) {
        super(EVENT_NAME, null, config);
    }
}
