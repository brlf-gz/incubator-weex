package com.alibaba.weex.extend.module;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.weex.commons.AbsWeexActivity;
import com.taobao.weex.WXSDKInstance;
import com.taobao.weex.utils.WXLogUtils;
import com.alibaba.weex.utils.PictureUtil;
import com.taobao.weex.annotation.JSMethod;
import com.taobao.weex.bridge.JSCallback;
import com.taobao.weex.common.WXModule;
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

/**
 * author: liminjie
 * date: 2017/6/15
 * desc: 图片选取Module
 */

public class WXImagePickerModule extends WXModule {
    private static final String TAG = "ImgPickerModule";
    private static final String WEEX_CATEGORY = "com.brlf.weex.anchor";
    private static final int REQUEST_CAMERA = 100;
    private static final int REQUEST_GALLERY = REQUEST_CAMERA + 1;

    private boolean edit = false;
    private boolean mLockRatio = false;
    private float mRatio = 1.5f;
    private float mMaxWidth = 3600;
    private float mMaxHeight = 3600;
    private float mFileSize = 5000;

    //系统拍照默认保存文件夹
    private String cameraPath = Environment.getExternalStorageDirectory().toString() + "/DCIM/Camera/";
    //保存文件夹
    private String folderPath = Environment.getExternalStorageDirectory().toString() + "/brlfScreenshot/";
    //拍照图片名
    private String photoName;
    //获取图片回调
    private JSCallback callback;

    /*
     {
       "edit":true, //可裁剪？
       "file-size": "5000",//文件大小kb
       "max-width": "600",//最大宽度
       "max-height": "600",//最大高度
       "lock-ratio": true, true编辑时锁定比例，此时必须指定ratio, false尺寸比例自由
       "ratio": 1.56f, 宽度和高度的比例
     }
     */

    /**
     * 拍照
     *
     * @param options
     * @param callback
     */
    @JSMethod
    public void captureImage(Map<String, Object> options, final JSCallback callback) {

        ActivityCompat.requestPermissions(
                ((AbsWeexActivity) mWXSDKInstance.getContext()),
                new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                1);

        this.callback = callback;
        if(options.containsKey("edit"))
        {
            edit = (boolean) options.get("edit");
        }
        if(options.containsKey("lock-ratio"))
        {
            mLockRatio = (boolean)options.get("lock-ratio");
        }
        if(options.containsKey("ratio"))
        {
            mRatio = Float.parseFloat(options.get("ratio").toString());
        }
        if(options.containsKey("max-width"))
        {
            mMaxWidth = Float.parseFloat(options.get("max-width").toString());
        }
        if(options.containsKey("max-height"))
        {
            mMaxHeight = Float.parseFloat(options.get("max-height").toString());
        }
        if(options.containsKey("file-size"))
        {
            mFileSize = Float.parseFloat(options.get("file-size").toString());
        }

        File file = new File(folderPath);
        if(!file.exists()){
            file.mkdirs();
        }
        photoName = "IMG_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".png";

        PictureUtil.startCapture(
                (AbsWeexActivity) mWXSDKInstance.getContext(),
                REQUEST_CAMERA,
                photoName
        );
    }

    /**
     * 相册
     *
     * @param options
     * @param callback
     */
    @JSMethod
    public void pickerImage(Map<String, Object> options, final JSCallback callback) {

        ActivityCompat.requestPermissions(
                ((AbsWeexActivity) mWXSDKInstance.getContext()),
                new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                1);

        this.callback = callback;
        if(options.containsKey("edit"))
        {
            edit = (boolean) options.get("edit");
        }
        if(options.containsKey("lock-ratio"))
        {
            mLockRatio = (boolean)options.get("lock-ratio");
        }
        if(options.containsKey("ratio"))
        {
            mRatio = Float.parseFloat(options.get("ratio").toString());
        }
        if(options.containsKey("max-width"))
        {
            mMaxWidth = Float.parseFloat(options.get("max-width").toString());
        }
        if(options.containsKey("max-height"))
        {
            mMaxHeight = Float.parseFloat(options.get("max-height").toString());
        }
        if(options.containsKey("file-size"))
        {
            mFileSize = Float.parseFloat(options.get("file-size").toString());
        }

        File file = new File(folderPath);
        if(!file.exists()){
            file.mkdirs();
        }
        photoName = "IMG_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".png";

        PictureUtil.startGallery(
                (AbsWeexActivity) mWXSDKInstance.getContext(),
                REQUEST_GALLERY
        );
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CAMERA://拍照
                if (resultCode == RESULT_OK) {
                    String photoPath = cameraPath + photoName;
                    PictureUtil.updateGallery(mWXSDKInstance.getContext(), photoPath);
                    if (edit == true) {
                        Uri contentUri = Uri.fromFile(new File(photoPath));
                        //打开编辑页面
                        startCropActivity(contentUri);
                        return;
                    }
                    callbackToWeex(photoPath);
                } else {
                    Toast.makeText(mWXSDKInstance.getContext(), "获取图片失败", Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_GALLERY://相册
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    if (edit == true) {
                        startCropActivity(uri);
                        return;
                    }
                    String photoPath = PictureUtil.getRealFilePath(mWXSDKInstance.getContext(), uri);
                    callbackToWeex(photoPath);
                } else {
                    Toast.makeText(mWXSDKInstance.getContext(), "获取图片失败", Toast.LENGTH_SHORT).show();
                }
                break;

            case UCrop.REQUEST_CROP:
                if(resultCode == RESULT_OK) {
                    final Uri resultUri = UCrop.getOutput(data);
                    if (resultUri != null) {
                        String photoPath = PictureUtil.getRealFilePath(mWXSDKInstance.getContext(), resultUri);
                        callbackToWeex(photoPath);
                    }
                }
                else if(resultCode == RESULT_CANCELED){
                    callbackToWeexErr(data.getStringExtra("message"));
                }
                else if(resultCode == UCrop.RESULT_ERROR){
                    Toast.makeText(mWXSDKInstance.getContext(), "获取图片失败", Toast.LENGTH_SHORT).show();
                    final Throwable cropError = UCrop.getError(data);
                    if(cropError!=null){
                        callbackToWeexErr(cropError.getMessage());
                    }
                }
                break;
            default:
                break;
        }
    }

    //回调B端
    private void callbackToWeex(String path) {
        try {
            String photo64 = null;
            photo64 = PictureUtil.bitmapToBase64(path);
            Map<String, Object> ret = new HashMap<>();
            Map<String, Object> dataOption = new HashMap<>();

            String imageData = "data:image/jpeg;base64," + photo64;
            dataOption.put("imagePath", path);
            dataOption.put("size", "5000");
            dataOption.put("imageData", imageData);

            ret.put("result", "success");
            ret.put("data", dataOption);

            callback.invoke(ret);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void callbackToWeexErr(String message){
        Map<String, Object>ret = new HashMap<>();
        ret.put("result", "failure");
        ret.put("message", message);
    }

    private void startCropActivity(@NonNull Uri uri) {
       /* String destinationFileName = "IMG_CHW";
        destinationFileName += ".png";*/

        UCrop uCrop = UCrop.of(uri, Uri.fromFile(new File(folderPath, photoName))); //图片输出位置
        uCrop = setConfig(uCrop);
        uCrop.start((AbsWeexActivity)mWXSDKInstance.getContext());
    }

    private UCrop setConfig(@NonNull UCrop uCrop) {
        //设置结果图片最大宽高
        try {
            if (mMaxWidth > 0 && mMaxHeight > 0) {
                uCrop = uCrop.withMaxResultSize((int)mMaxWidth, (int)mMaxHeight);
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "Number please", e);
        }

        //设置裁剪框的比例
        try {
            if (mRatio > 0) {
                uCrop = uCrop.withAspectRatio(mRatio, 1);
            }
        } catch (NumberFormatException e) {
            Log.i(TAG, String.format("Number please: %s", e.getMessage()));
        }

        UCrop.Options options = new UCrop.Options();
        options.setCompressionFormat(Bitmap.CompressFormat.PNG); //图片格式
        options.setCompressionQuality(100); //图片质量
        options.setMaxBitmapSize((int)mFileSize); //最大尺寸
        options.setHideBottomControls(false); //显示下面控制的视图
        if(!mLockRatio) {
            options.setFreeStyleCropEnabled(true); //裁剪框可自由移动
        }
        return uCrop.withOptions(options);
    }

}