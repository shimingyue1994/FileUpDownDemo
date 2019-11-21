package com.yue.fileupdown.ui;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.yue.fileupdown.R;
import com.yue.fileupdown.apkupdate.ApkUpdateHelper;
import com.yue.fileupdown.constant.Constanct;
import com.yue.fileupdown.databinding.ActivityRangDownLoadBinding;
import com.yue.fileupdown.download.DownloadRangListener;
import com.yue.fileupdown.download.FileDownLoadUtils;
import com.yue.fileupdown.download.MyDownloadException;
import com.yue.fileupdown.service.NotificationService;

/**
 * @author shimy
 * @create 2019/11/19 14:09
 * @desc 断点下载
 */
public class RangDownLoadActivity extends AppCompatActivity {

    private ActivityRangDownLoadBinding mBinding;
    //    private String url = "https://app-10034140.cos.ap-shanghai.myqcloud.com/app/pad-1.4.0.apk";
    private String url = "https://t.alipayobjects.com/L1/71/100/and/alipay_wap_main.apk";
    private String fileName = "/alipay_wap_main.apk";
    private String path = Constanct.downloadPath;

    private ApkUpdateHelper apkUpdateHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_rang_down_load);
        apkUpdateHelper = new ApkUpdateHelper(this, url, fileName, path, "com.yue.fileupdown.fileprovider");
        initView();
    }

    private void initView() {
        mBinding.btnDown.setOnClickListener((v) -> {
            try {
                FileDownLoadUtils.getInstance().downLoadRang(url, path, fileName, "hello", new DownloadRangListener() {

                    @Override
                    public void existed(String key) {
                        runOnUiThread(() -> {
                            Toast.makeText(RangDownLoadActivity.this, "文件已存在", Toast.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void pause(String key) {

                    }

                    @Override
                    public void canle(String key) {

                    }

                    @Override
                    public void failure(String key) {
                        runOnUiThread(() -> {
                            mBinding.tvShow.setText("下载失败");
                        });

                    }

                    @Override
                    public void success(String key) {
                        runOnUiThread(() -> {
                            mBinding.tvShow.setText("下载完成");
                        });

                    }

                    @Override
                    public void progress(String key, int progress, long downloadedLength, long contentLength, double speed) {
                        runOnUiThread(() -> {
                            mBinding.tvShow.setText(progress + "/100");
                        });
                    }


                    @Override
                    public void error(String key, Exception e) {
                        runOnUiThread(() -> {
                            mBinding.tvShow.setText("下载异常：" + e.getMessage());
                        });

                    }
                });
            } catch (MyDownloadException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    switch (e.code()) {
                        case REPEAT:
                            Toast.makeText(this, "重复下载", Toast.LENGTH_SHORT).show();
                            break;
                    }
                });
            }
        });
        mBinding.btnDown2.setOnClickListener(v -> {
            apkUpdateHelper.updateApk();
//            Intent intent = new Intent(this, NotificationService.class);
//            startService(intent);
        });
    }

}
