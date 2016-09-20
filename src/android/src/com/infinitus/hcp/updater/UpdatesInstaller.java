package com.infinitus.hcp.updater;

import android.content.Context;

import com.infinitus.hcp.events.BeforeInstallEvent;
import com.infinitus.hcp.events.NothingToInstallEvent;
import com.infinitus.hcp.events.UpdateInstallationErrorEvent;
import com.infinitus.hcp.model.HCPError;
import com.infinitus.hcp.model.HCPFilesStructure;

import org.greenrobot.eventbus.EventBus;
import java.io.File;

/**
 * Created by M on 16/9/9.
 * <p/>
 * 安装更新的工具类
 */
public class UpdatesInstaller {

    private static boolean isInstalling;

    /**
     * 是否正在安装
     *
     * @return <code>true</code> - 正在安装; <code>false</code> - otherwise
     */
    public static boolean isInstalling() {
        return isInstalling;
    }

    /**
     * 启动安装
     *
     * @param context        context
     * @param newVersion     需要安装的版本
     * @param currentVersion 当前版本
     * @return <code>HCPError.NONE</code> 启动成功; otherwise - error details
     * @see NothingToInstallEvent
     * @see com.infinitus.hcp.events.UpdateInstallationErrorEvent
     * @see com.infinitus.hcp.events.UpdateInstalledEvent
     */
    public static void install(final Context context, final String newVersion, final String currentVersion) {

        if (isInstalling) {
            EventBus.getDefault().post(new UpdateInstallationErrorEvent(HCPError.INSTALLATION_ALREADY_IN_PROGRESS, null));
            return;
        }

        if (UpdatesLoader.isExecuting()) {
            EventBus.getDefault().post(new UpdateInstallationErrorEvent(HCPError.CANT_INSTALL_WHILE_DOWNLOAD_IN_PROGRESS, null));
            return;
        }

        final HCPFilesStructure newReleaseFS = new HCPFilesStructure(context, newVersion);
        if (!new File(newReleaseFS.getDownloadFolder()).exists()) {
            EventBus.getDefault().post(new UpdateInstallationErrorEvent(HCPError.NOTHING_TO_INSTALL, null));
            return;
        }

        dispatchBeforeInstallEvent();

        final InstallationWorker task = new InstallationWorker(context, newVersion, currentVersion);

        isInstalling = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                task.install();
                isInstalling = false;

                // dispatch resulting event
                EventBus.getDefault().post(task.result());
            }
        }).start();
    }



    private static void dispatchBeforeInstallEvent() {
        EventBus.getDefault().post(new BeforeInstallEvent());
    }
}
