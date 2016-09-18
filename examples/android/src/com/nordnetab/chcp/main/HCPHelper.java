package com.nordnetab.chcp.main;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.nordnetab.chcp.main.config.ApplicationConfig;
import com.nordnetab.chcp.main.config.ChcpXmlConfig;
import com.nordnetab.chcp.main.config.ContentConfig;
import com.nordnetab.chcp.main.config.PluginInternalPreferences;
import com.nordnetab.chcp.main.events.AssetsInstallationErrorEvent;
import com.nordnetab.chcp.main.events.AssetsInstalledEvent;
import com.nordnetab.chcp.main.events.BeforeAssetsInstalledEvent;
import com.nordnetab.chcp.main.events.BeforeInstallEvent;
import com.nordnetab.chcp.main.events.NothingToInstallEvent;
import com.nordnetab.chcp.main.events.NothingToUpdateEvent;
import com.nordnetab.chcp.main.events.UpdateDownloadErrorEvent;
import com.nordnetab.chcp.main.events.UpdateInstallationErrorEvent;
import com.nordnetab.chcp.main.events.UpdateInstalledEvent;
import com.nordnetab.chcp.main.events.UpdateIsReadyToInstallEvent;
import com.nordnetab.chcp.main.model.ChcpError;
import com.nordnetab.chcp.main.model.PluginFilesStructure;
import com.nordnetab.chcp.main.model.UpdateTime;
import com.nordnetab.chcp.main.storage.ApplicationConfigStorage;
import com.nordnetab.chcp.main.storage.IObjectFileStorage;
import com.nordnetab.chcp.main.storage.IObjectPreferenceStorage;
import com.nordnetab.chcp.main.storage.PluginInternalPreferencesStorage;
import com.nordnetab.chcp.main.updater.UpdatesInstaller;
import com.nordnetab.chcp.main.updater.UpdatesLoader;
import com.nordnetab.chcp.main.utils.AssetsHelper;
import com.nordnetab.chcp.main.utils.CleanUpHelper;
import com.nordnetab.chcp.main.utils.Paths;
import com.nordnetab.chcp.main.utils.VersionHelper;
import com.nordnetab.chcp.main.view.AppUpdateRequestDialog;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.ConfigXmlParser;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.PluginResult;
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

    private static final String WWW_FOLDER = "www";
    private static final String LOCAL_ASSETS_FOLDER = "file:///android_asset/www";

    private IObjectFileStorage<ApplicationConfig> appConfigStorage;
    private PluginInternalPreferences pluginInternalPrefs;
    private IObjectPreferenceStorage<PluginInternalPreferences> pluginInternalPrefsStorage;
    private ChcpXmlConfig chcpXmlConfig;
    private PluginFilesStructure fileStructure;

    private Handler handler;
    private boolean isPluginReadyForWork;

    private URL webUrl;
    private static Context context;
    private static HCPHelper helper;
    private HCPResult hcpResult;

    public static HCPHelper getInstance(Context ctx)
    {
        if (helper == null)
        {
            helper = new HCPHelper();
            context = ctx;
        }

        return helper;
    }

    public void setListener(HCPResult listener)
    {
        hcpResult = listener;
    }

    public void setWebUrl(URL webUrl)
    {
        this.webUrl = webUrl;
        doLocalInit();
        Log.d("HCP", "Currently running release version " + pluginInternalPrefs.getCurrentReleaseVersionName());

        // 清理文件
        if (!TextUtils.isEmpty(pluginInternalPrefs.getCurrentReleaseVersionName())) {
            CleanUpHelper.removeReleaseFolders(context,
                    new String[]{pluginInternalPrefs.getCurrentReleaseVersionName(),
                            pluginInternalPrefs.getPreviousReleaseVersionName(),
                            pluginInternalPrefs.getReadyForInstallationReleaseVersionName()
                    }
            );
        }

        handler = new Handler();
        fileStructure = new PluginFilesStructure(context, pluginInternalPrefs.getCurrentReleaseVersionName());
        appConfigStorage = new ApplicationConfigStorage();
    }

    private void doLocalInit()
    {
        // 初始化config
        chcpXmlConfig = ChcpXmlConfig.getDefaultConfig();
        chcpXmlConfig.setWebUrl(webUrl.toString());
        // 加载internal preferences
        pluginInternalPrefsStorage = new PluginInternalPreferencesStorage(context);
        PluginInternalPreferences config = pluginInternalPrefsStorage.loadFromPreference();
        if (config == null || TextUtils.isEmpty(config.getCurrentReleaseVersionName())) {
            config = PluginInternalPreferences.createDefault(context);
            if (config.getCurrentReleaseVersionName().length() > 0)
            {
                pluginInternalPrefsStorage.storeInPreference(config);
            }
            else
            {
                Log.d("HCP","无法读取配置文件");
            }
        }
        pluginInternalPrefs = config;
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
    public void fetchUpdate()
    {
        final PluginFilesStructure currentReleaseFS = new PluginFilesStructure(context, pluginInternalPrefs.getCurrentReleaseVersionName());
        final ChcpError error = UpdatesLoader.downloadUpdate(chcpXmlConfig.getConfigUrl(), currentReleaseFS, chcpXmlConfig.getNativeInterfaceVersion());
        if (error != ChcpError.NONE) {
            hcpResult.fetchUpdateResult(false, error);
        }
        else {
            hcpResult.fetchUpdateResult(true, null);
        }
    }

    /**
     *  下载更新
     */
    public void downloadUpdate()
    {
        final PluginFilesStructure currentReleaseFS = new PluginFilesStructure(context, pluginInternalPrefs.getCurrentReleaseVersionName());
        final ChcpError error = UpdatesLoader.downloadUpdate(chcpXmlConfig.getConfigUrl(), currentReleaseFS, chcpXmlConfig.getNativeInterfaceVersion());
        if (error != ChcpError.NONE) {
            hcpResult.fetchUpdateResult(false, error);
        }
        else {
            hcpResult.fetchUpdateResult(true, null);
        }
    }

    /**
     * 安装更新
     */
    private void installUpdate()
    {
        ChcpError error = UpdatesInstaller.install(context, pluginInternalPrefs.getReadyForInstallationReleaseVersionName(), pluginInternalPrefs.getCurrentReleaseVersionName());
        if (error != ChcpError.NONE)
        {
            hcpResult.fetchUpdateResult(false, error);
        }
        else {
            hcpResult.fetchUpdateResult(true, null);
        }
    }

    /**
     * 是否需要安装www文件夹
     *
     * @return <code>true</code> 需要; <code>false</code> - otherwise
     */
    private boolean isWWwFolderNeedsToBeInstalled()
    {
        boolean isWwwFolderExists = isWwwFolderExists();
        boolean isWwwFolderInstalled = pluginInternalPrefs.isWwwFolderInstalled();
        boolean isApplicationHasBeenUpdated = isApplicationHasBeenUpdated();

        return !isWwwFolderExists && !isWwwFolderInstalled && isApplicationHasBeenUpdated;
    }

    private boolean isWwwFolderExists() {
        return new File(fileStructure.getWwwFolder()).exists();
    }

    private boolean isApplicationHasBeenUpdated() {
        return pluginInternalPrefs.getAppBuildVersion() != VersionHelper.applicationVersionCode(context);
    }

    private void installWwwFolder() {
        isPluginReadyForWork = false;

        // reset www folder installed flag
        if (pluginInternalPrefs.isWwwFolderInstalled()) {
            pluginInternalPrefs.setWwwFolderInstalled(false);
            pluginInternalPrefsStorage.storeInPreference(pluginInternalPrefs);
        }

        AssetsHelper.copyAssetDirectoryToAppDirectory(context.getAssets(), WWW_FOLDER, fileStructure.getWwwFolder());
    }

    private void onAssetsInstalledOnExternalStorageEvent()
    {
        pluginInternalPrefs.setAppBuildVersion(VersionHelper.applicationVersionCode(context));
        pluginInternalPrefs.setWwwFolderInstalled(true);
        pluginInternalPrefsStorage.storeInPreference(pluginInternalPrefs);
    }

    private void onUpdateIsReadyForInstallation(ApplicationConfig applicationConfig)
    {
        final ContentConfig newContentConfig = applicationConfig.getContentConfig();
        Log.d("CHCP", "Update is ready for installation: " + newContentConfig.getReleaseVersion());

        pluginInternalPrefs.setReadyForInstallationReleaseVersionName(newContentConfig.getReleaseVersion());
        pluginInternalPrefsStorage.storeInPreference(pluginInternalPrefs);
    }

    private void onNothingToUpdateEvent()
    {
        Log.d("CHCP", "Nothing to update");
    }

    private void onUpdateDownloadErrorEvent(UpdateDownloadErrorEvent event)
    {
        Log.d("CHCP", "Failed to update");

        final ChcpError error = event.error();
        if (error == ChcpError.LOCAL_VERSION_OF_APPLICATION_CONFIG_NOT_FOUND || error == ChcpError.LOCAL_VERSION_OF_MANIFEST_NOT_FOUND) {
            Log.d("CHCP", "Can't load application config from installation folder. Reinstalling external folder");
            installWwwFolder();
        }
        rollbackIfCorrupted(event.error());
    }

    private void onUpdateInstalledEvent(ApplicationConfig applicationConfig)
    {
        Log.d("CHCP", "Update is installed");

        final ContentConfig newContentConfig = applicationConfig.getContentConfig();

        // update preferences
        pluginInternalPrefs.setPreviousReleaseVersionName(pluginInternalPrefs.getCurrentReleaseVersionName());
        pluginInternalPrefs.setCurrentReleaseVersionName(newContentConfig.getReleaseVersion());
        pluginInternalPrefs.setReadyForInstallationReleaseVersionName("");
        pluginInternalPrefsStorage.storeInPreference(pluginInternalPrefs);

        fileStructure = new PluginFilesStructure(context, newContentConfig.getReleaseVersion());
    }

    private void onUpdateInstallationErrorEvent()
    {

    }

    private void rollbackIfCorrupted(ChcpError error) {
        if (error != ChcpError.LOCAL_VERSION_OF_APPLICATION_CONFIG_NOT_FOUND &&
                error != ChcpError.LOCAL_VERSION_OF_MANIFEST_NOT_FOUND) {
            return;
        }

        if (pluginInternalPrefs.getPreviousReleaseVersionName().length() > 0) {
            Log.d("CHCP", "Current release is corrupted, trying to rollback to the previous one");
            rollbackToPreviousRelease();
        } else {
            Log.d("CHCP", "Current release is corrupted, reinstalling www folder from assets");
            installWwwFolder();
        }
    }

    private void rollbackToPreviousRelease() {
        pluginInternalPrefs.setCurrentReleaseVersionName(pluginInternalPrefs.getPreviousReleaseVersionName());
        pluginInternalPrefs.setPreviousReleaseVersionName("");
        pluginInternalPrefs.setReadyForInstallationReleaseVersionName("");
        pluginInternalPrefsStorage.storeInPreference(pluginInternalPrefs);

        fileStructure.switchToRelease(pluginInternalPrefs.getCurrentReleaseVersionName());
    }
}
