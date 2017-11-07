package com.alibaba.weex.extend.module;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.weex.utils.ShareUtil;
import com.taobao.weex.bridge.JSCallback;
import com.taobao.weex.common.WXModule;
import com.taobao.weex.common.WXModuleAnno;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;

import java.util.Map;


public class WXEventModule extends WXModule {

  private static final String WEEX_CATEGORY = "com.brlf.smartbusiness.page";

  @WXModuleAnno(moduleMethod = true,runOnUIThread = true)
  public void openURL(String url) {
    if (TextUtils.isEmpty(url)) {
      return;
    }
    Intent intent = new Intent(Intent.ACTION_VIEW);
    Uri uri = Uri.parse(url);
    String scheme = uri.getScheme();

    if (TextUtils.equals("tel", scheme)) {

    } else if (TextUtils.equals("sms", scheme)) {

    } else if (TextUtils.equals("mailto", scheme)) {

    } else if (TextUtils.equals("http", scheme) ||
        TextUtils.equals("https",
        scheme)) {
      intent.putExtra("isLocal", false);
      intent.addCategory(WEEX_CATEGORY);
    } else if (TextUtils.equals("file", scheme)) {
      intent.putExtra("isLocal", true);
      intent.addCategory(WEEX_CATEGORY);
    } else {
      intent.addCategory(WEEX_CATEGORY);
      uri = Uri.parse(new StringBuilder("http:").append(url).toString());
    }

    intent.setData(uri);
    mWXSDKInstance.getContext().startActivity(intent);
  }

  @WXModuleAnno(moduleMethod = true,runOnUIThread = true)
  public void openURLToRoot(String url) {
    if (TextUtils.isEmpty(url)) {
      return;
    }


    Intent intent = new Intent(Intent.ACTION_VIEW);
    Uri uri = Uri.parse(url);
    String scheme = uri.getScheme();

    if (TextUtils.equals("tel", scheme)) {

    } else if (TextUtils.equals("sms", scheme)) {

    } else if (TextUtils.equals("mailto", scheme)) {

    } else if (TextUtils.equals("http", scheme) ||
            TextUtils.equals("https",
                    scheme)) {
      intent.putExtra("isLocal", false);
      intent.addCategory(WEEX_CATEGORY);
    } else if (TextUtils.equals("file", scheme)) {
      intent.putExtra("isLocal", true);
      intent.addCategory(WEEX_CATEGORY);
    } else {
      intent.addCategory(WEEX_CATEGORY);
      uri = Uri.parse(new StringBuilder("http:").append(url).toString());
    }

    intent.setData(uri);
    mWXSDKInstance.getContext().startActivity(intent);
  }

  @WXModuleAnno
  public void goBack(){
    ((Activity)mWXSDKInstance.getContext()).finish();
  }

  @WXModuleAnno
  public void finish(){
        ((Activity)mWXSDKInstance.getContext()).finish();
    }

  @WXModuleAnno
  public void exitApp(){
    Handler handler = new Handler();
    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        /**
         *要执行的操作
         */
        System.exit(0);
      }
    }, 300);//3秒后执行Runnable中的run方法

  }

  @WXModuleAnno
  public void Call(String phone){
    Intent intent = new Intent(Intent.ACTION_DIAL,Uri.parse("tel:"+phone));
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    mWXSDKInstance.getContext().startActivity(intent);
  }

  //@JSMethod
  /*public void openURLtoLandscape(String url) {
    if (TextUtils.isEmpty(url)) {
      return;
    }
    Intent intent = new Intent(Intent.ACTION_VIEW);
    Uri uri = Uri.parse(url);
    String scheme = uri.getScheme();

    if (TextUtils.equals("tel", scheme)) {

    } else if (TextUtils.equals("sms", scheme)) {

    } else if (TextUtils.equals("mailto", scheme)) {

    } else if (TextUtils.equals("http", scheme) ||
            TextUtils.equals("https",
                    scheme)) {
      intent.putExtra("isLocal", false);
      intent.addCategory(WEEX_LANSCAPE_CATEGORY);
    } else if (TextUtils.equals("file", scheme)) {
      intent.putExtra("isLocal", true);
      intent.addCategory(WEEX_LANSCAPE_CATEGORY);
    } else {
      intent.addCategory(WEEX_LANSCAPE_CATEGORY);
      uri = Uri.parse(new StringBuilder("http:").append(url).toString());
    }

    intent.setData(uri);
    mWXSDKInstance.getContext().startActivity(intent);
  };*/

  //友盟分享
  @WXModuleAnno
  public void umShare(Map<String, Object> options, final JSCallback callback) {
    Log.e("tag", options.toString());
    ShareUtil.share((Activity)mWXSDKInstance.getContext(), options, shareListener);
  }

  //分享回调监听
  private UMShareListener shareListener = new UMShareListener() {
    /**
     * @descrption 分享开始的回调
     * @param platform 平台类型
     */
    @Override
    public void onStart(SHARE_MEDIA platform) {
      Log.e("onStart", "ssss");
    }

    /**
     * @descrption 分享成功的回调
     * @param platform 平台类型
     */
    @Override
    public void onResult(SHARE_MEDIA platform) {
      Toast.makeText(mWXSDKInstance.getContext()
              ,"分享成功", Toast.LENGTH_SHORT).show();
    }

    /**
     * @descrption 分享失败的回调
     * @param platform 平台类型
     * @param t 错误原因
     */
    @Override
    public void onError(SHARE_MEDIA platform, Throwable t) {
      Log.e("onError", t.getMessage());
      Toast.makeText(mWXSDKInstance.getContext()
              ,"分享失败："+t.getMessage(),Toast.LENGTH_LONG).show();
    }

    /**
     * @descrption 分享取消的回调
     * @param platform 平台类型
     */
    @Override
    public void onCancel(SHARE_MEDIA platform) {
      Toast.makeText(mWXSDKInstance.getContext()
              ,"分享取消",Toast.LENGTH_SHORT).show();
    }
  };

  ////////////////////////////////////////////////////////////////////////////

  //友盟统计
  @WXModuleAnno
  public void umStatistics(Map<String, Object> options) {
    //
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
