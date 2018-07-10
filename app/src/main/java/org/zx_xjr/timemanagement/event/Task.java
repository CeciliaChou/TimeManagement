package org.zx_xjr.timemanagement.event;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;
import org.zx_xjr.timemanagement.R;

/**
 * Created by Administrator on 2016/12/6.
 */
public class Task extends Event {
    Date start, expire;
    boolean finished;
    Interval interval;

    public Task(String label, Date start, Date expire) {
        super(label);
        this.start = start;
        this.expire = expire;
        finished = false;
    }

    public Task(String label, Date start, Date expire, int interval, int unit, Date intervalStart) {
        this(label, start, expire);
        this.interval = new Interval(unit, interval, intervalStart);
    }

    public Task(Event event, Date start, Date expire) {
        this(event.getLabel(), start, expire);
        System.out.println("start: " + start.getValue());
        this.uuid = event.uuid;
    }

    Task(JSONObject object) throws JSONException {
        super(object);
        interval = Interval.fromJson(object);
        start = new Date(object.getLong("start"));
        expire = new Date(object.getLong("expire"));
        finished = object.getBoolean("finished");
    }

    public JSONObject toJson() throws JSONException {
        JSONObject object = super.toJson();
        object.put("start", start.getValue()).put("expire", expire.getValue()).put("finished", finished).put("type", "task");
        if (interval != null)
            interval.putToJson(object);
        return object;
    }

    public Date getStart() {
        return start;
    }

    public Date getExpire() {
        return expire;
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public boolean isDateInRange(long now) {
        Date date = new Date(now);
        return Date.diff(start, date) >= 0 && Date.diff(date, expire) >= 0 && !finished;
    }

    public String getDesc(Context context) {
        StringBuilder builder = new StringBuilder();
        builder.append(context.getResources().getString(R.string.expire_date_with_colon))
                .append(expire.getLocale(context))
                .append(context.getResources().getString(R.string.separator));
        int diff = Date.diff(new Date(System.currentTimeMillis()), expire);
        if (diff > 0)
            builder.append(context.getResources().getString(R.string.days_left))
                    .append(diff).append(context.getResources().getString(R.string.days));
        else if (diff == 0)
            builder.append(context.getResources().getString(R.string.due_today));
        else
            builder.append(context.getResources().getString(R.string.expired));
        return builder.toString();
    }

    public int getDuration() {
        return Date.diff(start, expire);
    }

    public int getProgress() {
        return Date.diff(start, new Date(System.currentTimeMillis()));
    }
}
