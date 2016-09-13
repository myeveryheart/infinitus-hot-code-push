package com.nordnetab.chcp.main.events;

/**
 * Created by M on 16/9/9.
 *
 * 从assets拷贝到external storage成功
 */
public class AssetsInstalledEvent extends PluginEventImpl {

    public static final String EVENT_NAME = "chcp_assetsInstalledOnExternalStorage";

    /**
     * Class constructor
     */
    public AssetsInstalledEvent() {
        super(EVENT_NAME, null);
    }
}