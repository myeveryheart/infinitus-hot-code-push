package com.nordnetab.hcp.main.storage;

import com.nordnetab.hcp.main.config.ContentManifest;
import com.nordnetab.hcp.main.model.HCPFilesStructure;
import com.nordnetab.hcp.main.utils.Paths;

/**
 * Created by M on 16/9/9.
 * <p/>
 * 保存和读取manifest文件的工具类
 *
 * @see ContentManifest
 * @see IObjectFileStorage
 */
public class ContentManifestStorage extends FileStorageAbs<ContentManifest> {

    private final String fileName;

    /**
     * Constructor.
     */
    public ContentManifestStorage() {
        fileName = HCPFilesStructure.MANIFEST_FILE_NAME;
    }

    @Override
    protected ContentManifest createInstance(String json) {
        return ContentManifest.fromJson(json);
    }

    @Override
    protected String getFullPathForFileInFolder(String folder) {
        return Paths.get(folder, fileName);
    }
}
