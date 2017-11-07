package com.alibaba.weex.extend.module.widget.upload;

import com.squareup.okhttp.RequestBody;

/**
 * Created by Admin on 2017/9/21.
 */

public class ProgressHelper {

    /**
     * 包装请求体用于上传文件的回调
     * @param requestBody 请求体RequestBody
     * @param progressListener 进度回调接口
     * @return 包装后的进度回调请求体
     */
    public static ProgressBody addProgressRequestListener(RequestBody requestBody, ProgressListener progressListener){
        //包装请求体
        return new ProgressBody(requestBody,progressListener);
    }
}
