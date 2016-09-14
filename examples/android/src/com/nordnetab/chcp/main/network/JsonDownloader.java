package com.nordnetab.chcp.main.network;

import com.nordnetab.chcp.main.utils.URLUtility;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by M on 16/9/9.
 * <p/>
 * 下载JSON然后转成对象
 *
 * @see DownloadResult
 */
abstract class JsonDownloader<T> {

    private String downloadUrl;

    // connection timeout in milliseconds
    private static final int CONNECTION_TIMEOUT = 30000;

    // data read timeout in milliseconds
    private static final int READ_TIMEOUT = 30000;

    /**
     * json转对象
     *
     * @param json 下载的JSON string
     * @return 对象实例
     */
    protected abstract T createInstance(String json);

    /**
     * Class constructor
     *
     * @param url JSON的url
     */
    public JsonDownloader(String url) {
        this.downloadUrl = url;
    }

    /**
     * 执行下载
     *
     * @return 下载结果
     * @see DownloadResult
     */
    public DownloadResult<T> download() {
        DownloadResult<T> result;

        try {
            String json = downloadJson();
            T value = createInstance(json);

            result = new DownloadResult<T>(value);
        } catch (Exception e) {
            e.printStackTrace();

            result = new DownloadResult<T>(e);
        }

        return result;
    }

    private String downloadJson() throws Exception {
        final StringBuilder jsonContent = new StringBuilder();

        final URL url = URLUtility.stringToUrl(downloadUrl);
        if (url == null) {
            throw new Exception("Invalid url format:" + downloadUrl);
        }

        final URLConnection urlConnection = url.openConnection();
        urlConnection.setConnectTimeout(CONNECTION_TIMEOUT);
        urlConnection.setReadTimeout(READ_TIMEOUT);

        final InputStreamReader streamReader = new InputStreamReader(urlConnection.getInputStream());
        final BufferedReader bufferedReader = new BufferedReader(streamReader);

        final char data[] = new char[1024];
        int count;
        while ((count = bufferedReader.read(data)) != -1) {
            jsonContent.append(data, 0, count);
        }
        bufferedReader.close();

        return jsonContent.toString();
    }
}
