package com.nordnetab.hcp.main.events;

import com.nordnetab.hcp.main.config.ApplicationConfig;
import com.nordnetab.hcp.main.model.HCPError;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by M on 16/9/9.
 * <p/>
 * 下载文件出错
 */
public class DownloadProgressEvent {

    public static final String EVENT_NAME = "hcp_downloadProgressEvent";

    private final int totalFiles;
    private final int fileDownloaded;

//    /**
//     * Class constructor.
//     *
//     * @param error  错误
//     * @param config 使用的config
//     */
    public DownloadProgressEvent(int totalFiles, int fileDownloaded) {
        this.totalFiles = totalFiles;
        this.fileDownloaded = fileDownloaded;
    }

    public int totalFiles()
    {
        return totalFiles;
    }

    public int fileDownloaded()
    {
        return fileDownloaded;
    }
}
