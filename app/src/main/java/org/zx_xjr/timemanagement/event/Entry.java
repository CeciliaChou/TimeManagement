package org.zx_xjr.timemanagement.event;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Provides an abstract model for {@link Event} and {@link Reminder}
 * classes, for they are similar in that they both model entries to
 * save. This makes convenience to {@link org.zx_xjr.timemanagement.entrymanager.ReminderManager}
 * and {@link org.zx_xjr.timemanagement.entrymanager.EventManager}
 * classes with reusable codes.
 */
public abstract class Entry {

    public abstract JSONObject toJson() throws JSONException;
}
