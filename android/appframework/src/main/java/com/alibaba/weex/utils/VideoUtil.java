package com.alibaba.weex.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.Window;

import com.zero.smallvideorecord.DeviceUtils;
import com.zero.smallvideorecord.JianXiCamera;
import com.zero.smallvideorecord.LocalMediaCompress;
import com.zero.smallvideorecord.StringUtils;
import com.zero.smallvideorecord.model.BaseMediaBitrateConfig;
import com.zero.smallvideorecord.model.CBRMode;
import com.zero.smallvideorecord.model.LocalMediaConfig;
import com.zero.smallvideorecord.model.OnlyCompressOverBean;

import java.io.File;

/**
 * Created by Liangdj on 2017/9/22.
 * 视频处理工具
 */

public class VideoUtil {
    //视频存储地址
    private static final String VIDEOPATH = Environment.getExternalStorageDirectory().getPath() + "/brlfVideoRecord/temp";
    private static final String TAG = "VideoUtil CompressVideo";

    private static ProgressDialog mProgressDialog;
    private static String result;

    /**
     * 视频压缩处理
     * @param path 文件路径
     * @param bitrate 码率/比特率
     * @param rate 视频帧率
     */
    public static void CompressVideo(@NonNull final Activity activity, final CompressListener listener,
                                     @NonNull String path, @NonNull String bitrate, String rate){
        if(TextUtils.isEmpty(path)) {
            Log.e(TAG, "file path error");
            return;
        }
        File file = new File(path);
        if(!file.exists()) {
            Log.e(TAG, "file not exists");
            return;
        }

        initSmallVideo("");
        BaseMediaBitrateConfig compressMode  = new CBRMode(166, Integer.valueOf(bitrate));
        int videoRate = 0; //rate为空时默认使用原视频的
        float videoScale=1.0f; //视频比例，默认1.0
        if (!TextUtils.isEmpty(rate)) {
            videoRate = Integer.valueOf(rate);
        }

        LocalMediaConfig.Buidler buidler = new LocalMediaConfig.Buidler();
        final LocalMediaConfig config = buidler
                .setVideoPath(path)
                .captureThumbnailsTime(1)
                .doH264Compress(compressMode)
                .setFramerate(videoRate)
                .setScale(videoScale)
                .build();

        new Handler().postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Log.i(TAG,"开始压缩");
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        showProgress(activity, "", "压缩中...", ProgressDialog.THEME_HOLO_LIGHT);
                                    }
                                });
                                OnlyCompressOverBean onlyCompressOverBean = new LocalMediaCompress(config).startCompress();
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        hideProgress();
                                    }
                                });

                                result = onlyCompressOverBean.getVideoPath();
                                Log.e("path==============", result);
                                if(TextUtils.isEmpty(result)) {
                                    listener.onCompress(false,"","");
                                }
                                else{
                                    listener.onCompress(true,result,onlyCompressOverBean.getPicPath());
                                }
                            }
                        }).start();
                    }
                },300);
    }


    //压缩过程显示进度
    private static void showProgress(Activity activity,String title, String message, int theme) {
        if (mProgressDialog == null) {
            if (theme > 0)
                mProgressDialog = new ProgressDialog(activity, theme);
            else
                mProgressDialog = new ProgressDialog(activity);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mProgressDialog.setCanceledOnTouchOutside(false);// 不能取消
            mProgressDialog.setCancelable(false);
            mProgressDialog.setIndeterminate(true);// 设置进度条是否不明确
        }

        if (!StringUtils.isEmpty(title))
            mProgressDialog.setTitle(title);
            mProgressDialog.setMessage(message);
            mProgressDialog.show();
    }

    private static void hideProgress() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    //设置存储路径
    public static void initSmallVideo(@NonNull String path) {
        File f;
        if(TextUtils.isEmpty(path)) {
            f = new File(Environment.getExternalStorageDirectory().getPath() + "/brlfVideoRecord/");
            Log.e("*******************",f.getAbsolutePath() + "/");
        } else{
            f = new File(path);
        }
        if(!f.exists()){
            f.mkdirs();
        }
        JianXiCamera.setVideoCachePath(f.getAbsolutePath() + "/");
        Log.e("*******************",f.getAbsolutePath() + "/");
        JianXiCamera.initialize(false, null);
    }

    public interface CompressListener{
        public void onCompress(boolean compressResult, String videoPath, String coverPath);
    }
}
