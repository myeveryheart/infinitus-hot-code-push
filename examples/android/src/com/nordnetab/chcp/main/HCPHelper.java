package com.nordnetab.chcp.main;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.nordnetab.chcp.main.config.ApplicationConfig;
import com.nordnetab.chcp.main.config.ChcpXmlConfig;
import com.nordnetab.chcp.main.config.PluginInternalPreferences;
import com.nordnetab.chcp.main.model.PluginFilesStructure;
import com.nordnetab.chcp.main.storage.ApplicationConfigStorage;
import com.nordnetab.chcp.main.storage.IObjectFileStorage;
import com.nordnetab.chcp.main.storage.IObjectPreferenceStorage;
import com.nordnetab.chcp.main.storage.PluginInternalPreferencesStorage;
import com.nordnetab.chcp.main.utils.AssetsHelper;
import com.nordnetab.chcp.main.utils.CleanUpHelper;
import com.nordnetab.chcp.main.utils.ContextUtil;
import com.nordnetab.chcp.main.utils.VersionHelper;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by M on 16/9/9.
 */
public class HCPHelper {

    private static final String FILE_PREFIX = "file://";
    private static final String WWW_FOLDER = "www";
    private static final String LOCAL_ASSETS_FOLDER = "file:///android_asset/www";

    private IObjectFileStorage<ApplicationConfig> appConfigStorage;
    private PluginInternalPreferences pluginInternalPrefs;
    private IObjectPreferenceStorage<PluginInternalPreferences> pluginInternalPrefsStorage;
    private ChcpXmlConfig chcpXmlConfig;
    private PluginFilesStructure fileStructure;

    private Handler handler;
    private boolean isPluginReadyForWork;
    private boolean dontReloadOnStart;

    private URL webUrl;
    private static Context context;
    private static HCPHelper helper;

    public static HCPHelper instance(Context ctx)
    {
        if (helper == null)
        {
            helper = new HCPHelper();
            context = ctx;
        }

        return helper;
    }



    /**
     *  初始化
     *
     *  @param webUrl 远程目录的url
     *
     *  @return HCPHelper
     */
    public HCPHelper initWithWebUrl(URL webUrl)
    {
//        HCPHelper helper = new HCPHelper();
        helper.webUrl = webUrl;
        helper.context = ContextUtil.getInstance();

        doLocalInit();
        Log.d("CHCP", "Currently running release version " + pluginInternalPrefs.getCurrentReleaseVersionName());

        // clean up file system
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

        return helper;
    }

    private void doLocalInit()
    {
        // 初始化config
        chcpXmlConfig = ChcpXmlConfig.loadFromConfig();
        chcpXmlConfig.setWebUrl(webUrl.toString());
        // 加载internal preferences
        pluginInternalPrefsStorage = new PluginInternalPreferencesStorage(context);
        PluginInternalPreferences config = pluginInternalPrefsStorage.loadFromPreference();
        if (config == null || TextUtils.isEmpty(config.getCurrentReleaseVersionName())) {
            config = PluginInternalPreferences.createDefault(context);
            pluginInternalPrefsStorage.storeInPreference(config);
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

    public void fetchUpdate()
    {
        fetchUpdateResult(true, null);
    }

    public void fetchUpdateResult(boolean needUpdate, Error error)
    {

    }

    private boolean isWWwFolderNeedsToBeInstalled()
    {
        boolean isWwwFolderExists = isWwwFolderExists();
        boolean isWwwFolderInstalled = pluginInternalPrefs.isWwwFolderInstalled();
        boolean isApplicationHasBeenUpdated = isApplicationHasBeenUpdated();

        return !isWwwFolderExists && !isWwwFolderInstalled && isApplicationHasBeenUpdated;
    }

    /**
     * Check if external version of www folder exists.
     *
     * @return <code>true</code> if it is in place; <code>false</code> - otherwise
     */
    private boolean isWwwFolderExists() {
        return new File(fileStructure.getWwwFolder()).exists();
    }

    /**
     * Check if application has been updated through the Google Play since the last launch.
     *
     * @return <code>true</code> if application was update; <code>false</code> - otherwise
     */
    private boolean isApplicationHasBeenUpdated() {
        return pluginInternalPrefs.getAppBuildVersion() != VersionHelper.applicationVersionCode(context);
    }

    /**
     * Install assets folder onto the external storage
     */
    private void installWwwFolder()
    {
        // reset www folder installed flag
        if (pluginInternalPrefs.isWwwFolderInstalled()) {
            pluginInternalPrefs.setWwwFolderInstalled(false);
            pluginInternalPrefsStorage.storeInPreference(pluginInternalPrefs);
        }

        AssetsHelper.copyAssetDirectoryToAppDirectory(context.getAssets(), WWW_FOLDER, fileStructure.getWwwFolder());
    }
}
