package com.nordnetab.chcp.main.events;

/**
 * Created by M on 16/9/9.
 *
 * 即将从assets拷贝到external storage
 */
public class BeforeAssetsInstalledEvent extends PluginEventImpl {

    public static final String EVENT_NAME = "chcp_beforeAssetsInstalledOnExternalStorage";

    /**
     * Class constructor
     */
    public BeforeAssetsInstalledEvent() {
        super(EVENT_NAME, null);
    }

}
