package com.infinitus.hcp.network;

import com.infinitus.hcp.config.ApplicationConfig;

/**
 * Created by M on 16/9/9.
 * <p/>
 * 下载config文件
 *
 * @see ApplicationConfig
 * @see DownloadResult
 */
public class ApplicationConfigDownloader extends JsonDownloader<ApplicationConfig> {

    /**
     * Class constructor
     *
     * @param url config url
     */
    public ApplicationConfigDownloader(String url) {
        super(url);
    }

    @Override
    protected ApplicationConfig createInstance(String json) {
        return ApplicationConfig.fromJson(json);
    }
}