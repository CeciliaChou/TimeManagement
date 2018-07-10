package org.zx_xjr.timemanagement.event;

import android.content.Context;
import android.text.format.DateUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

import static org.zx_xjr.timemanagement.event.Interval.NULL_INTERVAL;

/**
 * Created by Administrator on 2016/12/6.
 */
public class Reminder extends Entry {
    private UUID uuid;
    private String label;
    private Set<UUID> events;
    private long time;
    private Interval interval;
    public static final String TIME = "time", LABEL = "label", INTERVAL = "interval";

    public Reminder(String label, int hour, int minute, int interval, int unit, Date start) {
        this.label = label;
        uuid = UUID.randomUUID();
        events = new HashSet<>();
        setInterval(interval, unit, start);
        setHourMinute(hour, minute);
    }

    public int getIntervalValue() {
        return interval.getInterval();
    }

    public int getIntervalUnit() {
        return interval.getUnit();
    }

    public Date getIntervalStart() {
        return interval.getStart();
    }

    public String getLabel() {
        return label;
    }

    public void setInterval(int interval, int unit, Date start) {
        this.interval = new Interval(unit, interval, start);
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Reminder(JSONObject object) throws JSONException {
        label = object.getString("label");
        uuid = UUID.fromString(object.getString("uuid"));
        time = object.getLong("time");
        JSONArray array = object.getJSONArray("events");
        events = new HashSet<>();
        int l = array.length();
        for (int i = 0; i < l; i++)
            events.add(UUID.fromString(array.getString(i)));
        interval = Interval.fromJson(object);
        //if (interval == null) throw new JSONException(NULL_INTERVAL);
    }

    public void addEvent(UUID uuid) {
        events.add(uuid);
    }

    public void removeEvent(UUID uuid) {
        events.remove(uuid);
    }

    public Set<String> getEvents() {
        Set<String> strings = new HashSet<>();
        for (UUID uuid: events)
            strings.add(uuid.toString());
        return strings;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getTimeLocale(Context context) {
        return DateUtils.formatDateTime(context, time, DateUtils.FORMAT_SHOW_TIME);
    }

    public String getInterval() {
        return interval.getString();
    }

    @Override
    public JSONObject toJson() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("label", label).put("uuid", uuid.toString()).put("time", time);
        JSONArray array = new JSONArray();
        for (UUID uuid: events)
            array.put(uuid.toString());
        object.put("events", array);
        if (interval != null)
            interval.putToJson(object);
        return object;
    }

    public void setHourMinute(int hour, int minute) {
        Calendar calendar = interval.getStart().getCalendar();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        this.time = calendar.getTimeInMillis();
    }

    public Date getTime() {
        return Interval.getNextDate(time, interval, System.currentTimeMillis());
    }
}
