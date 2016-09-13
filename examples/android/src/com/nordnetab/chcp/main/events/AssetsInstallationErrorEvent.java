package com.nordnetab.chcp.main.events;

import com.nordnetab.chcp.main.model.ChcpError;

/**
 * Created by M on 16/9/9.
 *
 * 从assets拷贝到external storage出错
 */
public class AssetsInstallationErrorEvent extends PluginEventImpl {

    public static final String EVENT_NAME = "chcp_assetsInstallationError";

    /**
     * Class constructor
     */
    public AssetsInstallationErrorEvent() {
        super(EVENT_NAME, ChcpError.FAILED_TO_INSTALL_ASSETS_ON_EXTERNAL_STORAGE);
    }
}