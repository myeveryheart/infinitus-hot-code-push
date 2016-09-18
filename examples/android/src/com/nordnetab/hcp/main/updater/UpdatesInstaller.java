package com.nordnetab.hcp.main.updater;

import android.content.Context;

import com.nordnetab.hcp.main.events.BeforeInstallEvent;
import com.nordnetab.hcp.main.events.NothingToInstallEvent;
import com.nordnetab.hcp.main.model.HCPError;
import com.nordnetab.hcp.main.model.HCPFilesStructure;

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
     * @see com.nordnetab.hcp.main.events.UpdateInstallationErrorEvent
     * @see com.nordnetab.hcp.main.events.UpdateInstalledEvent
     */
    public static HCPError install(final Context context, final String newVersion, final String currentVersion) {
        // if we already installing - exit
        if (isInstalling) {
            return HCPError.INSTALLATION_ALREADY_IN_PROGRESS;
        }

        // if we are loading update - exit
        if (UpdatesLoader.isExecuting()) {
            return HCPError.CANT_INSTALL_WHILE_DOWNLOAD_IN_PROGRESS;
        }

        final HCPFilesStructure newReleaseFS = new HCPFilesStructure(context, newVersion);
        if (!new File(newReleaseFS.getDownloadFolder()).exists()) {
            return HCPError.NOTHING_TO_INSTALL;
        }

        dispatchBeforeInstallEvent();

        final WorkerTask task = new InstallationWorker(context, newVersion, currentVersion);
        execute(task);

        return HCPError.NONE;
    }

    private static void execute(final WorkerTask task) {
        isInstalling = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                task.run();
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
