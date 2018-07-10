package org.zx_xjr.timemanagement.event;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/12/6.
 */
public class SegmentTask extends Task {
    private List<Date> dates;
    private List<String> labels;
    private int subTasks;

    public SegmentTask(String label, Date start, Date expire, List<Date> dates, List<String> labels) {
        super(label, start, expire);
        this.dates = dates;
        this.labels = labels;
        subTasks = labels.size();
    }

    public SegmentTask(String label, Date start, Date expire, List<Date> dates, List<String> labels, int interval, int unit, Date intervalStart) {
        this(label, start, expire, dates, labels);
        this.interval = new Interval(unit, interval, intervalStart);
    }

    SegmentTask(JSONObject object) throws JSONException {
        super(object);
        JSONArray array = object.getJSONArray("dates");
        int l = array.length();
        dates = new ArrayList<>();
        for (int i = 0; i < l; i++)
            dates.add(new Date(array.getLong(i)));
        array = object.getJSONArray("labels");
        l = array.length();
        labels = new ArrayList<>();
        for (int i = 0; i < l; i++)
            labels.add(array.getString(i));
    }

    public JSONObject toJson() throws JSONException {
        JSONObject object = super.toJson();
        int l = dates.size();
        List<Long> dateList = new ArrayList<>();
        for (Date date: dates)
            dateList.add(date.getValue());
        object.put("dates", new JSONArray(dateList)).put("labels", new JSONArray(labels)).put("type", "segment");
        return object;
    }
}
