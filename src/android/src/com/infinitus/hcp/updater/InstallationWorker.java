package com.infinitus.hcp.updater;

import android.content.Context;

import com.infinitus.hcp.config.ApplicationConfig;
import com.infinitus.hcp.config.ContentManifest;
import com.infinitus.hcp.events.UpdateInstallationErrorEvent;
import com.infinitus.hcp.events.UpdateInstalledEvent;
import com.infinitus.hcp.events.WorkerEvent;
import com.infinitus.hcp.model.HCPError;
import com.infinitus.hcp.model.ManifestDiff;
import com.infinitus.hcp.model.ManifestFile;
import com.infinitus.hcp.model.HCPFilesStructure;
import com.infinitus.hcp.storage.ApplicationConfigStorage;
import com.infinitus.hcp.storage.ContentManifestStorage;
import com.infinitus.hcp.storage.IObjectFileStorage;
import com.infinitus.hcp.utils.FilesUtility;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by M on 16/9/9.
 * <p/>
 * 安装类
 */
class InstallationWorker implements WorkerTask {

    private ManifestDiff manifestDiff;
    private ApplicationConfig newAppConfig;

    private HCPFilesStructure newReleaseFS;
    private HCPFilesStructure currentReleaseFS;

    private WorkerEvent resultEvent;

    /**
     * Constructor.
     *
     * @param context        application context
     * @param newVersion     新版版本号
     * @param currentVersion 现在版本号
     */
    public InstallationWorker(final Context context, final String newVersion, final String currentVersion) {
        newReleaseFS = new HCPFilesStructure(context, newVersion);
        currentReleaseFS = new HCPFilesStructure(context, currentVersion);
    }

    public void install()
    {
        if (!init()) {
            return;
        }

        // 验证更新
        if (!isUpdateValid(newReleaseFS.getDownloadFolder(), manifestDiff)) {
            setResultForError(HCPError.UPDATE_IS_INVALID);
            return;
        }

        // 拷贝旧的到新的
        if (!copyFilesFromCurrentReleaseToNewRelease()) {
            setResultForError(HCPError.FAILED_TO_COPY_FILES_FROM_PREVIOUS_RELEASE);
            return;
        }

        // 删除旧的 manifest 文件
        deleteUnusedFiles();

        // 安装更新文件
        boolean isInstalled = moveFilesFromInstallationFolderToWwwFodler();
        if (!isInstalled) {
            cleanUpOnFailure();
            setResultForError(HCPError.FAILED_TO_COPY_NEW_CONTENT_FILES);
            return;
        }

        // 清理
        cleanUpOnSuccess();

        // 发送成功消息
        setSuccessResult();
    }

    /**
     * 初始化
     *
     * @return <code>true</code> 成功; <code>false</code> - otherwise
     */
    private boolean init() {
        // loaded application config
        IObjectFileStorage<ApplicationConfig> appConfigStorage = new ApplicationConfigStorage();
        newAppConfig = appConfigStorage.loadFromFolder(newReleaseFS.getDownloadFolder());
        if (newAppConfig == null) {
            setResultForError(HCPError.LOADED_VERSION_OF_APPLICATION_CONFIG_NOT_FOUND);
            return false;
        }

        // old manifest file
        IObjectFileStorage<ContentManifest> manifestStorage = new ContentManifestStorage();
        ContentManifest oldManifest = manifestStorage.loadFromFolder(currentReleaseFS.getWwwFolder());
        if (oldManifest == null) {
            setResultForError(HCPError.LOCAL_VERSION_OF_MANIFEST_NOT_FOUND);
            return false;
        }

        // loaded manifest file
        ContentManifest newManifest = manifestStorage.loadFromFolder(newReleaseFS.getDownloadFolder());
        if (newManifest == null) {
            setResultForError(HCPError.LOADED_VERSION_OF_MANIFEST_NOT_FOUND);
            return false;
        }

        // difference between old and the new manifest files
        manifestDiff = oldManifest.calculateDifference(newManifest);

        return true;
    }

    /**
     * 拷贝旧版文件到新版文件夹
     *
     * @return <code>true</code> 拷贝成; <code>false</code> - otherwise.
     */
    private boolean copyFilesFromCurrentReleaseToNewRelease() {
        boolean result = true;
        final File currentWwwFolder = new File(currentReleaseFS.getWwwFolder());
        final File newWwwFolder = new File(newReleaseFS.getWwwFolder());
        try {
            // just in case if www folder already exists - remove it
            if (newWwwFolder.exists()) {
                FilesUtility.delete(newWwwFolder);
            }

            FilesUtility.copy(currentWwwFolder, newWwwFolder);
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }

        return result;
    }

    /**
     * 安装更新失败时清除
     */
    private void cleanUpOnFailure() {
        FilesUtility.delete(newReleaseFS.getContentFolder());
    }

    /**
     * 安装更新成功时清除
     */
    private void cleanUpOnSuccess() {
        FilesUtility.delete(newReleaseFS.getDownloadFolder());
    }

    /**
     * 删除无用文件
     */
    private void deleteUnusedFiles() {
        final List<ManifestFile> files = manifestDiff.deletedFiles();
        for (ManifestFile file : files) {
            File fileToDelete = new File(newReleaseFS.getWwwFolder(), file.name);
            FilesUtility.delete(fileToDelete);
        }
    }

    /**
     * 拷贝更新文件到www
     *
     * @return <code>true</code> 拷贝成功; <code>false</code> - otherwise
     */
    private boolean moveFilesFromInstallationFolderToWwwFodler() {
        try {
            FilesUtility.copy(newReleaseFS.getDownloadFolder(), newReleaseFS.getWwwFolder());

            return true;
        } catch (IOException e) {
            e.printStackTrace();

            return false;
        }
    }

    /**
     * 检查更新文件是否已准备好
     *
     * @param downloadFolderPath 更新文件夹路径
     * @param manifestDiff       manifest差异
     * @return <code>true</code> 准备好了; <code>false</code> - otherwise
     */
    private boolean isUpdateValid(String downloadFolderPath, ManifestDiff manifestDiff) {
        File downloadFolder = new File(downloadFolderPath);
        if (!downloadFolder.exists()) {
            return false;
        }

        boolean isValid = true;
        List<ManifestFile> updateFileList = manifestDiff.getUpdateFiles();

        for (ManifestFile updatedFile : updateFileList) {
            File file = new File(downloadFolder, updatedFile.name);

            try {
                if (!file.exists() ||
                        !FilesUtility.calculateFileHash(file).equals(updatedFile.hash)) {
                    isValid = false;
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                isValid = false;
                break;
            }
        }

        return isValid;
    }

    // region Events

    private void setResultForError(final HCPError error) {
        resultEvent = new UpdateInstallationErrorEvent(error, newAppConfig);
    }

    private void setSuccessResult() {
        resultEvent = new UpdateInstalledEvent(newAppConfig);
    }

    @Override
    public WorkerEvent result() {
        return resultEvent;
    }

    // endregion
}