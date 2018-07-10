package org.zx_xjr.timemanagement.event;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.UUID;

/**
 * Created by Administrator on 2016/12/1.
 */
public class Event extends Entry {
    String label;
    protected UUID uuid;
    private boolean finished;

    public static Event fromJson(JSONObject object) throws JSONException {
        String type = object.getString("type");
        switch (type) {
            case "event":
                return new Event(object);
            case "practice":
                return new Practice(object);
            case "task":
                return new Task(object);
            case "segment":
                return new SegmentTask(object);
        }
        return null;
    }

    public Event(String label) {
        this.label = label;
        finished = false;
        uuid = UUID.randomUUID();
    }

    Event(JSONObject object) throws JSONException {
        label = object.getString("label");
        uuid = UUID.fromString(object.getString("uuid"));
        finished = object.getBoolean("finished");
    }

    @Override
    public JSONObject toJson() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("label", label).put("modified", System.currentTimeMillis())
                .put("finished", finished).put("type", "event")
                .put("uuid", uuid);
        return object;
    }

    public Map<String, Object> getMap(Context context) {
        return null;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public UUID getUUID() {
        return uuid;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public boolean isFinished() {
        return finished;
    }
}
