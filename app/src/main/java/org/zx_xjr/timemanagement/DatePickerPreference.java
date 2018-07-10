package org.zx_xjr.timemanagement;

import android.content.Context;
import android.preference.DialogPreference;
import android.preference.Preference;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import org.zx_xjr.timemanagement.event.Date;

import java.util.Calendar;

public class DatePickerPreference extends DialogPreference {
    private DatePicker picker;
    private OnPreferenceChangeListener listener = new OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            return false;
        }
    };
    private int year, month, day;

    @Override
    protected View onCreateView(ViewGroup parent) {
        setSummary();
        return super.onCreateView(parent);
    }

    @Override
    protected View onCreateDialogView() {
        picker = new DatePicker(getContext());
        return picker;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        picker.updateDate(year, month, day);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            year = picker.getYear();
            month = picker.getMonth();
            day = picker.getDayOfMonth();

            listener.onPreferenceChange(this, getDate());

            setSummary();
        }
    }

    public DatePickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        Calendar calendar = Calendar.getInstance();
        setToCalendar(calendar);
    }

    private void setSummary() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        String string = DateUtils.formatDateTime(getContext(), calendar.getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE);
        persistString(string);
        setSummary(string);
    }

    Date getDate() {
        return new Date(year, month, day);
    }

    void setDate(Date date) {
        Calendar calendar = date.getCalendar();
        setToCalendar(calendar);
    }

    private void setToCalendar(Calendar calendar) {
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
    }

    public void setListener(OnPreferenceChangeListener listener) {
        this.listener = listener;
    }
}
