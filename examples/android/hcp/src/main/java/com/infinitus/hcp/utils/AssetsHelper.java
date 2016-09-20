package com.infinitus.hcp.utils;

import android.content.res.AssetManager;

import com.infinitus.hcp.events.AssetsInstallationErrorEvent;
import com.infinitus.hcp.events.AssetsInstalledEvent;
import com.infinitus.hcp.events.BeforeAssetsInstalledEvent;

import org.greenrobot.eventbus.EventBus;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by M on 16/9/9.
 * <p/>
 * 把www文件夹安装到外部存储的工具类
 */
public class AssetsHelper {

    private static boolean isWorking;

    private AssetsHelper() {
    }

    /**
     * 把bunlde的www文件夹安装到外部存储
     *
     * @param assetManager  assets manager
     * @param fromDirectory fromDirectory
     * @param toDirectory   toDirectory
     *
     * @see AssetsInstallationErrorEvent
     * @see AssetsInstalledEvent
     * @see EventBus
     */
    public static void copyAssetDirectoryToAppDirectory(final AssetManager assetManager, final String fromDirectory, final String toDirectory) {
        if (isWorking) {
            return;
        }
        isWorking = true;

        // notify, that we are starting assets installation
        EventBus.getDefault().post(new BeforeAssetsInstalledEvent());

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    copyAssetDirectory(assetManager, fromDirectory, toDirectory);
                    EventBus.getDefault().post(new AssetsInstalledEvent());
                } catch (IOException e) {
                    e.printStackTrace();
                    EventBus.getDefault().post(new AssetsInstallationErrorEvent());
                } finally {
                    isWorking = false;
                }
            }
        }).start();
    }

    private static void copyAssetDirectory(AssetManager assetManager, String fromDirectory, String toDirectory) throws IOException {
        // recreate cache folder
        FilesUtility.delete(toDirectory);
        FilesUtility.ensureDirectoryExists(toDirectory);

        // copy files
        String[] files = assetManager.list(fromDirectory);
        for (String file : files) {
            final String destinationFileAbsolutePath = com.infinitus.hcp.utils.Paths.get(toDirectory, file);
            final String assetFileAbsolutePath = Paths.get(fromDirectory, file).substring(1);

            String subFiles[] = assetManager.list(assetFileAbsolutePath);
            if (subFiles.length == 0) {
                copyAssetFile(assetManager, assetFileAbsolutePath, destinationFileAbsolutePath);
            } else {
                copyAssetDirectory(assetManager, assetFileAbsolutePath, destinationFileAbsolutePath);
            }
        }
    }

    /**
     * 拷贝本地www到外部www
     */
    private static void copyAssetFile(AssetManager assetManager, String assetFilePath, String destinationFilePath) throws IOException {
        InputStream in = assetManager.open(assetFilePath);
        OutputStream out = new FileOutputStream(destinationFilePath);

        // Transfer bytes from in to out
        byte[] buf = new byte[8192];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }

        in.close();
        out.close();
    }
}