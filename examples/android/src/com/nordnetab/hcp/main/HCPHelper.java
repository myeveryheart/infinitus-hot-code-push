package com.nordnetab.hcp.main;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.nordnetab.hcp.main.config.ApplicationConfig;
import com.nordnetab.hcp.main.config.Config;
import com.nordnetab.hcp.main.config.ContentConfig;
import com.nordnetab.hcp.main.config.HCPInternalPreferences;
import com.nordnetab.hcp.main.events.AssetsInstallationErrorEvent;
import com.nordnetab.hcp.main.events.AssetsInstalledEvent;
import com.nordnetab.hcp.main.events.BeforeAssetsInstalledEvent;
import com.nordnetab.hcp.main.events.BeforeInstallEvent;
import com.nordnetab.hcp.main.events.FetchUpdateCompletedEvent;
import com.nordnetab.hcp.main.events.FetchUpdateErrorEvent;
import com.nordnetab.hcp.main.events.NothingToInstallEvent;
import com.nordnetab.hcp.main.events.NothingToUpdateEvent;
import com.nordnetab.hcp.main.events.UpdateDownloadErrorEvent;
import com.nordnetab.hcp.main.events.UpdateInstallationErrorEvent;
import com.nordnetab.hcp.main.events.UpdateInstalledEvent;
import com.nordnetab.hcp.main.events.UpdateIsReadyToInstallEvent;
import com.nordnetab.hcp.main.events.WorkerEvent;
import com.nordnetab.hcp.main.model.HCPError;
import com.nordnetab.hcp.main.model.HCPFilesStructure;
import com.nordnetab.hcp.main.model.UpdateTime;
import com.nordnetab.hcp.main.storage.ApplicationConfigStorage;
import com.nordnetab.hcp.main.storage.IObjectFileStorage;
import com.nordnetab.hcp.main.storage.IObjectPreferenceStorage;
import com.nordnetab.hcp.main.storage.HCPInternalPreferencesStorage;
import com.nordnetab.hcp.main.updater.UpdatesInstaller;
import com.nordnetab.hcp.main.updater.UpdatesLoader;
import com.nordnetab.hcp.main.utils.AssetsHelper;
import com.nordnetab.hcp.main.utils.CleanUpHelper;
import com.nordnetab.hcp.main.utils.Paths;
import com.nordnetab.hcp.main.utils.VersionHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by M on 16/9/9.
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

    private static final String WWW_FOLDER = "www";
    private static final String LOCAL_ASSETS_FOLDER = "file:///android_asset/www";

    private IObjectFileStorage<ApplicationConfig> appConfigStorage;
    private HCPInternalPreferences hcpInternalPrefs;
    private IObjectPreferenceStorage<HCPInternalPreferences> hcpInternalPrefsStorage;
    private Config config;
    private HCPFilesStructure fileStructure;

//    private Handler handler;

    private String webUrl;
    private static Context context;
    private static HCPHelper helper;
//    private HCPResult hcpResult;
    private FetchUpdateCallback fetchUpdateCallback;
    private DownloadUpdateCallback downloadUpdateCallback;

    private static final int FETCH_UPDATE_ERROR_EVENT = 1;
    private static final int FETCH_UPDATE = 2;
    private static Handler handler = new Handler();

//    private Handler handler = new Handler() {
//
//        // 处理子线程给我们发送的消息。
//        @Override
//        public void handleMessage(Message message) {
////            byte[] data = (byte[])msg.obj;
////            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
////            imageView.setImageBitmap(bitmap);
////            if(msg.what == DOWNLOAD_IMG){
////                dialog.dismiss();
////            }
//            WorkerEvent event = (WorkerEvent)message.obj;
//            if (FetchUpdateErrorEvent.class.isInstance(event))
//            {
//                fetchUpdateCallback.fetchUpdateCallback(false, event.error());
//            }
//            if (FetchUpdateCompletedEvent.class.isInstance(event))
//            {
//                fetchUpdateCallback.fetchUpdateCallback(true, null);
//            }
//        }
//    };


    public static HCPHelper getInstance(Context ctx)
    {
        if (helper == null)
        {
            helper = new HCPHelper();
            context = ctx;
            final EventBus eventBus = EventBus.getDefault();
            if (!eventBus.isRegistered(helper)) {
                eventBus.register(helper);
            }
        }

        return helper;
    }

    public void setWebUrl(String webUrl)
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

        handler = new Handler();
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
     *  检查更新
     */
    public void fetchUpdate(FetchUpdateCallback fetchUpdateCallback)
    {
        this.fetchUpdateCallback = fetchUpdateCallback;
        final HCPFilesStructure currentReleaseFS = new HCPFilesStructure(context, hcpInternalPrefs.getCurrentReleaseVersionName());
        UpdatesLoader.fetchUpdate(config.getConfigUrl(), currentReleaseFS, config.getNativeInterfaceVersion());
//        if (error != HCPError.NONE)
//        {
//            fetchUpdateCallback.fetchUpdateCallback(false, error);
////            hcpResult.fetchUpdateResult(false, error);
//        }
//        else {
//            fetchUpdateCallback.fetchUpdateCallback(true, null);
////            hcpResult.fetchUpdateResult(true, null);
//        }
    }

    /**
     *  下载更新
     */
    public void downloadUpdate()
    {
        UpdatesLoader.downloadUpdate();
    }

    /**
     * 安装更新
     */
    private void installUpdate()
    {
        HCPError error = UpdatesInstaller.install(context, hcpInternalPrefs.getReadyForInstallationReleaseVersionName(), hcpInternalPrefs.getCurrentReleaseVersionName());
//        if (error != HCPError.NONE)
//        {
//            hcpResult.fetchUpdateResult(false, error);
//        }
//        else {
//            hcpResult.fetchUpdateResult(true, null);
//        }
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
    @SuppressWarnings("unused")
    @Subscribe
    public void onEvent(final FetchUpdateErrorEvent event) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                fetchUpdateCallback.fetchUpdateCallback(false, event.error());
            }
        });
    }

    /**
     * 检查更新成功
     *
     * @param event event details
     * @see FetchUpdateCompletedEvent
     * @see AssetsHelper
     * @see EventBus
     */
    @SuppressWarnings("unused")
    @Subscribe
    public void onEvent(final FetchUpdateCompletedEvent event) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                fetchUpdateCallback.fetchUpdateCallback(true, null);
            }
        });
    }



    /**
     * www文件夹安装到外部成功
     *
     * @param event event details
     * @see AssetsInstalledEvent
     * @see AssetsHelper
     * @see EventBus
     */
    @SuppressWarnings("unused")
    @Subscribe
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
    @SuppressWarnings("unused")
    @Subscribe
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
    @SuppressWarnings("unused")
    @Subscribe
    public void onEvent(UpdateIsReadyToInstallEvent event) {
        final ContentConfig newContentConfig = event.applicationConfig().getContentConfig();
        Log.d("HCP", "Update is ready for installation: " + newContentConfig.getReleaseVersion());

        hcpInternalPrefs.setReadyForInstallationReleaseVersionName(newContentConfig.getReleaseVersion());
        hcpInternalPrefsStorage.storeInPreference(hcpInternalPrefs);
        handler.post(new Runnable() {
            @Override
            public void run() {
                downloadUpdateCallback.downloadUpdateCallback();
            }
        });
    }

    /**
     * 无需下载更新
     *
     * @param event event information
     * @see EventBus
     * @see NothingToUpdateEvent
     * @see UpdatesLoader
     */
    @SuppressWarnings("unused")
    @Subscribe
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
    @SuppressWarnings("unused")
    @Subscribe
    public void onEvent(BeforeInstallEvent event) {
        Log.d("HCP", "Dispatching Before install event");
    }

    /**
     * 下载更新过程中出错
     *
     * @param event event information
     * @see EventBus
     * @see UpdateDownloadErrorEvent
     * @see UpdatesLoader
     */
    @SuppressWarnings("unused")
    @Subscribe
    public void onEvent(UpdateDownloadErrorEvent event) {
        Log.d("HCP", "Failed to update");

        final HCPError error = event.error();
        if (error == HCPError.LOCAL_VERSION_OF_APPLICATION_CONFIG_NOT_FOUND || error == HCPError.LOCAL_VERSION_OF_MANIFEST_NOT_FOUND) {
            Log.d("HCP", "Can't load application config from installation folder. Reinstalling external folder");
            installWwwFolder();
        }

        rollbackIfCorrupted(event.error());
    }

    /**
     * 安装更新成功
     *
     * @param event event information
     * @see EventBus
     * @see UpdateInstalledEvent
     * @see UpdatesInstaller
     */
    @SuppressWarnings("unused")
    @Subscribe
    public void onEvent(UpdateInstalledEvent event) {
        Log.d("HCP", "Update is installed");

        final ContentConfig newContentConfig = event.applicationConfig().getContentConfig();

        // update preferences
        hcpInternalPrefs.setPreviousReleaseVersionName(hcpInternalPrefs.getCurrentReleaseVersionName());
        hcpInternalPrefs.setCurrentReleaseVersionName(newContentConfig.getReleaseVersion());
        hcpInternalPrefs.setReadyForInstallationReleaseVersionName("");
        hcpInternalPrefsStorage.storeInPreference(hcpInternalPrefs);

        fileStructure = new HCPFilesStructure(context, newContentConfig.getReleaseVersion());
    }

//    private void onUpdateDownloadErrorEvent(UpdateDownloadErrorEvent event)
//    {
//        Log.d("HCP", "Failed to update");
//
//        final HCPError error = event.error();
//        if (error == HCPError.LOCAL_VERSION_OF_APPLICATION_CONFIG_NOT_FOUND || error == HCPError.LOCAL_VERSION_OF_MANIFEST_NOT_FOUND) {
//            Log.d("HCP", "Can't load application config from installation folder. Reinstalling external folder");
//            installWwwFolder();
//        }
//        rollbackIfCorrupted(event.error());
//    }

//    private void onUpdateInstalledEvent(ApplicationConfig applicationConfig)
//    {
//        Log.d("HCP", "Update is installed");
//
//        final ContentConfig newContentConfig = applicationConfig.getContentConfig();
//
//        // update preferences
//        hcpInternalPrefs.setPreviousReleaseVersionName(hcpInternalPrefs.getCurrentReleaseVersionName());
//        hcpInternalPrefs.setCurrentReleaseVersionName(newContentConfig.getReleaseVersion());
//        hcpInternalPrefs.setReadyForInstallationReleaseVersionName("");
//        hcpInternalPrefsStorage.storeInPreference(hcpInternalPrefs);
//
//        fileStructure = new HCPFilesStructure(context, newContentConfig.getReleaseVersion());
//    }

//    private void onUpdateInstallationErrorEvent()
//    {
//
//    }

    /**
     * 安装更新出错
     *
     * @param event event information
     * @see UpdateInstallationErrorEvent
     * @see EventBus
     * @see UpdatesInstaller
     */
    @SuppressWarnings("unused")
    @Subscribe
    public void onEvent(UpdateInstallationErrorEvent event) {
        Log.d("HCP", "Failed to install");

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
    @SuppressWarnings("unused")
    @Subscribe
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
