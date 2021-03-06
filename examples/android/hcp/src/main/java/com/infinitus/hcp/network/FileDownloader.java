package com.infinitus.hcp.network;

import android.util.Log;

import com.infinitus.hcp.events.DownloadProgressEvent;
import com.infinitus.hcp.model.ManifestFile;
import com.infinitus.hcp.utils.FilesUtility;
import com.infinitus.hcp.utils.MD5;
import com.infinitus.hcp.utils.Paths;
import com.infinitus.hcp.utils.URLUtility;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

/**
 * Created by M on 16/9/9.
 * <p/>
 * 下载文件
 */
public class FileDownloader {

    // connection timeout in milliseconds
    private static final int CONNECTION_TIMEOUT = 30000;

    // data read timeout in milliseconds
    private static final int READ_TIMEOUT = 30000;

    private static int fileDownloaded = 0;
    private static int totalFiles = 0;

    /**
     * 异步下载文件
     *
     * @param downloadFolder   存储url
     * @param contentFolderUrl 文件url
     * @param files            文件列表
     * @throws IOException
     * @see ManifestFile
     */
    public static void downloadFiles(final String downloadFolder, final String contentFolderUrl, List<ManifestFile> files) throws IOException {
        fileDownloaded = 0;
        totalFiles = files.size();
        for (ManifestFile file : files) {
            String fileUrl = URLUtility.construct(contentFolderUrl, file.name);
            String filePath = Paths.get(downloadFolder, file.name);
            download(fileUrl, filePath, file.hash);
            fileDownloaded++;
            //发送下载进度
            setProgress();
        }
    }

    /**
     * 异步下载
     *
     * @param urlFrom  下载的url
     * @param filePath 存储url
     * @param checkSum HASH
     * @throws IOException
     */
    public static void download(final String urlFrom, final String filePath, final String checkSum) throws IOException {
        Log.d("HCP", "Loading file: " + urlFrom);

        final File downloadFile = new File(filePath);
        FilesUtility.delete(downloadFile);
        FilesUtility.ensureDirectoryExists(downloadFile.getParentFile());

        final MD5 md5 = new MD5();

        final URL downloadUrl = URLUtility.stringToUrl(urlFrom);
        if (downloadUrl == null) {
            throw new IOException("Invalid url format");
        }

        // create connection
        final URLConnection connection = downloadUrl.openConnection();
        connection.setConnectTimeout(CONNECTION_TIMEOUT);
        connection.setReadTimeout(READ_TIMEOUT);
        connection.connect();

        // create streams
        final InputStream input = new BufferedInputStream(downloadUrl.openStream());
        final OutputStream output = new BufferedOutputStream(new FileOutputStream(filePath, false));

        final byte data[] = new byte[1024];
        int count;
        while ((count = input.read(data)) != -1) {
            output.write(data, 0, count);
            md5.write(data, count);
        }

        output.flush();
        output.close();
        input.close();

        final String downloadedFileHash = md5.calculateHash();
        if (!downloadedFileHash.equals(checkSum)) {
            throw new IOException("File is corrupted: checksum " + checkSum + " doesn't match hash " + downloadedFileHash + " of the downloaded file");
        }
    }

    private static void setProgress()
    {
        EventBus.getDefault().post(new DownloadProgressEvent(totalFiles, fileDownloaded));
    }
}

