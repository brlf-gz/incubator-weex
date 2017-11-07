package com.alibaba.weex.extend.module;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.weex.extend.module.widget.upload.ProgressHelper;
import com.alibaba.weex.extend.module.widget.upload.ProgressListener;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.taobao.weex.annotation.JSMethod;
import com.taobao.weex.bridge.JSCallback;
import com.taobao.weex.common.WXModule;
import com.taobao.weex.utils.WXLogUtils;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by Admin on 2017/9/21.
 */

public class WXFileUploadModule extends WXModule {
    private final String TAG = "wxupload";
    private JSCallback callback;
    private String path;
    private String url;
    private String formField;
    private String mimeType;

    private OkHttpClient client;

    private List<Map<String, Object>> list;

    @JSMethod
    public void upload(@NonNull Map<String, Object> options, @Nullable final JSCallback callback){
        this.list = new ArrayList<Map<String, Object>>();
        this.callback = callback;
        this.path = options.get("path").toString();
        this.url = options.get("url").toString();
        this.formField = options.get("formField").toString();
        if( options.get("mimeType") == null ){
            this.mimeType = "video/mpeg";
        } else {
            this.mimeType = options.get("mimeType").toString();
        }

        if( TextUtils.isEmpty(this.path) ) {
            callbackToWeex(-1, "上传文件path不能为空");
            return;
        }
        if( TextUtils.isEmpty(this.url) ) {
            callbackToWeex(-1, "上传路径url不能为空");
            return;
        }
        if( TextUtils.isEmpty(this.formField) ) {
            callbackToWeex(-1, "上传form域不能为空");
            return;
        }
        if( TextUtils.isEmpty(this.mimeType) ) {
            callbackToWeex(-1, "上传文件类型mimeType未知");
            return;
        }
        if( this.callback == null ) {
            callbackToWeex(-1, "回调方法为空");
            return;
        }

        //handler.postDelayed(runnable, 1000);

        this.uploadFile();
    }


    private void uploadFile(){
        File file = new File(path);
        if(!file.exists()) {
            callbackToWeex(-1, "文件不存在");
            return;
        }

        initClient();

        //进度监听回调
        final ProgressListener progressListener = new ProgressListener() {
            @Override
            public void onProgress(long bytesWrite, long contentLength, boolean done) {
                //callback.invoke(bytesWrite/contentLength);
                Log.e("TAG", (100 * bytesWrite) / contentLength + " % done ");
                double num1 = Double.parseDouble(String.valueOf(bytesWrite));
                double num2 = Double.parseDouble(String.valueOf(contentLength));
                String process = new DecimalFormat("0.0").format(num1/num2);
                callbackToWeex(1, process);
            }
        };

        //构造请求体，表单形式
        RequestBody requestBody = new MultipartBuilder()
                .type(MultipartBuilder.FORM)
                .addFormDataPart("name", this.formField)
                .addFormDataPart("file", file.getName(), RequestBody.create(MediaType.parse("application/octet-stream"), file))
                //.addPart(Headers.of("Content-Disposition", "form-data; name=\"another\";filename=\"another.dex\""), RequestBody.create(MediaType.parse("application/octet-stream"), file))
                .build();

        //建立请求
        final Request request = new Request.Builder()
                .url(url)
                .post(ProgressHelper.addProgressRequestListener(requestBody, progressListener))
                .build();

        //开始请求，异步
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.e("TAG", "error ", e);
                callbackToWeex(-1, "上传出错");
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if(!response.isSuccessful()) {
                    callbackToWeex(-1, "上传失败");
                    Log.e("TAG", "上传失败");
                }
                else {
                    String message = response.body().string();
                    callbackToWeex(0, message );
                    Log.e("TAG", "上传成功");
                }
            }
        });
    }

    private void initClient(){
        if(client == null){
            client = new OkHttpClient();
            client.setConnectTimeout(10, TimeUnit.SECONDS);
            client.setReadTimeout(500, TimeUnit.MINUTES);
            client.setWriteTimeout(500, TimeUnit.MINUTES);
        }
    }

    private void callbackToWeex(int flag, @Nullable String message) {
        Map<String, Object> resultMap = new HashMap<>();
        Map<String, Object> dataMap = new HashMap<>();
        switch (flag) {
            case -1 : {
                resultMap.put("result", "failure");
                if (!TextUtils.isEmpty(message)) {
                    dataMap.put("message", message);
                } else {
                    dataMap.put("message", "params missing");
                }
                resultMap.put("data", dataMap);
                break;
            }
            case 0:{
                resultMap.put("result", "success");
                JSONObject json = JSON.parseObject(message);
                resultMap.put("data", json);
                break;
            }
            case 1:{
                resultMap.put("result", "resume");
                dataMap.put("process", message);
                resultMap.put("data", dataMap);
                break;
            }
        }

        if(this.callback != null) {
            this.callback.invokeAndKeepAlive(resultMap);
        }
    }
}
