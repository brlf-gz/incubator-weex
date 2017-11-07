package com.alibaba.weex.extend.module;

import android.content.Intent;
import android.text.TextUtils;

import com.alibaba.weex.commons.AbsWeexActivity;
import com.taobao.weex.annotation.JSMethod;
import com.taobao.weex.bridge.JSCallback;
import com.taobao.weex.common.WXModule;
import com.brlf.brscanner.CaptureActivity;
import com.taobao.weex.utils.WXLogUtils;
import com.zero.smallvideorecord.Log;

import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

/**
 * Created by Admin on 2017/9/26.
 */

public class WXScannerModule extends WXModule {

    private static final String TAG = "WXScannerModule";
    private static final int ScannerRequestCode = 103;

    private JSCallback callback;

    /**
     * 开始扫码
     * @param params 参数列表
     * @param callback js回调方法
     */
    @JSMethod
    public void startScan(Map<String,Object> params, JSCallback callback){
        this.callback = callback;
        final AbsWeexActivity activity = (AbsWeexActivity) mWXSDKInstance.getContext();
        activity.startActivityForResult(
                new Intent(activity, CaptureActivity.class)
                , ScannerRequestCode
        );
    }


    //结束扫码
    @JSMethod
    public void stopScan(){
        return;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
//        super.onActivityResult(requestCode, resultCode, intent);
        Map<String, Object> resultMap = new HashMap<>();

        if(requestCode == ScannerRequestCode) {
            switch (resultCode){
                case RESULT_OK:
                    if(TextUtils.isEmpty(intent.getStringExtra("result"))) {
                        resultMap.put("result", "fail");
                        resultMap.put("data", "扫码失败");
                    } else {
                        resultMap.put("result", "success");
                        resultMap.put("data", intent.getStringExtra("result"));
                    }
                    Log.e(TAG, intent.getStringExtra("result"));
                    callback.invoke(resultMap);
                    break;

                case RESULT_CANCELED:
                    resultMap.put("result", "fail");
                    resultMap.put("data", "用户取消");
                    callback.invoke(resultMap);
                    break;

                case CaptureActivity.RESULT_ERROR:
                    resultMap.put("result", "fail");
                    resultMap.put("data", intent.getStringExtra("result"));
                    callback.invoke(resultMap);
                    break;
            }
        }
        else{
            super.onActivityResult(requestCode, resultCode, intent);
        }
    }
}
