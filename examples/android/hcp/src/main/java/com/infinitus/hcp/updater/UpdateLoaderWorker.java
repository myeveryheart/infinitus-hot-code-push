package com.infinitus.hcp.updater;

import android.text.TextUtils;
import android.util.Log;

import com.infinitus.hcp.config.ApplicationConfig;
import com.infinitus.hcp.config.ContentManifest;
import com.infinitus.hcp.events.FetchUpdateCompletedEvent;
import com.infinitus.hcp.events.FetchUpdateErrorEvent;
import com.infinitus.hcp.events.UpdateDownloadErrorEvent;
import com.infinitus.hcp.events.UpdateIsReadyToInstallEvent;
import com.infinitus.hcp.events.WorkerEvent;
import com.infinitus.hcp.model.HCPError;
import com.infinitus.hcp.model.ManifestDiff;
import com.infinitus.hcp.model.ManifestFile;
import com.infinitus.hcp.model.HCPFilesStructure;
import com.infinitus.hcp.model.UpdateTime;
import com.infinitus.hcp.network.ApplicationConfigDownloader;
import com.infinitus.hcp.network.ContentManifestDownloader;
import com.infinitus.hcp.network.DownloadResult;
import com.infinitus.hcp.network.FileDownloader;
import com.infinitus.hcp.storage.ApplicationConfigStorage;
import com.infinitus.hcp.storage.ContentManifestStorage;
import com.infinitus.hcp.storage.IObjectFileStorage;
import com.infinitus.hcp.utils.FilesUtility;
import com.infinitus.hcp.utils.URLUtility;

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
    private final HCPFilesStructure filesStructure;

    private IObjectFileStorage<ApplicationConfig> appConfigStorage;
    private IObjectFileStorage<ContentManifest> manifestStorage;

    private ApplicationConfig oldAppConfig;
    private ContentManifest oldManifest;

    private WorkerEvent resultEvent;
    private ApplicationConfig newAppConfig;

    /**
     * Constructor.
     *
     * @param configUrl                   config url
     * @param currentReleaseFileStructure 当前版本的文件结构
     * @param currentNativeVersion        当前app版本
     */
    public UpdateLoaderWorker(final String configUrl, final HCPFilesStructure currentReleaseFileStructure, final int currentNativeVersion) {
        filesStructure = currentReleaseFileStructure;
        applicationConfigUrl = configUrl;
        appNativeVersion = currentNativeVersion;
    }

    public void fetch()
    {
        Log.d("HCP", "Starting fetch worker");
        if (!init()) {
            return;
        }

        // 下载新的config
        newAppConfig = downloadApplicationConfig();
        if (newAppConfig == null) {
            setFetchErrorResult(HCPError.FAILED_TO_DOWNLOAD_APPLICATION_CONFIG, null);
            return;
        }

        // 新版本号比旧版大才更新
        if (newAppConfig.getContentConfig().getReleaseVersion().compareTo(oldAppConfig.getContentConfig().getReleaseVersion()) <= 0) {
            setFetchErrorResult(HCPError.NOTHING_TO_UPDATE, newAppConfig);
            return;
        }

        // 本地app版本是否支持新版本
        if (newAppConfig.getContentConfig().getMinimumNativeVersion() > appNativeVersion) {
            setFetchErrorResult(HCPError.APPLICATION_BUILD_VERSION_TOO_LOW, newAppConfig);
            return;
        }

        setFetchSuccessResult(newAppConfig);

//        if (newAppConfig.getContentConfig().getUpdateTime() == UpdateTime.FORCED)
//        {
//            //强制更新
//            setFetchSuccessResult(newAppConfig);
//        }
//        else
//        {
//            //静默更新
//
//        }
        Log.d("HCP", "Fetch worker has finished");
    }

    public void download()
    {
        // 下载新的manifest
        ContentManifest newContentManifest = downloadContentManifest(newAppConfig);
        if (newContentManifest == null) {
            setDownloadErrorResult(HCPError.FAILED_TO_DOWNLOAD_CONTENT_MANIFEST, newAppConfig);
            return;
        }

        // 比较manifest
        ManifestDiff diff = oldManifest.calculateDifference(newContentManifest);
        if (diff.isEmpty()) {
            manifestStorage.storeInFolder(newContentManifest, filesStructure.getWwwFolder());
            appConfigStorage.storeInFolder(newAppConfig, filesStructure.getWwwFolder());
            setDownloadErrorResult(HCPError.NOTHING_TO_INSTALL, newAppConfig);

            return;
        }

        // 新版文件
        filesStructure.switchToRelease(newAppConfig.getContentConfig().getReleaseVersion());

        recreateDownloadFolder(filesStructure.getDownloadFolder());

        // 下载更新文件
        boolean isDownloaded = downloadNewAndChangedFiles(newAppConfig, diff);
        if (!isDownloaded) {
            cleanUp();
            setDownloadErrorResult(HCPError.FAILED_TO_DOWNLOAD_UPDATE_FILES, newAppConfig);
            return;
        }

        // 保存manifest和config
        manifestStorage.storeInFolder(newContentManifest, filesStructure.getDownloadFolder());
        appConfigStorage.storeInFolder(newAppConfig, filesStructure.getDownloadFolder());

        setDownloadSuccessResult(newAppConfig);

        Log.d("HCP", "Loader worker has finished");
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
            setFetchErrorResult(HCPError.LOCAL_VERSION_OF_APPLICATION_CONFIG_NOT_FOUND, null);
            return false;
        }

        // load current content manifest
        oldManifest = manifestStorage.loadFromFolder(filesStructure.getWwwFolder());
        if (oldManifest == null) {
            setFetchErrorResult(HCPError.LOCAL_VERSION_OF_MANIFEST_NOT_FOUND, null);
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
            Log.d("HCP", "Failed to download application config");

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
            Log.d("HCP", "Content url is not set in your application config! Can't load updates.");
            return null;
        }

        final String url = URLUtility.construct(contentUrl, HCPFilesStructure.MANIFEST_FILE_NAME);
        DownloadResult<ContentManifest> downloadResult = new ContentManifestDownloader(url).download();
        if (downloadResult.error != null) {
            Log.d("HCP", "Failed to download content manifest");
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
            Log.d("HCP", "Content url is not set in your application config! Can't load updates.");
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

    private void setFetchErrorResult(HCPError error, ApplicationConfig newAppConfig) {
        resultEvent = new FetchUpdateErrorEvent(error, newAppConfig);
    }

    private void setFetchSuccessResult(ApplicationConfig newAppConfig){
        resultEvent = new FetchUpdateCompletedEvent(newAppConfig);
    }

    private void setDownloadErrorResult(HCPError error, ApplicationConfig newAppConfig) {
        resultEvent = new UpdateDownloadErrorEvent(error, newAppConfig);
    }

    private void setDownloadSuccessResult(ApplicationConfig newAppConfig) {
        resultEvent = new UpdateIsReadyToInstallEvent(newAppConfig);
    }

//    private void setNothingToUpdateResult(ApplicationConfig newAppConfig) {
//        resultEvent = new NothingToUpdateEvent(newAppConfig);
//    }

    @Override
    public WorkerEvent result() {
        return resultEvent;
    }

    // endregion
}