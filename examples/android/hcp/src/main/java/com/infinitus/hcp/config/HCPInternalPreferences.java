package com.infinitus.hcp.config;

import android.content.Context;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.infinitus.hcp.model.HCPFilesStructure;
import com.infinitus.hcp.utils.VersionHelper;

import java.io.IOException;

/**
 * Created by M on 16/9/9.
 * <p/>
 * 更新功能使用的参数
 */
public class HCPInternalPreferences {

    // json keys of the preference
    private static final String APPLICATION_BUILD_VERSION = "app_build_version";
    private static final String WWW_FOLDER_INSTALLED_FLAG = "www_folder_installed";
    private static final String PREVIOUS_RELEASE_VERSION_NAME = "previous_release_version_name";
    private static final String CURRENT_RELEASE_VERSION_NAME = "current_release_version_name";
    private static final String READY_FOR_INSTALLATION_RELEASE_VERSION_NAME = "ready_for_installation_release_version_name";

    private int appBuildVersion;
    private boolean wwwFolderInstalled;
    private String currentReleaseVersionName;
    private String previousReleaseVersionName;
    private String readyForInstallationReleaseVersionName;

    /**
     * 从JSON实例化
     *
     * @param json JSON string
     * @return object instance
     */
    public static HCPInternalPreferences fromJson(final String json) {
        HCPInternalPreferences config = new HCPInternalPreferences();
        try {
            JsonNode jsonNode = new ObjectMapper().readTree(json);
            config.setAppBuildVersion(
                    jsonNode.get(APPLICATION_BUILD_VERSION).asInt()
            );
            config.setWwwFolderInstalled(
                    jsonNode.get(WWW_FOLDER_INSTALLED_FLAG).asBoolean()
            );

            if (jsonNode.has(CURRENT_RELEASE_VERSION_NAME)) {
                config.setCurrentReleaseVersionName(
                        jsonNode.get(CURRENT_RELEASE_VERSION_NAME).asText()
                );
            }

            if (jsonNode.has(PREVIOUS_RELEASE_VERSION_NAME)) {
                config.setPreviousReleaseVersionName(
                        jsonNode.get(PREVIOUS_RELEASE_VERSION_NAME).asText()
                );
            }

            if (jsonNode.has(READY_FOR_INSTALLATION_RELEASE_VERSION_NAME)) {
                config.setReadyForInstallationReleaseVersionName(
                        jsonNode.get(READY_FOR_INSTALLATION_RELEASE_VERSION_NAME).asText()
                );
            }
        } catch (IOException e) {
            e.printStackTrace();

            config = null;
        }

        return config;
    }

    private HCPInternalPreferences() {
        currentReleaseVersionName = "";
        previousReleaseVersionName = "";
        readyForInstallationReleaseVersionName = "";
    }

    /**
     * 创建默认配置
     *
     * @param context context
     * @return 默认配置
     */
    public static HCPInternalPreferences createDefault(final Context context) {
        final HCPInternalPreferences hcpPrefs = new HCPInternalPreferences();
        hcpPrefs.setAppBuildVersion(VersionHelper.applicationVersionCode(context));
        hcpPrefs.setWwwFolderInstalled(false);
        hcpPrefs.setPreviousReleaseVersionName("");
        hcpPrefs.setReadyForInstallationReleaseVersionName("");
        hcpPrefs.setCurrentReleaseVersionName("");

        // read app config from assets to get current release version
        final ApplicationConfig appConfig = ApplicationConfig.configFromAssets(context, HCPFilesStructure.CONFIG_FILE_NAME);
        if (appConfig != null) {
            hcpPrefs.setCurrentReleaseVersionName(appConfig.getContentConfig().getReleaseVersion());
        }

        return hcpPrefs;
    }

    /**
     * 获取app版本号
     *
     * @return app版本号
     */
    public int getAppBuildVersion() {
        return appBuildVersion;
    }

    /**
     * 设置app版本号
     *
     * @param appBuildVersion app版本号
     */
    public void setAppBuildVersion(int appBuildVersion) {
        this.appBuildVersion = appBuildVersion;
    }

    /**
     * www文件夹是否拷贝到外部了
     *
     * @return <code>true</code> - 是; otherwise - <code>false</code>
     */
    public boolean isWwwFolderInstalled() {
        return wwwFolderInstalled;
    }

    /**
     * 设置www文件夹是否拷贝到外部了
     *
     * @param isWwwFolderInstalled is www folder is installed
     */
    public void setWwwFolderInstalled(boolean isWwwFolderInstalled) {
        wwwFolderInstalled = isWwwFolderInstalled;
    }

    /**
     * 获取现在的版本
     *
     * @return 现在的版本
     */
    public String getCurrentReleaseVersionName() {
        return currentReleaseVersionName;
    }

    /**
     * 设置现在的版本
     *
     * @param currentReleaseVersionName 现在的版本
     */
    public void setCurrentReleaseVersionName(final String currentReleaseVersionName) {
        this.currentReleaseVersionName = currentReleaseVersionName;
    }

    /**
     * 获取之前的版本
     *
     * @return 之前的版本
     */
    public String getPreviousReleaseVersionName() {
        return previousReleaseVersionName;
    }

    /**
     * 设置之前的版本
     *
     * @param previousReleaseVersionName 之前的版本
     */
    public void setPreviousReleaseVersionName(String previousReleaseVersionName) {
        this.previousReleaseVersionName = previousReleaseVersionName;
    }

    /**
     * 获取可安装的版本
     *
     * @return 可安装的版本
     */
    public String getReadyForInstallationReleaseVersionName() {
        return readyForInstallationReleaseVersionName;
    }

    /**
     * 设置可安装的版本
     *
     * @param readyForInstallationReleaseVersionName 可安装的版本
     */
    public void setReadyForInstallationReleaseVersionName(String readyForInstallationReleaseVersionName) {
        this.readyForInstallationReleaseVersionName = readyForInstallationReleaseVersionName;
    }

    /**
     * 对象转JSON string
     *
     * @return JSON string
     */
    @Override
    public String toString() {
        JsonNodeFactory nodeFactory = JsonNodeFactory.instance;
        ObjectNode object = nodeFactory.objectNode();
        object.set(APPLICATION_BUILD_VERSION, nodeFactory.numberNode(appBuildVersion));
        object.set(WWW_FOLDER_INSTALLED_FLAG, nodeFactory.booleanNode(wwwFolderInstalled));
        object.set(CURRENT_RELEASE_VERSION_NAME, nodeFactory.textNode(currentReleaseVersionName));
        object.set(PREVIOUS_RELEASE_VERSION_NAME, nodeFactory.textNode(previousReleaseVersionName));
        object.set(READY_FOR_INSTALLATION_RELEASE_VERSION_NAME, nodeFactory.textNode(readyForInstallationReleaseVersionName));

        return object.toString();
    }
}
