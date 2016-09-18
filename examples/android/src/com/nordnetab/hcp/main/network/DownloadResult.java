package com.nordnetab.hcp.main.network;

/**
 * Created by M on 16/9/9.
 * <p/>
 * 下载结果
 */
public class DownloadResult<T> {

    /**
     * 下载的data
     */
    public final T value;

    /**
     * 错误
     */
    public final Exception error;

    /**
     * Class constructor
     *
     * @param value loaded value
     */
    public DownloadResult(T value) {
        this(value, null);
    }

    /**
     * Class constructor
     *
     * @param error occurred error
     */
    public DownloadResult(Exception error) {
        this(null, error);
    }

    private DownloadResult(T value, Exception error) {
        this.value = value;
        this.error = error;

    }
}