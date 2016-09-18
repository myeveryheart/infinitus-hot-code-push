package com.nordnetab.hcp.main.storage;

import com.nordnetab.hcp.main.config.ApplicationConfig;
import com.nordnetab.hcp.main.model.HCPFilesStructure;
import com.nordnetab.hcp.main.utils.Paths;

/**
 * Created by M on 16/9/9.
 * <p/>
 * 从文件夹读取和保存配置的工具类
 *
 * @see ApplicationConfig
 * @see IObjectFileStorage
 */
public class ApplicationConfigStorage extends FileStorageAbs<ApplicationConfig> {

    private final String fileName;

    /**
     * Class constructor
     */
    public ApplicationConfigStorage() {
        fileName = HCPFilesStructure.CONFIG_FILE_NAME;
    }

    @Override
    protected ApplicationConfig createInstance(String json) {
        return ApplicationConfig.fromJson(json);
    }

    @Override
    protected String getFullPathForFileInFolder(String folder) {
        return Paths.get(folder, fileName);
    }
}