package com.nordnetab.hcp.main.updater;

import com.nordnetab.hcp.main.events.FetchUpdateErrorEvent;
import com.nordnetab.hcp.main.model.HCPError;
import com.nordnetab.hcp.main.model.HCPFilesStructure;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by M on 16/9/9.
 * <p/>
 * 下载更新工具类
 */
public class UpdatesLoader {

    private static boolean isExecuting;

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

        final UpdateLoaderWorker task = new UpdateLoaderWorker(configURL, currentReleaseFileStructure, currentNativeVersion);
        new Thread(new Runnable() {
            @Override
            public void run() {
                task.fetch();
                isExecuting = false;

                EventBus.getDefault().post(task.result());
            }
        }).start();
    }

//    private static void executeTask(final WorkerTask task) {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                task.run();
//                isExecuting = false;
//
//                EventBus.getDefault().post(task.result());
//            }
//        }).start();
//    }
}