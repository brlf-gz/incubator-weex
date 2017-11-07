package com.alibaba.weex.extend.module.widget.upload;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;
import com.taobao.weex.utils.WXLogUtils;

import java.io.IOException;
import java.text.DecimalFormat;

import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

/**
 * Created by Admin on 2017/9/21.
 * 包装请求体，使支持进度回调
 */

public class ProgressBody extends RequestBody {

    private final RequestBody requestBody; //待包装的请求体
    private final ProgressListener progressListener; //进度回调接口
    private BufferedSink bufferedSink;  //包装完成的BufferedSink
    private String process = "";


    public ProgressBody(RequestBody requestBody, ProgressListener progressListener) {
        this.requestBody = requestBody;
        this.progressListener = progressListener;
    }

    //重写调用实际的响应体的contentType
    @Override
    public MediaType contentType() {
        return requestBody.contentType();
    }

    //重写调用实际的响应体的contentLength
    @Override
    public long contentLength() throws IOException {
        return requestBody.contentLength();
    }


    /**
     * 重写进行写入
     * @param sink BufferedSink
     * @throws IOException 异常
     */
    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        if (bufferedSink == null) {
            bufferedSink = Okio.buffer(sink(sink));
        }
        //写入
        requestBody.writeTo(bufferedSink);
        bufferedSink.flush(); //必须flush
    }


    /**
     * 写入，回调进度接口
     * @param sink Sink
     * @return Sink
     */
    private Sink sink(Sink sink) {
        return new ForwardingSink(sink) {
            //当前写入字节数
            long bytesWritten = 0L;
            //总字节长度，避免多次调用contentLength()方法
            long contentLength = 0L;

            @Override
            public void write(Buffer source, long byteCount) throws IOException {
                super.write(source, byteCount);
                if (contentLength == 0) {
                    //获得contentLength的值，后续不再调用
                    contentLength = contentLength();
                }
                //增加当前写入的字节数
                bytesWritten += byteCount;
                //回调

                double num1 = Double.parseDouble(String.valueOf(bytesWritten));
                double num2 = Double.parseDouble(String.valueOf(contentLength));
                String process2 = new DecimalFormat("0.0").format(num1/num2);

                if(!process.equals(process2))
                {
                    process=process2;
                    WXLogUtils.e("fuck",process2);
                    progressListener.onProgress(bytesWritten, contentLength, bytesWritten == contentLength);
                }
            }
        };
    }
}
