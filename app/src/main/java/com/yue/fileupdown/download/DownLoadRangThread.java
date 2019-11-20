package com.yue.fileupdown.download;

import android.support.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author shimy
 * @create 2019/11/20 8:29
 * @desc 下载线程 断点续传
 */
public class DownLoadRangThread extends Thread implements IDownLoadThread {

    private DownloadRangListener downloadListener;
    private OkHttpClient client;
    private String downloadUrl;//下载地址
    private String directory;//文件存放路径
    private String fileName;//下载文件命名
    private String key;//线程key
    private boolean isCanceled = false;
    private boolean isPaused = false;

    public DownLoadRangThread(String downloadUrl, String directory, String fileName, String key, DownloadRangListener downloadListener) {
        super();
        this.downloadListener = downloadListener;
        this.downloadUrl = downloadUrl;
        this.directory = directory;
        this.fileName = fileName;
        this.key = key;
        client = FileDownLoadUtils.getInstance().getClient();
    }

    @Override
    public void run() {
        super.run();
        InputStream is = null;
        RandomAccessFile savedFile = null;
        File file = null;
        try {
            long downloadedLength = 0; // 记录已下载的文件长度
            File dirctoryFile = new File(directory);
            if (!dirctoryFile.exists())
                dirctoryFile.mkdirs();
            file = new File(directory + File.separator + fileName);
            if (file.exists()) {
                downloadedLength = file.length();
            }
            long contentLength = getContentLength(downloadUrl);
            if (contentLength == 0) {
                downloadListener.error(key, new MyDownloadException("contentLength==0", MyDownloadException.Code.UNKNOWN));
                return;
            } else if (contentLength == downloadedLength) {
                // 已下载字节和文件总字节相等，说明已经下载完成了
                downloadListener.existed(key);
                return;
            }

            Request request = new Request.Builder()
                    // 断点下载，指定从哪个字节开始下载
                    .addHeader("RANGE", "bytes=" + downloadedLength + "-")
                    .url(downloadUrl)
                    .build();
            Response response = client.newCall(request).execute();
            if (response != null) {
                is = response.body().byteStream();//。响应体中的流通管道
                savedFile = new RandomAccessFile(file, "rw");
                savedFile.seek(downloadedLength); // 跳过已下载的字节
                byte[] b = new byte[1024];
                int total = 0;
                int len;
                while ((len = is.read(b)) != -1) {
                    if (isCanceled) {
                        downloadListener.canle(key);
                        return;
                    } else if (isPaused) {
                        downloadListener.pause(key);
                        return;
                    } else {
                        total += len;
                        savedFile.write(b, 0, len);
                        // 计算已下载的百分比
                        int progress = (int) ((total + downloadedLength) * 100 / contentLength);
                        downloadListener.progress(key, progress);
                    }
                }
                response.body().close();
                downloadListener.success(key);
                return;
            }
        } catch (Exception e) {
            downloadListener.error(key, e);
            return;
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (savedFile != null) {
                    savedFile.close();
                }
                if (isCanceled && file != null) {
                    file.delete();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        downloadListener.failure(key);
    }

    public String getKey() {
        return key;
    }

    /**
     * 恢复下载 与pause相对应
     */
    @Override
    public void resumeDownload() {
        isPaused = false;
    }

    /**
     * 暂停下载
     */
    @Override
    public void pauseDownload() {
        isPaused = true;
    }

    /**
     * 取消下载会删除掉源文件
     */
    @Override
    public void cancelDownload() {
        isCanceled = true;
    }

    private long getContentLength(String downloadUrl) throws IOException {
        Request request = new Request.Builder()
                .url(downloadUrl)
                .build();
        Response response = client.newCall(request).execute();
        if (response != null && response.isSuccessful()) {
            long contentLength = response.body().contentLength();
            response.close();
            return contentLength;
        }
        return 0;
    }
}