package com.alibaba.weex.extend.module;

import android.content.DialogInterface;

import com.alibaba.weex.extend.module.widget.picker.DateChooseWheelViewDialog;
import com.taobao.weex.WXSDKEngine;
import com.taobao.weex.annotation.JSMethod;
import com.taobao.weex.bridge.JSCallback;

import java.util.HashMap;
import java.util.Map;

/**
 * author: liminjie
 * date: 2017/6/15
 * desc: 日期选择器
 */

public class WXDatePickerModule extends WXSDKEngine.DestroyableModule {

    private DateChooseWheelViewDialog startDateChooseDialog;

    @JSMethod
    public void pickDateAndTime(Map<String, Object> options, final JSCallback callback) {
        Object value = options.get("value");
        Object min = options.get("min");

        startDateChooseDialog = new DateChooseWheelViewDialog(mWXSDKInstance.getUIContext(), new DateChooseWheelViewDialog.DateChooseInterface() {
            @Override
            public void getDateTime(String time) {
                Map<String, Object> ret = new HashMap<>();
                ret.put("result", "success");
                ret.put("data", time);
                callback.invoke(ret);
            }
        });
        startDateChooseDialog.setDateDialogTitle("请选择");
        startDateChooseDialog.setTitleColor("#da000f");
        startDateChooseDialog.setSurebtnColor("#da000f");
        startDateChooseDialog.showDateChooseDialog();
    }

    @Override
    public void destroy() {
        startDateChooseDialog.dismiss();
    }
}
