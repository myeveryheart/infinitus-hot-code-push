package com.infinitus.hcp.updater;

import com.infinitus.hcp.events.FetchUpdateErrorEvent;
import com.infinitus.hcp.events.UpdateDownloadErrorEvent;
import com.infinitus.hcp.model.HCPError;
import com.infinitus.hcp.model.HCPFilesStructure;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by M on 16/9/9.
 * <p/>
 * 下载更新工具类
 */
public class UpdatesLoader {

    private static boolean isExecuting;
    private static UpdateLoaderWorker task;

    /**
     * 是否正在下载
     *
     * @return <code>true</code> 正在下载; <code>false</code> otherwise
     */
    public static boolean isExecuting() {
        return isExecuting;
    }

    /**
     * 启动下载
     *
     * @param configURL                   服务器上的configUrl
     * @param currentReleaseFileStructure 当前版本的文件结构
     * @param currentNativeVersion        当前本地版本
     */
    public static void fetchUpdate(final String configURL, final HCPFilesStructure currentReleaseFileStructure, final int currentNativeVersion) {

        if (isExecuting) {
            EventBus.getDefault().post(new FetchUpdateErrorEvent(HCPError.DOWNLOAD_ALREADY_IN_PROGRESS, null));
            return;
        }

        if (UpdatesInstaller.isInstalling()) {
            EventBus.getDefault().post(new FetchUpdateErrorEvent(HCPError.CANT_DOWNLOAD_UPDATE_WHILE_INSTALLATION_IN_PROGRESS, null));
            return;
        }

        isExecuting = true;

        task = new UpdateLoaderWorker(configURL, currentReleaseFileStructure, currentNativeVersion);
        new Thread(new Runnable() {
            @Override
            public void run() {
                task.fetch();
                isExecuting = false;
                EventBus.getDefault().post(task.result());
            }
        }).start();
    }

    public static void downloadUpdate()
    {
        if (isExecuting) {
            EventBus.getDefault().post(new UpdateDownloadErrorEvent(HCPError.DOWNLOAD_ALREADY_IN_PROGRESS, null));
            return;
        }

        if (UpdatesInstaller.isInstalling()) {
            EventBus.getDefault().post(new UpdateDownloadErrorEvent(HCPError.CANT_DOWNLOAD_UPDATE_WHILE_INSTALLATION_IN_PROGRESS, null));
            return;
        }

        isExecuting = true;

        new Thread(new Runnable() {
            @Override
            public void run() {
                task.download();
                isExecuting = false;
                EventBus.getDefault().post(task.result());
            }
        }).start();
    }

}