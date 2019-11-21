package com.yue.fileupdown.apkupdate;

import android.content.Context;

import java.io.File;

/**
 * @author shimy
 * @create 2019/11/21 11:33
 * @desc 应用更新辅助类
 */
public class ApkUpdateHelper {
    private Context context;
    private String downLoadUrl;
    private String fileName;
    private String dir;
    private ApkNotificationParams notificationParams;


    /**
     * @param context
     * @param downLoadUrl 下载地址
     * @param fileName    存储文件名
     * @param dir         存储路径
     */
    public ApkUpdateHelper(Context context, String downLoadUrl, String fileName, String dir) {
        this(context, downLoadUrl, fileName, dir, null);
    }

    /**
     * @param context
     * @param downLoadUrl        下载地址
     * @param fileName           存储文件名
     * @param dir                存储路径
     * @param notificationParams 通知帮助类
     */
    public ApkUpdateHelper(Context context, String downLoadUrl, String fileName, String dir, ApkNotificationParams notificationParams) {
        this.context = context;
        this.downLoadUrl = downLoadUrl;
        this.fileName = fileName;
        this.dir = dir;
        this.notificationParams = notificationParams;
        if (this.notificationParams == null) {
            if (dir.lastIndexOf("/") != -1)
                dir = dir.substring(0, (dir.length() - 1));
            if (fileName.indexOf("/", 0) != -1)
                fileName = fileName.substring(1, fileName.length());
            this.notificationParams = new ApkNotificationParams.Builder(context, dir + File.separator + fileName).create();
        }
    }


    public void updateApk() {
        ApkUpdateService.startService(context, downLoadUrl, dir, fileName, notificationParams);
    }
}
