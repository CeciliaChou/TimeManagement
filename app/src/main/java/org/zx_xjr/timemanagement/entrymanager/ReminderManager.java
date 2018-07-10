package org.zx_xjr.timemanagement.entrymanager;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.zx_xjr.timemanagement.R;
import org.zx_xjr.timemanagement.ReminderActivity;
import org.zx_xjr.timemanagement.ReminderReceiver;
import org.zx_xjr.timemanagement.event.Entry;
import org.zx_xjr.timemanagement.event.Reminder;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ReminderManager extends EntryManager {
    private final static String FILE_NAME = "reminder.json";

    public final static String INTENT_EVENTS = "events";

    private void setAlarm(Context activity, int index, boolean on) {
        Reminder reminder = (Reminder) entryList.get(index);
        Intent intent = new Intent(activity, ReminderReceiver.class);
        intent.putExtra(INTENT_EVENTS, new ArrayList<>(reminder.getEvents()));
        PendingIntent pi = PendingIntent.getBroadcast(activity, reminder.getUuid().hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager manager = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
        if (on)
            manager.set(AlarmManager.RTC_WAKEUP, reminder.getTime().getValue(), pi);
        else {
            manager.cancel(pi);
            pi.cancel();
        }
    }

    private boolean isAlarmSet(Context activity, int index) {
        Reminder reminder = (Reminder) entryList.get(index);
        Intent intent = new Intent();
        intent.setClass(activity, ReminderReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(activity, reminder.getUuid().hashCode(), intent, PendingIntent.FLAG_NO_CREATE);
        return pi != null;
    }

    @Override
    protected BaseAdapter newAdapter(final Activity context) {
        return new BaseAdapter() {
            @Override
            public int getCount() {
                return entryList.size();
            }

            @Override
            public Object getItem(int position) {
                return entryList.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                if (convertView == null)
                    convertView = context.getLayoutInflater().inflate(R.layout.reminder_item, null);
                Reminder reminder = (Reminder) entryList.get(position);
                ((TextView) convertView.findViewById(R.id.reminder_time)).setText(reminder.getTimeLocale(context));
                ((TextView) convertView.findViewById(R.id.reminder_label)).setText(reminder.getLabel());
                String string = context.getResources().getText(R.string.next_reminder_date) +
                        reminder.getTime().getLocale(context);
                ((TextView) convertView.findViewById(R.id.reminder_interval)).setText(string);

                Switch theSwitch = (Switch) convertView.findViewById(R.id.reminder_switch);
                theSwitch.setChecked(isAlarmSet(context, position));
                theSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        setAlarm(context, position, isChecked);
                        System.out.println(isAlarmSet(context, position));
                    }
                });
                return convertView;
            }
        };
    }

    @Override
    protected Entry fromJson(JSONObject object) throws JSONException {
        return new Reminder(object);
    }

    @Override
    protected String fileName() {
        return FILE_NAME;
    }

    @Override
    public void deleteEntry(Context activity, int index) {
        setAlarm(activity, index, false);
        super.deleteEntry(activity, index);
    }

    @Override
    protected void editEntry(Context activity, int index, Bundle bundle) {
        setAlarm(activity, index, true);
    }
}
