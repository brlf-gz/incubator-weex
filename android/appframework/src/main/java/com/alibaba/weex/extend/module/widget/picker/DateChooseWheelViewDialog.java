package com.alibaba.weex.extend.module.widget.picker;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.weex.appframework.R;
import com.alibaba.weex.extend.module.widget.picker.widget.OnWheelChangedListener;
import com.alibaba.weex.extend.module.widget.picker.widget.OnWheelScrollListener;
import com.alibaba.weex.extend.module.widget.picker.widget.WheelView;
import com.alibaba.weex.extend.module.widget.picker.widget.adapters.AbstractWheelTextAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class DateChooseWheelViewDialog extends Dialog implements View.OnClickListener {

    //使用WheelView的组合
    private WheelView mDateWheelView;
    private WheelView mTimeWheelView;
    private WheelView mHourWheelView;
    private WheelView mMinuteWheelView;

    //WheelView的适配器
    private CalendarTextAdapter mDateAdapter;
    private CalendarTextAdapter mTimeAdapter;
    private CalendarTextAdapter mHourAdapter;
    private CalendarTextAdapter mMinuteAdapter;

    //对话框，对话框标题，确定按钮，关闭按钮
    private Dialog mDialog;
    private TextView mTitleTextView;
    private Button mSureButton;
    private Button mCloseDialog;

    //变量
    private ArrayList<String> arry_date = new ArrayList<>();
    private ArrayList<String> arry_time = new ArrayList<>();
    private ArrayList<String> arry_hour = new ArrayList<>();
    private ArrayList<String> arry_minute = new ArrayList<>();

    //id
    private int nowDateId = 0;
    private int nowTimeId = 0;
    private int nowHourId = 0;
    private int nowMinuteId = 0;

    //对应字符串变量
    private String mDateStr;
    private String mTimeStr;
    private String mHourStr;
    private String mMinuteStr;
    private String mYearStr;

    private String[] weekDays = {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};

    //常量
    private final int MAX_TEXT_SIZE = 16;
    private final int MIN_TEXT_SIZE = 14;

    private Context mContext;
    private DateChooseInterface dateChooseInterface;

    //////////////////////增加的变量/////////////////////////
    private LinearLayout ll_title;
    /////////////////////////////////////////////////

    public DateChooseWheelViewDialog(Context context, DateChooseInterface dateChooseInterface) {
        super(context);
        this.mContext = context;
        this.dateChooseInterface = dateChooseInterface;
        mDialog = new Dialog(context, R.style.dialog);
        initView();
        initData();
    }

    //初始化视图
    private void initView() {
        View view = View.inflate(mContext, R.layout.appframework_dialog_date_choose, null);
//        View view = LayoutInflater.from(mContext).inflate(R.layout.appframework_dialog_date_choose, null);
        mDialog.setContentView(view);

        mDateWheelView = (WheelView) view.findViewById(R.id.date_wv);
        mTimeWheelView = (WheelView) view.findViewById(R.id.time_wv);
        mHourWheelView = (WheelView) view.findViewById(R.id.hour_wv);
        mMinuteWheelView = (WheelView) view.findViewById(R.id.minute_wv);

        mDateWheelView.setCyclic(true);
        mTimeWheelView.setCyclic(false);
        mHourWheelView.setCyclic(true);
        mMinuteWheelView.setCyclic(true);

        mTitleTextView = (TextView) view.findViewById(R.id.title_tv);
        mSureButton = (Button) view.findViewById(R.id.sure_btn);
        mCloseDialog = (Button) view.findViewById(R.id.date_choose_close_btn);
        ll_title = (LinearLayout) view.findViewById(R.id.ll_title);

        mSureButton.setOnClickListener(this);
        mCloseDialog.setOnClickListener(this);
    }

    //初始化数据
    private void initData() {
        initDate();
        initTime();
        initHour();
        initMinute();
        initListener();

    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.sure_btn) {

            dateChooseInterface.getDateTime(strTimeToDateFormat(mYearStr, mDateStr, mTimeStr, mHourStr, mMinuteStr));
            dismissDialog();

        } else if (i == R.id.date_choose_close_btn) {
            dismissDialog();

        } else {
        }
    }

    //初始化日期
    private void initDate() {
        Calendar nowCalendar = Calendar.getInstance();
        int nowYear = nowCalendar.get(Calendar.YEAR);
        mYearStr = String.valueOf(nowYear);
        arry_date.clear();
        setDate(nowYear);
        setDate(nowYear+1);
        mDateAdapter = new CalendarTextAdapter(mContext, arry_date, nowDateId, MAX_TEXT_SIZE, MIN_TEXT_SIZE);
        mDateWheelView.setViewAdapter(mDateAdapter);
        mDateWheelView.setVisibleItems(7);
        mDateWheelView.setCurrentItem(nowDateId);
        mDateStr = arry_date.get(nowDateId);
        setTextViewStyle(mDateStr, mDateAdapter);

    }

    //初始化上午/下午
    private void initTime() {
        arry_time.clear();
        arry_time.add("上午");
        arry_time.add("下午");
        mTimeAdapter = new CalendarTextAdapter(mContext, arry_time, nowTimeId, MAX_TEXT_SIZE, MIN_TEXT_SIZE);
        mTimeWheelView.setViewAdapter(mTimeAdapter);
        mTimeWheelView.setVisibleItems(2);//显示的item数量

    }

    //初始化时间
    private void initHour() {
        Calendar nowCalendar = Calendar.getInstance();
        int nowHour = nowCalendar.get(Calendar.HOUR_OF_DAY);

        if (nowHour < 12) {
            nowTimeId = 0;
        } else {
            nowTimeId = 1;
        }
        mTimeWheelView.setCurrentItem(nowTimeId);//设置当前的item
        mTimeStr = arry_time.get(nowTimeId);
        setTextViewStyle(mTimeStr, mTimeAdapter);

        arry_hour.clear();
        for (int i = 0; i < 12; i++) {
            arry_hour.add(i + "");
            if (nowHour % 12 == i) {
                nowHourId = arry_hour.size() - 1;
            }
        }

        mHourAdapter = new CalendarTextAdapter(mContext, arry_hour, nowHourId, MAX_TEXT_SIZE, MIN_TEXT_SIZE);
        mHourWheelView.setVisibleItems(7);
        mHourWheelView.setViewAdapter(mHourAdapter);
        mHourWheelView.setCurrentItem(nowHourId);
        mHourStr = arry_hour.get(nowHourId) + "";
        setTextViewStyle(mHourStr, mHourAdapter);
    }

    //初始化分钟
    private void initMinute() {
        Calendar nowCalendar = Calendar.getInstance();
        int nowMinite = nowCalendar.get(Calendar.MINUTE);
        arry_minute.clear();
        for (int i = 0; i <= 59; i++) {
            arry_minute.add(i + "");
            if (nowMinite == i) {
                nowMinuteId = arry_minute.size() - 1;
            }
        }

        mMinuteAdapter = new CalendarTextAdapter(mContext, arry_minute, nowMinuteId, MAX_TEXT_SIZE, MIN_TEXT_SIZE);
        mMinuteWheelView.setVisibleItems(7);
        mMinuteWheelView.setViewAdapter(mMinuteAdapter);
        mMinuteWheelView.setCurrentItem(nowMinuteId);
        mMinuteStr = arry_minute.get(nowMinuteId) + "";
        setTextViewStyle(mMinuteStr, mMinuteAdapter);
    }

    /*************初始化WheelView滚动监听事件***************/
    private void initListener() {

        //第一个

        mDateWheelView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                String currentText = (String) mDateAdapter.getItemText(mDateWheelView.getCurrentItem());
                setTextViewStyle(currentText, mDateAdapter);
            }
        });

        mDateWheelView.addChangingListener(new OnWheelChangedListener() {

            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                String currentText = (String) mDateAdapter.getItemText(wheel.getCurrentItem());
                setTextViewStyle(currentText, mDateAdapter);
                mDateStr = arry_date.get(wheel.getCurrentItem());
            }
        });

        mDateWheelView.addScrollingListener(new OnWheelScrollListener() {
            @Override
            public void onScrollingStarted(WheelView wheel) {
            }

            @Override
            public void onScrollingFinished(WheelView wheel) {
                String currentText = (String) mDateAdapter.getItemText(wheel.getCurrentItem());
                setTextViewStyle(currentText, mDateAdapter);
            }
        });


        //第二个
        mTimeWheelView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                String currentText = (String) mTimeAdapter.getItemText(mTimeWheelView.getCurrentItem());
                setTextViewStyle(currentText, mTimeAdapter);
            }
        });

        mTimeWheelView.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                String currentText = (String) mTimeAdapter.getItemText(wheel.getCurrentItem());
                setTextViewStyle(currentText, mTimeAdapter);
                mTimeStr = arry_time.get(wheel.getCurrentItem()) + "";
            }
        });

        mTimeWheelView.addScrollingListener(new OnWheelScrollListener() {
            @Override
            public void onScrollingStarted(WheelView wheel) {
            }

            @Override
            public void onScrollingFinished(WheelView wheel) {
                String currentText = (String) mTimeAdapter.getItemText(wheel.getCurrentItem());
                setTextViewStyle(currentText, mTimeAdapter);
            }
        });


        //第三个

        mHourWheelView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                String currentText = (String) mHourAdapter.getItemText(mHourWheelView.getCurrentItem());
                setTextViewStyle(currentText, mHourAdapter);
            }
        });

        mHourWheelView.addChangingListener(new OnWheelChangedListener() {

            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                String currentText = (String) mHourAdapter.getItemText(wheel.getCurrentItem());
                setTextViewStyle(currentText, mHourAdapter);
                mHourStr = arry_hour.get(wheel.getCurrentItem()) + "";
            }
        });

        mHourWheelView.addScrollingListener(new OnWheelScrollListener() {
            @Override
            public void onScrollingStarted(WheelView wheel) {
            }

            @Override
            public void onScrollingFinished(WheelView wheel) {
                String currentText = (String) mHourAdapter.getItemText(wheel.getCurrentItem());
                setTextViewStyle(currentText, mHourAdapter);
            }
        });

        //第四个
        mMinuteWheelView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                String currentText = (String) mMinuteAdapter.getItemText(mMinuteWheelView.getCurrentItem());
                setTextViewStyle(currentText, mMinuteAdapter);
            }
        });

        mMinuteWheelView.addChangingListener(new OnWheelChangedListener() {

            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                String currentText = (String) mMinuteAdapter.getItemText(wheel.getCurrentItem());
                setTextViewStyle(currentText, mMinuteAdapter);
                mMinuteStr = arry_minute.get(wheel.getCurrentItem()) + "";
            }
        });

        mMinuteWheelView.addScrollingListener(new OnWheelScrollListener() {
            @Override
            public void onScrollingStarted(WheelView wheel) {
            }

            @Override
            public void onScrollingFinished(WheelView wheel) {
                String currentText = (String) mMinuteAdapter.getItemText(wheel.getCurrentItem());
                setTextViewStyle(currentText, mMinuteAdapter);
            }
        });

    }

    //设置title
    public void setDateDialogTitle(String title) {
        mTitleTextView.setText(title);
    }

    // 将该年的所有日期写入数组
    private void setDate(int year) {
        boolean isRun = isRunNian(year);
        String week = "";
        Calendar nowCalendar = Calendar.getInstance();
        int nowYear = nowCalendar.get(Calendar.YEAR);
        int nowMonth = nowCalendar.get(Calendar.MONTH) + 1;
        int nowDay = nowCalendar.get(Calendar.DAY_OF_MONTH);
        for (int month = 1; month <= 12; month++) {
            switch (month) {
                case 1:
                case 3:
                case 5:
                case 7:
                case 8:
                case 10:
                case 12:
                    for (int day = 1; day <= 31; day++) {
                        week = dayForWeek(year + "-" + month + "-" + day);
                        arry_date.add(year + "年" + month + "月" + day + "日 " + week);
                        if (year == nowYear && month == nowMonth && day == nowDay) {
                            nowDateId = arry_date.size() - 1;
                            arry_date.set(nowDateId, "今天");
                        }
                    }
                    break;
                case 2:
                    if (isRun) {
                        for (int day = 1; day <= 29; day++) {
                            week = dayForWeek(year + "-" + month + "-" + day);
                            arry_date.add(year + "年" + month + "月" + day + "日 " + week);
                            if (year == nowYear && month == nowMonth && day == nowDay) {
                                nowDateId = arry_date.size() - 1;
                                arry_date.set(nowDateId, "今天");
                            }
                        }
                    } else {
                        for (int day = 1; day <= 28; day++) {
                            week = dayForWeek(year + "-" + month + "-" + day);
                            arry_date.add(year + "年" + month + "月" + day + "日 " + week);
                            if (year == nowYear && month == nowMonth && day == nowDay) {
                                nowDateId = arry_date.size() - 1;
                                arry_date.set(nowDateId, "今天");
                            }
                        }
                    }
                    break;
                case 4:
                case 6:
                case 9:
                case 11:
                    for (int day = 1; day <= 30; day++) {
                        week = dayForWeek(year + "-" + month + "-" + day);
                        arry_date.add(year + "年" + month + "月" + day + "日 " + week);
                        if (year == nowYear && month == nowMonth && day == nowDay) {
                            nowDateId = arry_date.size() - 1;
                            arry_date.set(nowDateId, "今天");
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 判断是否是闰年
     *
     * @param year
     * @return
     */
    private boolean isRunNian(int year) {
        if (year % 4 == 0 && year % 100 != 0 || year % 400 == 0) {
            return true;
        } else {
            return false;
        }
    }

    //计算输入的日期数据对应的周几。
    public String dayForWeek(String pTime) {
        Calendar cal = Calendar.getInstance();
        int i = -1;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date date;
            date = dateFormat.parse(pTime);
            cal.setTime(date);
            i = cal.get(Calendar.DAY_OF_WEEK);
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        return weekDays[i - 1];
    }

    /**
     * 设置文字的大小
     *
     * @param curriteItemText
     * @param adapter
     */
    public void setTextViewStyle(String curriteItemText, CalendarTextAdapter adapter) {
        ArrayList<View> arrayList = adapter.getTestViews();
        int size = arrayList.size();
        String currentText;
        for (int i = 0; i < size; i++) {
            TextView textvew = (TextView) arrayList.get(i);
            currentText = textvew.getText().toString();
            if (curriteItemText.equals(currentText)) {
                textvew.setTextSize(MAX_TEXT_SIZE);
                textvew.setTextColor(ContextCompat.getColor(mContext, R.color.text_10));
            } else {
                textvew.setTextSize(MIN_TEXT_SIZE);
                textvew.setTextColor(ContextCompat.getColor(mContext, R.color.text_11));
            }
        }
    }

    //对话框消失
    private void dismissDialog() {

        if (Looper.myLooper() != Looper.getMainLooper()) {

            return;
        }

        if (null == mDialog || !mDialog.isShowing() || null == mContext
                || ((Activity) mContext).isFinishing()) {

            return;
        }

        mDialog.dismiss();
        this.dismiss();
    }

    //显示日期选择dialog
    public void showDateChooseDialog() {

        if (Looper.myLooper() != Looper.getMainLooper()) {

            return;
        }

        if (null == mContext || ((Activity) mContext).isFinishing()) {

            // 界面已被销毁
            return;
        }

        if (null != mDialog) {

            mDialog.show();

            return;
        }

        if (null == mDialog) {

            return;
        }

        mDialog.setCanceledOnTouchOutside(true);
        mDialog.show();

    }

    //转换格式
    private String strTimeToDateFormat(String yearStr, String dateStr, String timeStr, String hourStr, String minuteStr) {


        //yyyy-MM-dd HH:mm

        int year = Integer.valueOf(yearStr);
        int month = 0;
        int day = 0;
        int hour = 0;
        int minute = 0;

        if (TextUtils.equals(dateStr, "今天")) {
            Calendar calendar = Calendar.getInstance();
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH) + 1;
            day = calendar.get(Calendar.DAY_OF_MONTH);
        } else {
            int yearIndex = dateStr.indexOf("年");
            int monthIndex = dateStr.indexOf("月");
            int dayIndex = dateStr.indexOf("日");
            year = Integer.parseInt(dateStr.substring(0, yearIndex));
            month = Integer.parseInt(dateStr.substring(yearIndex + 1, monthIndex));
            day = Integer.parseInt(dateStr.substring(monthIndex + 1, dayIndex));
        }

        if (TextUtils.equals(timeStr, "下午")) {
            hour = 12 + Integer.parseInt(hourStr);
        } else {
            hour = Integer.parseInt(hourStr);

        }

        minute = Integer.parseInt(minuteStr);

        return year + "-" + (month < 10 ? "0" : "") + month + "-" + (day < 10 ? "0" : "") + day + " " + (hour < 10 ? "0" : "") + hour + ":" + (minute < 10 ? "0" : "") + minute;
    }

    //滚轮的adapter
    private class CalendarTextAdapter extends AbstractWheelTextAdapter {
        ArrayList<String> list;

        protected CalendarTextAdapter(Context context, ArrayList<String> list, int currentItem, int maxsize, int minsize) {
            super(context, R.layout.appframework_item_birth_year, R.id.tempValue, currentItem, maxsize, minsize);
            this.list = list;
        }

        @Override
        public View getItem(int index, View cachedView, ViewGroup parent) {
            View view = super.getItem(index, cachedView, parent);
            return view;
        }

        @Override
        public int getItemsCount() {
            return list.size();
        }

        @Override
        protected CharSequence getItemText(int index) {
            String str = list.get(index) + "";
            return str;
        }
    }

    //回调选中的时间
    public interface DateChooseInterface {
        void getDateTime(String time);
    }


    //////////////////////////////////////////////////////
    public void setTitleColor(String color){
        if(!TextUtils.isEmpty(color)) {
            this.ll_title.setBackgroundColor(Color.parseColor(color));
        }
    }

    public void setSurebtnColor(String color) {
        if(!TextUtils.isEmpty(color)) {
            this.mSureButton.setBackgroundColor(Color.parseColor(color));
        }
    }


}
