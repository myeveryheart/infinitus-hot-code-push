package com.nordnetab.chcp.main.events;

import com.nordnetab.chcp.main.config.ApplicationConfig;
import com.nordnetab.chcp.main.model.ChcpError;

/**
 * Created by M on 16/9/9.
 * <p/>
 * 下载文件出错
 */
public class UpdateDownloadErrorEvent extends WorkerEvent {

    public static final String EVENT_NAME = "chcp_updateLoadFailed";

    /**
     * Class constructor.
     *
     * @param error  错误
     * @param config 使用的config
     */
    public UpdateDownloadErrorEvent(ChcpError error, ApplicationConfig config) {
        super(EVENT_NAME, error, config);
    }
}
