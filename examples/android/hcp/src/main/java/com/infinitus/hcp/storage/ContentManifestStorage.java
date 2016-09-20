package com.infinitus.hcp.storage;

import com.infinitus.hcp.config.ContentManifest;
import com.infinitus.hcp.model.HCPFilesStructure;
import com.infinitus.hcp.utils.Paths;

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
