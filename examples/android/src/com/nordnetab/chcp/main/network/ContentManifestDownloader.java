package com.nordnetab.chcp.main.network;

import com.nordnetab.chcp.main.config.ContentManifest;

/**
 * Created by M on 16/9/9.
 * <p/>
 * 下载manifest文件
 *
 * @see ContentManifest
 * @see DownloadResult
 */
public class ContentManifestDownloader extends JsonDownloader<ContentManifest> {

    /**
     * Class constructor
     *
     * @param url manifest url
     */
    public ContentManifestDownloader(String url) {
        super(url);
    }

    @Override
    protected ContentManifest createInstance(String json) {
        return ContentManifest.fromJson(json);
    }
}
