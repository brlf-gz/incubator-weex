package com.alibaba.weex.utils;

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
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;

/**
 * author: liminjie
 * date: 2017/6/25
 * desc: 图片处理工具
 */
public class PictureUtil {

    private static final String CAMERA = Environment.getExternalStorageDirectory() + "/DCIM/Camera/";


    //调用系统拍照
    public static void startCapture(@NonNull Activity activity, @NonNull int requestCode, @NonNull String photoName) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File file = new File(CAMERA + photoName);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT,
                Uri.fromFile(file));
        activity.startActivityForResult(intent, requestCode);
    }

    //调用系统图库选择图片
    public static void startGallery(@NonNull Activity activity, @NonNull int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        activity.startActivityForResult(intent, requestCode);
    }

    //根据传入的图片路径转成Base64编码
    public static String bitmapToBase64(@NonNull String path){
        Bitmap bitmap = getSmallBitmap(path);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 40, baos);
        byte[] bytes = baos.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    //根据Uri获取文件路径
    public static String getRealFilePath(@NonNull Context context, @NonNull Uri uri) {
        if (null == uri) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null)
            data = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

    //拍照后发送广播更新图库
    public static void updateGallery(@NonNull Context context, @NonNull String imagePath) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(new File(imagePath));
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }

    //得到压缩bitmap
    private static Bitmap getSmallBitmap(String path) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        options.inSampleSize = calculateInSampleSize(options, 1920, 1920);

        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(path, options);
    }

    //计算压缩比
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int width = options.outWidth;
        final int height = options.outHeight;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            int widthRadio = Math.round(width * 1.0f / reqWidth);
            int heightRadio = Math.round(height * 1.0f / reqHeight);

            inSampleSize = Math.max(widthRadio, heightRadio);
        }

        return inSampleSize;
    }
}
