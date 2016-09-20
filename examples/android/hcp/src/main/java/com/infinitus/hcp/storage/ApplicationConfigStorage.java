package com.infinitus.hcp.storage;

import com.infinitus.hcp.config.ApplicationConfig;
import com.infinitus.hcp.model.HCPFilesStructure;
import com.infinitus.hcp.utils.Paths;

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