package com.infinitus.hcp.events;

import com.infinitus.hcp.model.HCPError;

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
        super(EVENT_NAME, HCPError.FAILED_TO_INSTALL_ASSETS_ON_EXTERNAL_STORAGE);
    }
}