package com.infinitus.hcp.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.infinitus.hcp.model.HCPFilesStructure;

import java.io.File;

/**
 * Created by M on 16/9/9.
 * <p/>
 * 清理工具类
 */
public class CleanUpHelper {

    private static boolean isExecuting;

    private final File rootFolder;

    /**
     * Constructor.
     *
     * @param rootFolder 根目录
     */
    private CleanUpHelper(final String rootFolder) {
        this.rootFolder = new File(rootFolder);
    }

    /**
     * 删除根目录
     *
     * @param context          application context
     * @param excludedReleases 排除版本
     */
    public static void removeReleaseFolders(final Context context, final String[] excludedReleases) {
        if (isExecuting) {
            return;
        }
        isExecuting = true;

        final String rootFolder = HCPFilesStructure.getHCPRootFolder(context);

        new Thread(new Runnable() {
            @Override
            public void run() {
                new CleanUpHelper(rootFolder).removeFolders(excludedReleases);
                isExecuting = false;
            }
        }).start();
    }

    private void removeFolders(final String[] excludedReleases) {
        if (!rootFolder.exists()) {
            return;
        }

        File[] files = rootFolder.listFiles();
        for (File file : files) {
            boolean isIgnored = false;
            for (String excludedReleaseName : excludedReleases) {
                if (TextUtils.isEmpty(excludedReleaseName)) {
                    continue;
                }

                if (file.getName().equals(excludedReleaseName)) {
                    isIgnored = true;
                    break;
                }
            }

            if (!isIgnored) {
                Log.d("HCP", "Deleting old release folder: " + file.getName());
                FilesUtility.delete(file);
            }
        }
    }

}
