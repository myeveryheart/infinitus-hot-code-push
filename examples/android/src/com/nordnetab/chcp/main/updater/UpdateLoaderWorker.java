package com.nordnetab.chcp.main.updater;

import android.text.TextUtils;
import android.util.Log;

import com.nordnetab.chcp.main.HCPHelper;
import com.nordnetab.chcp.main.config.ApplicationConfig;
import com.nordnetab.chcp.main.config.ContentManifest;
import com.nordnetab.chcp.main.events.NothingToUpdateEvent;
import com.nordnetab.chcp.main.events.UpdateDownloadErrorEvent;
import com.nordnetab.chcp.main.events.UpdateIsReadyToInstallEvent;
import com.nordnetab.chcp.main.events.WorkerEvent;
import com.nordnetab.chcp.main.model.ChcpError;
import com.nordnetab.chcp.main.model.ManifestDiff;
import com.nordnetab.chcp.main.model.ManifestFile;
import com.nordnetab.chcp.main.model.PluginFilesStructure;
import com.nordnetab.chcp.main.model.UpdateTime;
import com.nordnetab.chcp.main.network.ApplicationConfigDownloader;
import com.nordnetab.chcp.main.network.ContentManifestDownloader;
import com.nordnetab.chcp.main.network.DownloadResult;
import com.nordnetab.chcp.main.network.FileDownloader;
import com.nordnetab.chcp.main.storage.ApplicationConfigStorage;
import com.nordnetab.chcp.main.storage.ContentManifestStorage;
import com.nordnetab.chcp.main.storage.IObjectFileStorage;
import com.nordnetab.chcp.main.utils.FilesUtility;
import com.nordnetab.chcp.main.utils.URLUtility;

import java.io.IOException;
import java.util.List;

/**
 * Created by M on 16/9/9.
 * <p/>
 * 下载类
 */
class UpdateLoaderWorker implements WorkerTask {

    private final String applicationConfigUrl;
    private final int appNativeVersion;
    private final PluginFilesStructure filesStructure;

    private IObjectFileStorage<ApplicationConfig> appConfigStorage;
    private IObjectFileStorage<ContentManifest> manifestStorage;

    private ApplicationConfig oldAppConfig;
    private ContentManifest oldManifest;

    private WorkerEvent resultEvent;

    private HCPHelper.FetchUpdateCallback fetchUpdateCallback;

    /**
     * Constructor.
     *
     * @param configUrl                   config url
     * @param currentReleaseFileStructure 当前版本的文件结构
     * @param currentNativeVersion        当前app版本
     */
    public UpdateLoaderWorker(final String configUrl, final PluginFilesStructure currentReleaseFileStructure, final int currentNativeVersion, HCPHelper.FetchUpdateCallback fetchUpdateCallback) {
        filesStructure = currentReleaseFileStructure;
        applicationConfigUrl = configUrl;
        appNativeVersion = currentNativeVersion;
        this.fetchUpdateCallback = fetchUpdateCallback;
    }

    public void fetch()
    {
        Log.d("CHCP", "Starting loader worker ");
        if (!init()) {
            fetchUpdateCallback.fetchUpdateCallback(false, resultEvent.error());
            return;
        }

        // 下载新的config
        ApplicationConfig newAppConfig = downloadApplicationConfig();
        if (newAppConfig == null) {
            setErrorResult(ChcpError.FAILED_TO_DOWNLOAD_APPLICATION_CONFIG, null);
            fetchUpdateCallback.fetchUpdateCallback(false, resultEvent.error());
            return;
        }

        // 新版本号比旧版大才更新
        if (newAppConfig.getContentConfig().getReleaseVersion().compareTo(oldAppConfig.getContentConfig().getReleaseVersion()) <= 0) {
            setNothingToUpdateResult(newAppConfig);
            fetchUpdateCallback.fetchUpdateCallback(false, resultEvent.error());
            return;
        }

        // 本地app版本是否支持新版本
        if (newAppConfig.getContentConfig().getMinimumNativeVersion() > appNativeVersion) {
            setErrorResult(ChcpError.APPLICATION_BUILD_VERSION_TOO_LOW, newAppConfig);
            fetchUpdateCallback.fetchUpdateCallback(false, resultEvent.error());
            return;
        }

        if (newAppConfig.getContentConfig().getUpdateTime() == UpdateTime.FORCED)
        {
            //强制更新
            fetchUpdateCallback.fetchUpdateCallback(true, null);
        }
    }

    @Override
    public void run() {
        fetch();

//        Log.d("CHCP", "Starting loader worker ");
//        if (!init()) {
//            return;
//        }
//
//        // 下载新的config
//        ApplicationConfig newAppConfig = downloadApplicationConfig();
//        if (newAppConfig == null) {
//            setErrorResult(ChcpError.FAILED_TO_DOWNLOAD_APPLICATION_CONFIG, null);
//            return;
//        }
//
//        // 新版本号比旧版大才更新
//        if (newAppConfig.getContentConfig().getReleaseVersion().compareTo(oldAppConfig.getContentConfig().getReleaseVersion()) <= 0) {
//            setNothingToUpdateResult(newAppConfig);
//
//            return;
//        }
//
//        // 本地app版本是否支持新版本
//        if (newAppConfig.getContentConfig().getMinimumNativeVersion() > appNativeVersion) {
//            setErrorResult(ChcpError.APPLICATION_BUILD_VERSION_TOO_LOW, newAppConfig);
//            return;
//        }
//
//        if (newAppConfig.getContentConfig().getUpdateTime() == UpdateTime.FORCED)
//        {
//            //强制更新
//
//        }
//
//        // download new content manifest
//        ContentManifest newContentManifest = downloadContentManifest(newAppConfig);
//        if (newContentManifest == null) {
//            setErrorResult(ChcpError.FAILED_TO_DOWNLOAD_CONTENT_MANIFEST, newAppConfig);
//            return;
//        }
//
//        // find files that were updated
//        ManifestDiff diff = oldManifest.calculateDifference(newContentManifest);
//        if (diff.isEmpty()) {
//            manifestStorage.storeInFolder(newContentManifest, filesStructure.getWwwFolder());
//            appConfigStorage.storeInFolder(newAppConfig, filesStructure.getWwwFolder());
//            setNothingToUpdateResult(newAppConfig);
//
//            return;
//        }
//
//        // switch file structure to new release
//        filesStructure.switchToRelease(newAppConfig.getContentConfig().getReleaseVersion());
//
//        recreateDownloadFolder(filesStructure.getDownloadFolder());
//
//        // download files
//        boolean isDownloaded = downloadNewAndChangedFiles(newAppConfig, diff);
//        if (!isDownloaded) {
//            cleanUp();
//            setErrorResult(ChcpError.FAILED_TO_DOWNLOAD_UPDATE_FILES, newAppConfig);
//            return;
//        }
//
//        // store configs
//        manifestStorage.storeInFolder(newContentManifest, filesStructure.getDownloadFolder());
//        appConfigStorage.storeInFolder(newAppConfig, filesStructure.getDownloadFolder());
//
//        // notify that we are done
//        setSuccessResult(newAppConfig);
//
//        Log.d("CHCP", "Loader worker has finished");
    }

    /**
     * 初始化
     *
     * @return <code>true</code> 成功, <code>false</code> - failed to initialize
     */
    private boolean init() {
        manifestStorage = new ContentManifestStorage();
        appConfigStorage = new ApplicationConfigStorage();

        // load current application config
        oldAppConfig = appConfigStorage.loadFromFolder(filesStructure.getWwwFolder());
        if (oldAppConfig == null) {
            setErrorResult(ChcpError.LOCAL_VERSION_OF_APPLICATION_CONFIG_NOT_FOUND, null);
            return false;
        }

        // load current content manifest
        oldManifest = manifestStorage.loadFromFolder(filesStructure.getWwwFolder());
        if (oldManifest == null) {
            setErrorResult(ChcpError.LOCAL_VERSION_OF_MANIFEST_NOT_FOUND, null);
            return false;
        }

        return true;
    }

    /**
     * 从服务器下载config文件
     *
     * @return 新config
     */
    private ApplicationConfig downloadApplicationConfig() {
        DownloadResult<ApplicationConfig> downloadResult = new ApplicationConfigDownloader(applicationConfigUrl).download();
        if (downloadResult.error != null) {
            Log.d("CHCP", "Failed to download application config");

            return null;
        }

        return downloadResult.value;
    }

    /**
     * 从服务器下载新的manifest文件
     *
     * @param config new application config from which we will take content url
     * @return new content manifest
     */
    private ContentManifest downloadContentManifest(ApplicationConfig config) {
        final String contentUrl = config.getContentConfig().getContentUrl();
        if (TextUtils.isEmpty(contentUrl)) {
            Log.d("CHCP", "Content url is not set in your application config! Can't load updates.");
            return null;
        }

        final String url = URLUtility.construct(contentUrl, PluginFilesStructure.MANIFEST_FILE_NAME);
        DownloadResult<ContentManifest> downloadResult = new ContentManifestDownloader(url).download();
        if (downloadResult.error != null) {
            Log.d("CHCP", "Failed to download content manifest");
            return null;
        }

        return downloadResult.value;
    }

    /**
     * 删除旧的下载文件夹，创建新的
     *
     * @param folder 下载文件夹的路径
     */
    private void recreateDownloadFolder(final String folder) {
        FilesUtility.delete(folder);
        FilesUtility.ensureDirectoryExists(folder);
    }

    /**
     * 下载新的和更新的文件
     *
     * @param newAppConfig 新的config
     * @param diff         manifest diff
     * @return <code>true</code> 下载成功; <code>false</code> - otherwise
     */
    private boolean downloadNewAndChangedFiles(ApplicationConfig newAppConfig, ManifestDiff diff) {
        final String contentUrl = newAppConfig.getContentConfig().getContentUrl();
        if (TextUtils.isEmpty(contentUrl)) {
            Log.d("CHCP", "Content url is not set in your application config! Can't load updates.");
            return false;
        }

        List<ManifestFile> downloadFiles = diff.getUpdateFiles();

        boolean isFinishedWithSuccess = true;
        try {
            FileDownloader.downloadFiles(filesStructure.getDownloadFolder(), contentUrl, downloadFiles);
        } catch (IOException e) {
            e.printStackTrace();
            isFinishedWithSuccess = false;
        }

        return isFinishedWithSuccess;
    }

    /**
     * 删除临时文件
     */
    private void cleanUp() {
        FilesUtility.delete(filesStructure.getContentFolder());
    }

    // region Events

    private void setErrorResult(ChcpError error, ApplicationConfig newAppConfig) {
        resultEvent = new UpdateDownloadErrorEvent(error, newAppConfig);
    }

    private void setSuccessResult(ApplicationConfig newAppConfig) {
        resultEvent = new UpdateIsReadyToInstallEvent(newAppConfig);
    }

    private void setNothingToUpdateResult(ApplicationConfig newAppConfig) {
        resultEvent = new NothingToUpdateEvent(newAppConfig);
    }

    @Override
    public WorkerEvent result() {
        return resultEvent;
    }

    // endregion
}