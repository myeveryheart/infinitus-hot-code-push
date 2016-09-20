package com.infinitus.hcp.events;

/**
 * Created by M on 16/9/9.
 * <p/>
 * 即将安装更新
 */
public class BeforeInstallEvent extends WorkerEvent {

    public static final String EVENT_NAME = "hcp_beforeInstall";

    /**
     * Class constructor.
     */
    public BeforeInstallEvent() {
        super(EVENT_NAME, null, null);
    }
}
