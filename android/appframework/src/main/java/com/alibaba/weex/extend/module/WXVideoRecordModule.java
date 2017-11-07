package com.alibaba.weex.extend.module;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.weex.commons.AbsWeexActivity;
import com.alibaba.weex.extend.module.widget.recorder.VideoRecordActivity;
import com.alibaba.weex.utils.VideoUtil;
import com.taobao.weex.annotation.JSMethod;
import com.taobao.weex.bridge.JSCallback;
import com.taobao.weex.common.WXModule;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

/**
 * Created by aijun on 2017/9/12.
 */

public class WXVideoRecordModule extends WXModule {

    private static final int RESULT_ERROR_CODE = 304;

    private int minTime, maxTime;
    private int cameraX, cameraY;
    private int videoRate;
    private JSCallback callback;
    private static final int REQUEST_RECORD= 102;

    @JSMethod
    public void videoRecord(Map<String, Object> options, final JSCallback callback){
        Bundle bundle = new Bundle();
        this.callback = callback;

        if(options.containsKey("minTime"))
        {
            this.minTime = Integer.parseInt(options.get("minTime").toString());
        }
        if(options.containsKey("maxTime"))
        {
            this.maxTime = Integer.parseInt(options.get("maxTime").toString());
        }
        if(options.containsKey("cameraX"))
        {
            this.cameraX = Integer.parseInt(options.get("cameraX").toString());
        }
        if(options.containsKey("cameraY"))
        {
            this.cameraY = Integer.parseInt(options.get("cameraY").toString());
        }
        if(options.containsKey("videoRate"))
        {
            this.videoRate = Integer.parseInt(options.get("videoRate").toString());
        }


        bundle.putInt("minTime", this.minTime);
        bundle.putInt("maxTime", this.maxTime);
        bundle.putInt("cameraX", this.cameraX);
        bundle.putInt("cameraY", this.cameraY);
        bundle.putInt("videoRate", this.videoRate);

        Intent intent = new Intent(mWXSDKInstance.getContext(), VideoRecordActivity.class);
        intent.putExtras(bundle);
        final AbsWeexActivity activity = (AbsWeexActivity) mWXSDKInstance.getContext();
        activity.startActivityForResult(intent, REQUEST_RECORD);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_RECORD ) {
            if(resultCode == RESULT_OK) {

                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.fromFile(new File(data.getStringExtra("video")));
                mediaScanIntent.setData(contentUri);
                mWXSDKInstance.getContext().sendBroadcast(mediaScanIntent);

                /*Intent mediaScanIntent2 = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri2 = Uri.fromFile(new File(data.getStringExtra("cover")));
                mediaScanIntent2.setData(contentUri2);
                mWXSDKInstance.getContext().sendBroadcast(mediaScanIntent2);*/

                final Map<String, Object> ret = new HashMap<>();
                final Map<String, Object> dataOption = new HashMap<>();
                VideoUtil.CompressVideo((AbsWeexActivity) mWXSDKInstance.getContext()
                        , new VideoUtil.CompressListener() {
                            @Override
                            public void onCompress(boolean compressResult, String videoPath, String coverPath) {
                                if(!TextUtils.isEmpty(videoPath)) {
                                    dataOption.put("video", videoPath);
                                    dataOption.put("cover", coverPath);
                                    dataOption.put("length", data.getStringExtra("length"));
                                    dataOption.put("degress", data.getStringExtra("degress"));
                                    ret.put("result", "success");
                                    ret.put("data", dataOption);

                                    Intent mediaScanIntent2 = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                                    Uri contentUri2 = Uri.fromFile(new File(coverPath));
                                    mediaScanIntent2.setData(contentUri2);
                                    mWXSDKInstance.getContext().sendBroadcast(mediaScanIntent2);
                                } else{
                                    ret.put("result", "failure");
                                    ret.put("data", "压缩失败");
                                }
                                callback.invoke(ret);
                            }
                        }
                        , data.getStringExtra("video")
                        , "" + videoRate
                        , "50");
            }
            else if(resultCode == RESULT_ERROR_CODE) {
                Map<String, Object> ret = new HashMap<>();
                Map<String, Object> dataOption = new HashMap<>();
                dataOption.put("message", data.getStringExtra("message"));
                ret.put("result", "failure");
                ret.put("data", dataOption);
                callback.invoke(ret);
            }
        }
    }

}
