package org.zx_xjr.timemanagement.event;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;
import org.zx_xjr.timemanagement.R;

import java.util.*;

import static org.zx_xjr.timemanagement.event.Interval.NULL_INTERVAL;

/**
 * Created by Administrator on 2016/12/6.
 */
public class Practice extends Event {
    private Interval interval;

    public Practice(String label, int interval, int unit, Date start) {
        super(label);
        this.interval = new Interval(unit, interval, start);
    }

    public Practice(Event event, int interval, int unit, Date start) {
        this(event.getLabel(), interval, unit, start);
        this.uuid = event.uuid;
    }

    Practice(JSONObject object) throws JSONException {
        super(object);
        interval = Interval.fromJson(object);
        if (interval == null) throw new JSONException(NULL_INTERVAL);
    }

    public JSONObject toJson() throws JSONException {
        JSONObject object = super.toJson();
        interval.putToJson(object);
        object.put("type", "practice");
        return object;
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

    public Date getNext(long now) {
        Calendar calendar = interval.getStart().getCalendar();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return Interval.getNextDate(calendar.getTimeInMillis(), interval, now);
    }

    public List<Date> getDueList(Date start) {
        List<Date> list = new ArrayList<>();
        long now = System.currentTimeMillis();
        Date next = getNext(start.getValue());
        if (Date.diff(start, next) != 0)
            list.add(next);
        Calendar calendar = next.getCalendar();
        while (true) {
            Interval.add(calendar, interval);
            list.add(new Date(calendar.getTimeInMillis()));
            if (calendar.getTimeInMillis() > now) break;
        }
        return list;
    }

    public String getDesc(Context context) {
        return context.getResources().getString(R.string.next_practice_date) +
                getNext(System.currentTimeMillis()).getLocale(context);
    }
}
