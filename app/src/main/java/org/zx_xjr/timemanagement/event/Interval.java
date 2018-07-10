package org.zx_xjr.timemanagement.event;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

/**
 * Created by Administrator on 2016/12/6.
 */
class Interval {
    private static final String[] UNITS = new String[]{"Day(s)", "Week(s)", "Month(s)"};
    private static final int DAY = 0, WEEK = 1, MONTH = 2;
    static final String NULL_INTERVAL = "Null interval";
    private Date start;

    Interval(int unit, int interval, Date start) {
        this.unit = unit;
        this.interval = interval;
        this.start = start;
    }

    int getInterval() {
        return interval;
    }

    void setInterval(int interval) {
        this.interval = interval;
    }

    int getUnit() {
        return unit;
    }

    void setUnit(int unit) {
        this.unit = unit;
    }

    String  getString() {
        return interval + " " + UNITS[unit];
    }

    void putToJson(JSONObject object) throws JSONException {
        object.put("interval", interval).put("unit", unit).put("intervalStart", start.getValue());
    }

    static Interval fromJson(JSONObject object) throws JSONException {
        if (object.has("intervalStart"))
            return new Interval(object.getInt("unit"), object.getInt("interval"), new Date(object.getLong("intervalStart")));
        else return null;
    }

    private int unit;
    private int interval;

    Date getStart() {
        return start;
    }

    static Date getNextDate(long start, Interval interval, long now) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(start);
        while (calendar.getTimeInMillis() < now) {
            add(calendar, interval);
        }
        return new Date(calendar.getTimeInMillis());
    }

    static void add(Calendar calendar, Interval interval) {
        switch (interval.getUnit()) {
            case DAY:
                calendar.add(Calendar.DATE, interval.getInterval());
                break;
            case WEEK:
                calendar.add(Calendar.DATE, 7 * interval.getInterval());
                break;
            case MONTH:
                calendar.add(Calendar.MONTH, interval.getInterval());
        }
    }
}
