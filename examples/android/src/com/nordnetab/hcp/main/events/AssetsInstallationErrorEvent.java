package com.nordnetab.hcp.main.events;

import com.nordnetab.hcp.main.model.HcpError;

/**
 * Created by M on 16/9/9.
 *
 * 从assets拷贝到external storage出错
 */
public class AssetsInstallationErrorEvent extends HCPEventImpl {

    public static final String EVENT_NAME = "hcp_assetsInstallationError";

    /**
     * Class constructor
     */
    public AssetsInstallationErrorEvent() {
        super(EVENT_NAME, HcpError.FAILED_TO_INSTALL_ASSETS_ON_EXTERNAL_STORAGE);
    }
}