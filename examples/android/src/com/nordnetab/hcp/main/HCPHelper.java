package com.nordnetab.hcp.main;

import android.content.Context;
import android.os.Handler;
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
import com.nordnetab.hcp.main.events.NothingToInstallEvent;
import com.nordnetab.hcp.main.events.NothingToUpdateEvent;
import com.nordnetab.hcp.main.events.UpdateDownloadErrorEvent;
import com.nordnetab.hcp.main.events.UpdateInstallationErrorEvent;
import com.nordnetab.hcp.main.events.UpdateInstalledEvent;
import com.nordnetab.hcp.main.events.UpdateIsReadyToInstallEvent;
import com.nordnetab.hcp.main.model.HcpError;
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

    private static final String WWW_FOLDER = "www";
    private static final String LOCAL_ASSETS_FOLDER = "file:///android_asset/www";

    private IObjectFileStorage<ApplicationConfig> appConfigStorage;
    private HCPInternalPreferences hcpInternalPrefs;
    private IObjectPreferenceStorage<HCPInternalPreferences> hcpInternalPrefsStorage;
    private Config config;
    private HCPFilesStructure fileStructure;

    private Handler handler;

    private String webUrl;
    private static Context context;
    private static HCPHelper helper;
//    private HCPResult hcpResult;

    public interface FetchUpdateCallback
    {
        void fetchUpdateCallback(boolean needUpdate, HcpError error);
    }

    public static HCPHelper getInstance(Context ctx)
    {
        if (helper == null)
        {
            helper = new HCPHelper();
            context = ctx;
        }

        return helper;
    }

//    public void setListener(HCPResult listener)
//    {
//        hcpResult = listener;
//    }

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
        final HCPFilesStructure currentReleaseFS = new HCPFilesStructure(context, hcpInternalPrefs.getCurrentReleaseVersionName());
        UpdatesLoader.fetchUpdate(config.getConfigUrl(), currentReleaseFS, config.getNativeInterfaceVersion(), fetchUpdateCallback);
//        if (error != HcpError.NONE)
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
        final HCPFilesStructure currentReleaseFS = new HCPFilesStructure(context, hcpInternalPrefs.getCurrentReleaseVersionName());
//        final HcpError error = UpdatesLoader.downloadUpdate(config.getConfigUrl(), currentReleaseFS, config.getNativeInterfaceVersion());
//        if (error != HcpError.NONE) {
//            hcpResult.fetchUpdateResult(false, error);
//        }
//        else {
//            hcpResult.fetchUpdateResult(true, null);
//        }
    }

    /**
     * 安装更新
     */
    private void installUpdate()
    {
        HcpError error = UpdatesInstaller.install(context, hcpInternalPrefs.getReadyForInstallationReleaseVersionName(), hcpInternalPrefs.getCurrentReleaseVersionName());
//        if (error != HcpError.NONE)
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

    private void onAssetsInstalledOnExternalStorageEvent()
    {
        hcpInternalPrefs.setAppBuildVersion(VersionHelper.applicationVersionCode(context));
        hcpInternalPrefs.setWwwFolderInstalled(true);
        hcpInternalPrefsStorage.storeInPreference(hcpInternalPrefs);
    }

    private void onUpdateIsReadyForInstallation(ApplicationConfig applicationConfig)
    {
        final ContentConfig newContentConfig = applicationConfig.getContentConfig();
        Log.d("HCP", "Update is ready for installation: " + newContentConfig.getReleaseVersion());

        hcpInternalPrefs.setReadyForInstallationReleaseVersionName(newContentConfig.getReleaseVersion());
        hcpInternalPrefsStorage.storeInPreference(hcpInternalPrefs);
    }

    private void onNothingToUpdateEvent()
    {
        Log.d("HCP", "Nothing to update");
    }

    private void onUpdateDownloadErrorEvent(UpdateDownloadErrorEvent event)
    {
        Log.d("HCP", "Failed to update");

        final HcpError error = event.error();
        if (error == HcpError.LOCAL_VERSION_OF_APPLICATION_CONFIG_NOT_FOUND || error == HcpError.LOCAL_VERSION_OF_MANIFEST_NOT_FOUND) {
            Log.d("HCP", "Can't load application config from installation folder. Reinstalling external folder");
            installWwwFolder();
        }
        rollbackIfCorrupted(event.error());
    }

    private void onUpdateInstalledEvent(ApplicationConfig applicationConfig)
    {
        Log.d("HCP", "Update is installed");

        final ContentConfig newContentConfig = applicationConfig.getContentConfig();

        // update preferences
        hcpInternalPrefs.setPreviousReleaseVersionName(hcpInternalPrefs.getCurrentReleaseVersionName());
        hcpInternalPrefs.setCurrentReleaseVersionName(newContentConfig.getReleaseVersion());
        hcpInternalPrefs.setReadyForInstallationReleaseVersionName("");
        hcpInternalPrefsStorage.storeInPreference(hcpInternalPrefs);

        fileStructure = new HCPFilesStructure(context, newContentConfig.getReleaseVersion());
    }

    private void onUpdateInstallationErrorEvent()
    {

    }

    private void rollbackIfCorrupted(HcpError error) {
        if (error != HcpError.LOCAL_VERSION_OF_APPLICATION_CONFIG_NOT_FOUND &&
                error != HcpError.LOCAL_VERSION_OF_MANIFEST_NOT_FOUND) {
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