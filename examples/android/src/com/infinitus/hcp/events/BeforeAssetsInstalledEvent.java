package com.infinitus.hcp.events;

/**
 * Created by M on 16/9/9.
 *
 * 即将从assets拷贝到external storage
 */
public class BeforeAssetsInstalledEvent extends HCPEventImpl {

    public static final String EVENT_NAME = "hcp_beforeAssetsInstalledOnExternalStorage";

    /**
     * Class constructor
     */
    public BeforeAssetsInstalledEvent() {
        super(EVENT_NAME, null);
    }

}
