package com.nordnetab.chcp.main.storage;

import android.text.TextUtils;

import com.nordnetab.chcp.main.utils.FilesUtility;

import java.io.IOException;

/**
 * Created by M on 16/9/9.
 * <p/>
 * 从文件夹读取和保存配置的基类
 *
 * @see IObjectFileStorage
 */
abstract class FileStorageAbs<T> implements IObjectFileStorage<T> {

    /**
     * JSON转对象
     *
     * @param json JSON string
     * @return 对象
     */
    protected abstract T createInstance(String json);

    /**
     * 保存的url
     *
     * @param folder 文件夹url
     * @return 文件url
     */
    protected abstract String getFullPathForFileInFolder(String folder);

    @Override
    public boolean storeInFolder(T object, String folder) {
        final String pathToStorableFile = getFullPathForFileInFolder(folder);
        if (TextUtils.isEmpty(pathToStorableFile)) {
            return false;
        }

        try {
            FilesUtility.writeToFile(object.toString(), pathToStorableFile);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    public T loadFromFolder(String folder) {
        final String pathToStorableFile = getFullPathForFileInFolder(folder);
        if (TextUtils.isEmpty(pathToStorableFile)) {
            return null;
        }

        T result = null;
        try {
            String json = FilesUtility.readFromFile(pathToStorableFile);
            result = createInstance(json);
        } catch (IOException e) {
            //e.printStackTrace();
        }

        return result;
    }
}