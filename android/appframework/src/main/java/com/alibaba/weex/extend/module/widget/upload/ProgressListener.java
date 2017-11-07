package com.alibaba.weex.extend.module.widget.upload;

/**
 * Created by Admin on 2017/9/21.
 */

public interface ProgressListener {
    void onProgress(long bytesWritten, long contentLength, boolean done);
}
