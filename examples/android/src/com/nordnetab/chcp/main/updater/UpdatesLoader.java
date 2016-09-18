package com.nordnetab.chcp.main.updater;

import com.nordnetab.chcp.main.HCPHelper;
import com.nordnetab.chcp.main.model.ChcpError;
import com.nordnetab.chcp.main.model.PluginFilesStructure;

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
    public static void fetchUpdate(final String configURL, final PluginFilesStructure currentReleaseFileStructure, final int currentNativeVersion, HCPHelper.FetchUpdateCallback fetchUpdateCallback) {

        if (isExecuting) {
            fetchUpdateCallback.fetchUpdateCallback(false, ChcpError.DOWNLOAD_ALREADY_IN_PROGRESS);
            return;
        }

        if (UpdatesInstaller.isInstalling()) {
            fetchUpdateCallback.fetchUpdateCallback(false, ChcpError.CANT_DOWNLOAD_UPDATE_WHILE_INSTALLATION_IN_PROGRESS);
            return;
        }

        isExecuting = true;

        final UpdateLoaderWorker task = new UpdateLoaderWorker(configURL, currentReleaseFileStructure, currentNativeVersion, fetchUpdateCallback);
        new Thread(new Runnable() {
            @Override
            public void run() {
                task.run();
                isExecuting = false;

//                EventBus.getDefault().post(task.result());
            }
        }).start();

//        return ChcpError.NONE;
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