package org.zx_xjr.timemanagement.event;

import android.content.Context;
import android.text.format.DateUtils;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2016/12/6.
 */
public class Date {
    private Calendar calendar;

    public Date(int year, int month, int day) {
        calendar = Calendar.getInstance();
        calendar.set(year, month, day);
    }

    public Date(long time) {
        calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
    }

    public Calendar getCalendar() {
        return calendar;
    }

    public long getValue() {
        return calendar.getTimeInMillis();
    }

    public static int diff(Date date1, Date date2) {
        return (int) TimeUnit.DAYS.convert(date2.getValue() - date1.getValue(), TimeUnit.MILLISECONDS);
    }

    public String getLocale(Context context) {
        return DateUtils.formatDateTime(context, getValue(), DateUtils.FORMAT_SHOW_DATE);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Date)
            return diff(this, (Date) obj) == 0;
        return super.equals(obj);
    }

    /**
     * Returns the same value when two instances are equal in date.
     * Helps in {@link java.util.HashMap} usages.
     */
    @Override
    public int hashCode() {
        return calendar.get(Calendar.YEAR) * 10000 + calendar.get(Calendar.MONTH) * 100 + calendar.get(Calendar.DAY_OF_MONTH);
    }
}
