package com.alibaba.weex.utils;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMVideo;
import com.umeng.socialize.media.UMWeb;

import java.util.Map;

/**
 * Created by Admin on 2017/10/13.
 */

public class ShareUtil {
    //分享类型：目前仅text-文本、image-图片
    private static String type="";
    private static final String TYPE_TEXT = "text";
    private static final String TYPE_IMAGE = "image";
    private static final String TYPE_VIDEO = "video";
    private static final String TYPE_URL = "url";

    //分享渠道：wechat-微信好友、wechatTimeline-微信朋友圈、qq-QQ、qqZone-QQ空间、all-4种
    private static String channel="";
    private static final String PLATFORM_WECHAT = "wechat";
    private static final String PLATFORM_TIMELINE = "wechatTimeline";
    private static final String PLATFORM_QQ = "qq";
    private static final String PLATFORM_QQZONE = "qqZone";
    private static final String PLATFORM_ALL = "all";

    //分享的图片url
    private static String imageUrl="";

    //分享的文本
    private static String text="";

    //分享的标题
    private static String title="";

    //分享的链接
    private static String  url="";

    //是否使用带面板分享
    private static boolean isWithUI = false;

    //分享的平台
    private static SHARE_MEDIA sharePlatform;

    /**
     * 分享功能
     * @param activity 分享的activity
     * @param options 参数
     * @param shareListener 回调监听
     */
    public static void share(Activity activity, Map<String, Object> options, UMShareListener shareListener) {
        initialize();
        setOptions(options);
        if(isWithUI) {
            ShareWithUI(activity, shareListener);
        } else {
            if(sharePlatform == null) {
                Toast.makeText(activity, "分享失败，分享平台错误", Toast.LENGTH_LONG).show();
                return;
            }
            if(!isInstallClient(activity)) {
                Toast.makeText(activity, "分享失败，未安装客户端", Toast.LENGTH_LONG).show();
                return;
            }
            ShareWithoutUI(activity, shareListener);
        }
    }

    /**
     * 参数获取
     * @param options 参数
     */
    private static void setOptions(Map<String, Object> options) {
//        Log.e("options", "sss");
        if(options.containsKey("type"))
        {
            type = (String) options.get("type");

            //暂用分享url
            if(type.equals(TYPE_TEXT)) {
                type = TYPE_URL;
            }
        }
        if(options.containsKey("channel"))
        {
            channel = (String) options.get("channel");
            if(channel.equals(PLATFORM_ALL)) {
                isWithUI = true;
            } else {
                isWithUI = false;
            }
            setPlatform();
        }
        if(options.containsKey("imageUrl"))
        {
            imageUrl = (String) options.get("imageUrl");
        }
        if(options.containsKey("text"))
        {
            text = (String) options.get("text");
        }
        if(options.containsKey("title"))
        {
            title = (String) options.get("title");
        }
        if(options.containsKey("url"))
        {
            url = (String) options.get("url");
            Log.e("set", url);
        }
//        Log.e("set", "end");
    }

    //带面板分享
    public static void ShareWithUI(Activity activity, UMShareListener shareListener) {
        setShareAction(activity)
                .setDisplayList(SHARE_MEDIA.QQ
                        ,SHARE_MEDIA.QZONE
                        ,SHARE_MEDIA.WEIXIN
                        ,SHARE_MEDIA.WEIXIN_CIRCLE)
                .setCallback(shareListener)
                .open();
    }

    //不带面板分享
    private static void ShareWithoutUI(Activity activity, UMShareListener shareListener){
//        Log.e("ShareWithoutUI", "sss");
        setShareAction(activity)
                .setCallback(shareListener)//回调监听器
                .share();
//        Log.e("ShareWithoutUI", "end");
    }

    /**设置分享的平台和分享内容
     * 平台支持的分享类型
     * QQ: 图片 链接 视频 音乐
     * Qzone：文字（说说） 图片（说说） 链接 视频 音乐
     * 微信：文本 图片 链接 视频 音乐
     * 微信朋友圈：文本 图片 链接（Description不会显示） 视频 音乐
     * @param activity
     * @return
     */
    private static ShareAction setShareAction(Activity activity){
//        Log.e("setShareAction", "sss");

        ShareAction shareAction = new ShareAction(activity);

        switch (type) {
            //文本
            case TYPE_TEXT:
                shareAction.setPlatform(sharePlatform).withText(text).withMedia(new UMImage(activity, imageUrl)).share();
                break;

            //图片
            case TYPE_IMAGE:
                shareAction.setPlatform(sharePlatform).withMedia(new UMImage(activity, imageUrl));
                break;

            //视频：只能网络视频
            case TYPE_VIDEO:
                UMVideo video = new UMVideo(url);
                video.setTitle(title);//视频的标题
                video.setThumb(new UMImage(activity, imageUrl));//视频的缩略图
                video.setDescription(text);//视频的描述
                shareAction.setPlatform(sharePlatform)
                           .withText(text)
                           .withMedia(video);
                break;

            //链接
            case TYPE_URL:
                UMWeb  web = new UMWeb(url);  //url地址
                web.setTitle(title);//标题
                web.setThumb(new UMImage(activity, imageUrl));  //缩略图
                web.setDescription(text);//描述
                shareAction.setPlatform(sharePlatform).withMedia(web);
                break;

            default:
                break;
        }
//        Log.e("setShareAction", shareAction.toString());
        return shareAction;
    }

    /**
     * 设置分享平台
     *QQ	        SHARE_MEDIA.QQ
     *Qzone	        SHARE_MEDIA.QZONE
     *微信好友	    SHARE_MEDIA.WEIXIN
     *微信朋友圈	    SHARE_MEDIA.WEIXIN_CIRCLE
     */
    private static void setPlatform(){
//        Log.e("setPlatform", "sss");
        switch (channel){
            case PLATFORM_WECHAT:
                sharePlatform = SHARE_MEDIA.WEIXIN;
                break;
            case PLATFORM_TIMELINE:
                sharePlatform = SHARE_MEDIA.WEIXIN_CIRCLE;
                break;
            case PLATFORM_QQ:
                sharePlatform = SHARE_MEDIA.QQ;
                break;
            case PLATFORM_QQZONE:
                sharePlatform = SHARE_MEDIA.QZONE;
                break;
            case PLATFORM_ALL:
                break;
            default:
                break;
        }
    }

    //初始化数据
    private static void initialize(){
        type = "";
        text = "";
        imageUrl = "";
        title = "";
        channel = "";
        url = "";
        isWithUI = false;
        sharePlatform = null;
    }

    private static boolean isInstallClient(Activity activity){
        return UMShareAPI.get(activity).isInstall(activity, sharePlatform);
    }
}
