package com.infinitus.hcp;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.infinitus.hcp.config.ApplicationConfig;
import com.infinitus.hcp.config.Config;
import com.infinitus.hcp.config.ContentConfig;
import com.infinitus.hcp.config.HCPInternalPreferences;
import com.infinitus.hcp.events.AssetsInstallationErrorEvent;
import com.infinitus.hcp.events.AssetsInstalledEvent;
import com.infinitus.hcp.events.BeforeInstallEvent;
import com.infinitus.hcp.events.DownloadProgressEvent;
import com.infinitus.hcp.events.FetchUpdateCompletedEvent;
import com.infinitus.hcp.events.FetchUpdateErrorEvent;
import com.infinitus.hcp.events.NothingToInstallEvent;
import com.infinitus.hcp.events.NothingToUpdateEvent;
import com.infinitus.hcp.events.UpdateDownloadErrorEvent;
import com.infinitus.hcp.events.UpdateInstallationErrorEvent;
import com.infinitus.hcp.events.UpdateInstalledEvent;
import com.infinitus.hcp.events.UpdateIsReadyToInstallEvent;
import com.infinitus.hcp.model.HCPError;
import com.infinitus.hcp.model.HCPFilesStructure;
import com.infinitus.hcp.model.UpdateTime;
import com.infinitus.hcp.storage.ApplicationConfigStorage;
import com.infinitus.hcp.storage.IObjectFileStorage;
import com.infinitus.hcp.storage.IObjectPreferenceStorage;
import com.infinitus.hcp.storage.HCPInternalPreferencesStorage;
import com.infinitus.hcp.updater.UpdatesInstaller;
import com.infinitus.hcp.updater.UpdatesLoader;
import com.infinitus.hcp.utils.AssetsHelper;
import com.infinitus.hcp.utils.CleanUpHelper;
import com.infinitus.hcp.utils.Paths;
import com.infinitus.hcp.utils.VersionHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;


/**
 * Created by M on 16/9/9.
 * <p/>
 * 更新工具类
 */
public class HCPHelper {

    public interface FetchUpdateCallback
    {
        void fetchUpdateCallback(boolean needUpdate, HCPError error);
    }
    public interface DownloadUpdateCallback
    {
        void downloadUpdateCallback(boolean success, int totalFiles, int fileDownloaded, HCPError error);
    }
    public interface InstallUpdateCallback
    {
        void installUpdateCallback(boolean success, HCPError error);
    }

    private static final String WWW_FOLDER = "www";
    private static final String LOCAL_ASSETS_FOLDER = "file:///android_asset/www";
    private IObjectFileStorage<ApplicationConfig> appConfigStorage;
    private HCPInternalPreferences hcpInternalPrefs;
    private IObjectPreferenceStorage<HCPInternalPreferences> hcpInternalPrefsStorage;
    private Config config;
    private HCPFilesStructure fileStructure;
    private String webUrl;
    private Context context;
    private static HCPHelper helper;
    private FetchUpdateCallback fetchUpdateCallback;
    private DownloadUpdateCallback downloadUpdateCallback;
    private InstallUpdateCallback installUpdateCallback;
    private static int totalFiles;
    private static int fileDownloaded;

    public static HCPHelper getInstance(Context context, String webUrl)
    {
        if (helper == null)
        {
            helper = new HCPHelper();
            helper.context = context;
            helper.setWebUrl(webUrl);
            final EventBus eventBus = EventBus.getDefault();
            if (!eventBus.isRegistered(helper)) {
                eventBus.register(helper);
            }
        }

        return helper;
    }

    private void setWebUrl(String webUrl)
    {
        this.webUrl = webUrl;
        doLocalInit();
        Log.d("HCP", "Currently running release version " + hcpInternalPrefs.getCurrentReleaseVersionName());

        // 清理文件
        if (!TextUtils.isEmpty(hcpInternalPrefs.getCurrentReleaseVersionName())) {
            CleanUpHelper.removeReleaseFolders(context,
                    new String[]{hcpInternalPrefs.getCurrentReleaseVersionName(),
                            hcpInternalPrefs.getPreviousReleaseVersionName(),
                            hcpInternalPrefs.getReadyForInstallationReleaseVersionName()
                    }
            );
        }

        fileStructure = new HCPFilesStructure(context, hcpInternalPrefs.getCurrentReleaseVersionName());
        appConfigStorage = new ApplicationConfigStorage();
    }

    private void doLocalInit()
    {
        // 初始化config
        config = Config.getDefaultConfig();
        config.setWebUrl(webUrl);
        // 加载internal preferences
        hcpInternalPrefsStorage = new HCPInternalPreferencesStorage(context);
        HCPInternalPreferences config = hcpInternalPrefsStorage.loadFromPreference();
        if (config == null || TextUtils.isEmpty(config.getCurrentReleaseVersionName())) {
            config = HCPInternalPreferences.createDefault(context);
            if (config.getCurrentReleaseVersionName().length() > 0)
            {
                hcpInternalPrefsStorage.storeInPreference(config);
            }
            else
            {
                Log.d("HCP","无法读取配置文件");
            }
        }
        hcpInternalPrefs = config;
    }

    /**
     *  是否从外部加载
     *
     *  @return 是否从外部加载
     */
    public boolean loadFromExternalStorageFolder()
    {
        if (isWWwFolderNeedsToBeInstalled())
        {
            installWwwFolder();
            return false;
        }
        return true;
    }

    /**
     *  获取加载www的路径
     *
     *  @return 加载www的路径
     */
    public String pathToWww()
    {
        if (isWWwFolderNeedsToBeInstalled())
        {
            return LOCAL_ASSETS_FOLDER;
        }
        else
        {
            return fileStructure.getWwwFolder();
        }
    }

    /**
     *  检查更新
     */
    public void fetchUpdate(FetchUpdateCallback fetchUpdateCallback)
    {
        this.fetchUpdateCallback = fetchUpdateCallback;
        final HCPFilesStructure currentReleaseFS = new HCPFilesStructure(context, hcpInternalPrefs.getCurrentReleaseVersionName());
        UpdatesLoader.fetchUpdate(config.getConfigUrl(), currentReleaseFS, config.getNativeInterfaceVersion());
    }

    /**
     *  下载更新
     */
    public void downloadUpdate(DownloadUpdateCallback downloadUpdateCallback)
    {
        this.downloadUpdateCallback = downloadUpdateCallback;
        totalFiles = 0;
        fileDownloaded = 0;
        UpdatesLoader.downloadUpdate();
    }

    /**
     *  安装更新
     */
    public void installUpdate(InstallUpdateCallback installUpdateCallback)
    {
        this.installUpdateCallback = installUpdateCallback;
        UpdatesInstaller.install(context, hcpInternalPrefs.getReadyForInstallationReleaseVersionName(), hcpInternalPrefs.getCurrentReleaseVersionName());
    }




    /**
     * 是否需要安装www文件夹
     *
     * @return <code>true</code> 需要; <code>false</code> - otherwise
     */
    private boolean isWWwFolderNeedsToBeInstalled()
    {
        boolean isWwwFolderExists = isWwwFolderExists();
        boolean isWwwFolderInstalled = hcpInternalPrefs.isWwwFolderInstalled();
        boolean isApplicationHasBeenUpdated = isApplicationHasBeenUpdated();

        return !isWwwFolderExists || !isWwwFolderInstalled || isApplicationHasBeenUpdated;
    }

    private boolean isWwwFolderExists() {
        return new File(fileStructure.getWwwFolder()).exists();
    }

    private boolean isApplicationHasBeenUpdated() {
        return hcpInternalPrefs.getAppBuildVersion() != VersionHelper.applicationVersionCode(context);
    }

    private void installWwwFolder() {

        // reset www folder installed flag
        if (hcpInternalPrefs.isWwwFolderInstalled()) {
            hcpInternalPrefs.setWwwFolderInstalled(false);
            hcpInternalPrefsStorage.storeInPreference(hcpInternalPrefs);
        }

        AssetsHelper.copyAssetDirectoryToAppDirectory(context.getAssets(), WWW_FOLDER, fileStructure.getWwwFolder());
    }

    /**
     * 检查更新错误
     *
     * @param event event details
     * @see FetchUpdateErrorEvent
     * @see AssetsHelper
     * @see EventBus
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(final FetchUpdateErrorEvent event) {
        fetchUpdateCallback.fetchUpdateCallback(false, event.error());
    }

    /**
     * 检查更新成功
     *
     * @param event event details
     * @see FetchUpdateCompletedEvent
     * @see AssetsHelper
     * @see EventBus
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(final FetchUpdateCompletedEvent event) {
        ApplicationConfig newAppConfig = event.applicationConfig();
        if (newAppConfig.getContentConfig().getUpdateTime() == UpdateTime.FORCED)
        {
            //强制更新
            fetchUpdateCallback.fetchUpdateCallback(true, null);
        }
        else
        {
            //静默更新
            fetchUpdateCallback.fetchUpdateCallback(false, null);
            downloadUpdateCallback = null;
            installUpdateCallback = null;
            downloadUpdate(null);
        }
    }

    /**
     * www文件夹安装到外部成功
     *
     * @param event event details
     * @see AssetsInstalledEvent
     * @see AssetsHelper
     * @see EventBus
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(final AssetsInstalledEvent event) {
        // update stored application version
        hcpInternalPrefs.setAppBuildVersion(VersionHelper.applicationVersionCode(context));
        hcpInternalPrefs.setWwwFolderInstalled(true);
        hcpInternalPrefsStorage.storeInPreference(hcpInternalPrefs);
    }

    /**
     * www文件夹安装到外部失败
     *
     * @param event event details
     * @see AssetsInstallationErrorEvent
     * @see AssetsHelper
     * @see EventBus
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(AssetsInstallationErrorEvent event) {
        Log.d("HCP", "Can't install assets on device. Continue to work with default bundle");
    }

    /**
     * 下载更新成功
     *
     * @param event event information
     * @see EventBus
     * @see UpdateIsReadyToInstallEvent
     * @see UpdatesLoader
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UpdateIsReadyToInstallEvent event) {
        final ContentConfig newContentConfig = event.applicationConfig().getContentConfig();
        Log.d("HCP", "Update is ready for installation: " + newContentConfig.getReleaseVersion());

        hcpInternalPrefs.setReadyForInstallationReleaseVersionName(newContentConfig.getReleaseVersion());
        hcpInternalPrefsStorage.storeInPreference(hcpInternalPrefs);

        if (downloadUpdateCallback != null)
        {
            downloadUpdateCallback.downloadUpdateCallback(true, totalFiles, fileDownloaded, null);
        }
        else
        {
            installUpdateCallback = null;
            installUpdate(null);
        }
    }

    /**
     * 下载进度
     *
     * @param event event information
     * @see EventBus
     * @see DownloadProgressEvent
     * @see UpdatesLoader
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(DownloadProgressEvent event) {
        totalFiles = event.totalFiles();
        fileDownloaded = event.fileDownloaded();
        if (downloadUpdateCallback != null)
        {
            downloadUpdateCallback.downloadUpdateCallback(false, totalFiles, fileDownloaded, null);
        }
    }

    /**
     * 下载更新过程中出错
     *
     * @param event event information
     * @see EventBus
     * @see UpdateDownloadErrorEvent
     * @see UpdatesLoader
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UpdateDownloadErrorEvent event) {
        Log.d("HCP", "Failed to update");
        final HCPError error = event.error();
        if (downloadUpdateCallback != null)
        {
            downloadUpdateCallback.downloadUpdateCallback(false, totalFiles, fileDownloaded, error);
        }


//        if (error == HCPError.LOCAL_VERSION_OF_APPLICATION_CONFIG_NOT_FOUND || error == HCPError.LOCAL_VERSION_OF_MANIFEST_NOT_FOUND) {
//            Log.d("HCP", "Can't load application config from installation folder. Reinstalling external folder");
//            installWwwFolder();
//        }
//
//        rollbackIfCorrupted(event.error());
    }

    /**
     * 无需下载更新
     *
     * @param event event information
     * @see EventBus
     * @see NothingToUpdateEvent
     * @see UpdatesLoader
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(NothingToUpdateEvent event) {
        Log.d("HCP", "Nothing to update");
    }

//    private void onAssetsInstalledOnExternalStorageEvent()
//    {
//        hcpInternalPrefs.setAppBuildVersion(VersionHelper.applicationVersionCode(context));
//        hcpInternalPrefs.setWwwFolderInstalled(true);
//        hcpInternalPrefsStorage.storeInPreference(hcpInternalPrefs);
//    }

//    private void onUpdateIsReadyForInstallation(ApplicationConfig applicationConfig)
//    {
//        final ContentConfig newContentConfig = applicationConfig.getContentConfig();
//        Log.d("HCP", "Update is ready for installation: " + newContentConfig.getReleaseVersion());
//
//        hcpInternalPrefs.setReadyForInstallationReleaseVersionName(newContentConfig.getReleaseVersion());
//        hcpInternalPrefsStorage.storeInPreference(hcpInternalPrefs);
//    }

//    private void onNothingToUpdateEvent()
//    {
//        Log.d("HCP", "Nothing to update");
//    }

    /**
     * 即将开始安装
     *
     * @param event event information
     * @see EventBus
     * @see BeforeInstallEvent
     * @see UpdatesLoader
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(BeforeInstallEvent event) {
        Log.d("HCP", "Dispatching Before install event");
    }


    /**
     * 安装更新成功
     *
     * @param event event information
     * @see EventBus
     * @see UpdateInstalledEvent
     * @see UpdatesInstaller
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UpdateInstalledEvent event) {
        Log.d("HCP", "Update is installed");

        final ContentConfig newContentConfig = event.applicationConfig().getContentConfig();

        // update preferences
        hcpInternalPrefs.setPreviousReleaseVersionName(hcpInternalPrefs.getCurrentReleaseVersionName());
        hcpInternalPrefs.setCurrentReleaseVersionName(newContentConfig.getReleaseVersion());
        hcpInternalPrefs.setReadyForInstallationReleaseVersionName("");
        hcpInternalPrefsStorage.storeInPreference(hcpInternalPrefs);

        fileStructure = new HCPFilesStructure(context, newContentConfig.getReleaseVersion());

        if (installUpdateCallback != null)
        {
            installUpdateCallback.installUpdateCallback(true, null);
        }
    }

    /**
     * 安装更新出错
     *
     * @param event event information
     * @see UpdateInstallationErrorEvent
     * @see EventBus
     * @see UpdatesInstaller
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UpdateInstallationErrorEvent event) {
        Log.d("HCP", "Failed to install");
        installUpdateCallback.installUpdateCallback(false, event.error());
        rollbackIfCorrupted(event.error());
    }

    /**
     * 无需安装更新
     *
     * @param event event information
     * @see NothingToInstallEvent
     * @see UpdatesInstaller
     * @see EventBus
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(NothingToInstallEvent event) {
        Log.d("HCP", "Nothing to install");
    }


    private void rollbackIfCorrupted(HCPError error) {
        if (error != HCPError.LOCAL_VERSION_OF_APPLICATION_CONFIG_NOT_FOUND &&
                error != HCPError.LOCAL_VERSION_OF_MANIFEST_NOT_FOUND) {
            return;
        }

        if (hcpInternalPrefs.getPreviousReleaseVersionName().length() > 0) {
            Log.d("HCP", "Current release is corrupted, trying to rollback to the previous one");
            rollbackToPreviousRelease();
        } else {
            Log.d("HCP", "Current release is corrupted, reinstalling www folder from assets");
            installWwwFolder();
        }
    }

    private void rollbackToPreviousRelease() {
        hcpInternalPrefs.setCurrentReleaseVersionName(hcpInternalPrefs.getPreviousReleaseVersionName());
        hcpInternalPrefs.setPreviousReleaseVersionName("");
        hcpInternalPrefs.setReadyForInstallationReleaseVersionName("");
        hcpInternalPrefsStorage.storeInPreference(hcpInternalPrefs);

        fileStructure.switchToRelease(hcpInternalPrefs.getCurrentReleaseVersionName());
    }
}
