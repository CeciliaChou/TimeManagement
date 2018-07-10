package org.zx_xjr.timemanagement;

import android.content.Context;
import android.os.Build;
import android.preference.DialogPreference;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TimePicker;

import java.util.Calendar;

public class TimePickerPreference extends DialogPreference {
    private TimePicker picker;
    private int lastHour = 20, lastMinute = 0;

    @Override
    protected View onCreateView(ViewGroup parent) {
        setSummary();
        return super.onCreateView(parent);
    }

    @Override
    protected View onCreateDialogView() {
        picker = new TimePicker(getContext());
        return picker;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            picker.setHour(lastHour);
            picker.setMinute(lastMinute);
        } else {
            picker.setCurrentHour(lastHour);
            picker.setCurrentMinute(lastMinute);
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                lastHour = picker.getHour();
                lastMinute = picker.getMinute();
            } else {
                lastHour = picker.getCurrentHour();
                lastMinute = picker.getCurrentMinute();
            }

            setSummary();
        }
    }

    public TimePickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setPositiveButtonText(context.getResources().getString(android.R.string.ok));
        setNegativeButtonText(context.getResources().getString(android.R.string.cancel));
    }

    private void setSummary() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.MINUTE, lastMinute);
        calendar.set(Calendar.HOUR_OF_DAY, lastHour);
        String string = DateUtils.formatDateTime(getContext(), calendar.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME);
        persistString(string);
        setSummary(string);
    }

    void setTime(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        lastHour = calendar.get(Calendar.HOUR_OF_DAY);
        lastMinute = calendar.get(Calendar.MINUTE);
    }

    public int getLastHour() {
        return lastHour;
    }

    public int getLastMinute() {
        return lastMinute;
    }
}
