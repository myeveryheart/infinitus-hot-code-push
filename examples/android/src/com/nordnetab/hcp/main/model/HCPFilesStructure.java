package com.nordnetab.hcp.main.model;

import android.content.Context;

import com.nordnetab.hcp.main.utils.Paths;

/**
 * Created by M on 16/9/9.
 * <p/>
 * 文件结构
 */
public class HCPFilesStructure {

    /**
     * config文件的名字
     */
    public static final String CONFIG_FILE_NAME = "hcp.json";

    /**
     * manifest文件的名字
     */
    public static final String MANIFEST_FILE_NAME = "hcp.manifest";

    private static final String HCP_FOLDER = "hot-code-push";

    private static final String MAIN_CONTENT_FOLDER = "www";
    private static final String DOWNLOAD_FOLDER = "update";

    private String contentFolder;
    private String wwwFolder;
    private String downloadFolder;

    /**
     * 获取更新的根目录
     *
     * @param context application context
     * @return 更新的根目录
     */
    public static String getHCPRootFolder(final Context context) {
        return Paths.get(context.getFilesDir().getAbsolutePath(), HCP_FOLDER);
        //return Paths.get(Environment.getExternalStorageDirectory().getAbsolutePath(), HCP_FOLDER);
    }

    /**
     * Constructor.
     *
     * @param context        application context
     * @param releaseVersion 文件版本
     */
    public HCPFilesStructure(final Context context, final String releaseVersion) {
        // uncomment this line, if you want store files on sdcard instead of application file directory
        //contentFolder = Paths.get(Environment.getExternalStorageDirectory().getAbsolutePath(), HCP_FOLDER, releaseVersion);
        contentFolder = Paths.get(context.getFilesDir().getAbsolutePath(), HCP_FOLDER, releaseVersion);
    }

    /**
     * 切换版本
     *
     * @param releaseVersion 切换到哪个版本
     */
    public void switchToRelease(final String releaseVersion) {
        int idx = contentFolder.lastIndexOf("/");
        contentFolder = Paths.get(contentFolder.substring(0, idx), releaseVersion);

        // reset values
        wwwFolder = null;
        downloadFolder = null;
    }

    /**
     * 获取更新目录的绝对路径
     *
     * @return 更新目录的绝对路径
     */
    public String getContentFolder() {
        return contentFolder;
    }

    /**
     * 获取web文件在外部存储的绝对路径
     *
     * @return web文件在外部存储的绝对路径
     */
    public String getWwwFolder() {
        if (wwwFolder == null) {
            wwwFolder = Paths.get(getContentFolder(), MAIN_CONTENT_FOLDER);
        }

        return wwwFolder;
    }

    /**
     * 获取存储下载文件的文件夹绝对路径
     *
     * @return 存储下载文件的文件夹绝对路径
     */
    public String getDownloadFolder() {
        if (downloadFolder == null) {
            downloadFolder = Paths.get(getContentFolder(), DOWNLOAD_FOLDER);
        }

        return downloadFolder;
    }

}
