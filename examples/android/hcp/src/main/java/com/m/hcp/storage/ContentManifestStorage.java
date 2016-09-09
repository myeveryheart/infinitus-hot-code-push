package com.m.hcp.storage;

import com.m.hcp.config.ContentManifest;
import com.m.hcp.model.PluginFilesStructure;
import com.m.hcp.utils.Paths;

/**
 * Created by Nikolay Demyankov on 23.07.15.
 * <p/>
 * Utility class to save and load content manifest file from the certain folder.
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
        fileName = PluginFilesStructure.MANIFEST_FILE_NAME;
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
