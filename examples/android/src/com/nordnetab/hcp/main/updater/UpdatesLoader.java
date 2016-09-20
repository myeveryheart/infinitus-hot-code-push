package com.nordnetab.hcp.main.updater;

import com.nordnetab.hcp.main.events.FetchUpdateErrorEvent;
import com.nordnetab.hcp.main.events.UpdateDownloadErrorEvent;
import com.nordnetab.hcp.main.model.HCPError;
import com.nordnetab.hcp.main.model.HCPFilesStructure;
import com.nordnetab.hcp.main.utils.AssetsHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

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
        final EventBus eventBus = EventBus.getDefault();
//        if (!eventBus.isRegistered(UpdatesLoader.class)) {
//            eventBus.register(UpdatesLoader.class);
//        }

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

//    /**
//     * 检查更新错误
//     *
//     * @param event event details
//     * @see FetchUpdateErrorEvent
//     * @see AssetsHelper
//     * @see EventBus
//     */
//    @SuppressWarnings("unused")
//    @Subscribe
//    public void onEvent(final FetchUpdateErrorEvent event) {
//        handler.post(new Runnable() {
//            @Override
//            public void run() {
//                fetchUpdateCallback.fetchUpdateCallback(false, event.error());
//            }
//        });
//    }
}