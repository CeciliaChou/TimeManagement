package org.zx_xjr.timemanagement.entrymanager;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.zx_xjr.timemanagement.EntryFragment;
import org.zx_xjr.timemanagement.MainActivity;
import org.zx_xjr.timemanagement.R;
import org.zx_xjr.timemanagement.event.*;
import org.zx_xjr.timemanagement.event.Date;

import java.io.IOException;
import java.util.*;

/**
 * Created by Administrator on 2016/12/10.
 */
public class EventManager extends EntryManager {
    private final static String FILE_NAME = "event.json";
    public final static String PRACTICE_MARK = "practice";
    public final static String NOTIF = "event";
    private PracticeMarkManager markManager = new PracticeMarkManager();

    @Override
    public void loadEntry(Activity activity) throws IOException, JSONException {
        super.loadEntry(activity);
        markManager.loadEntry(activity);
    }

    /**
     * This method provides a helper method to set UI update at midnight.
     * Useful for fragments and activities related to event.
     * @param activity Used to retrieve the UI thread
     * @param runnable Updating task
     */
    public static void setEventUpdate(final Activity activity, final Runnable runnable) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                activity.runOnUiThread(runnable);
            }
        }, calendar.getTime(), 86400000);
    }

    public void setUIUpdate(final Activity activity, final ListView layout) {
        setEventUpdate(activity, new Runnable() {
            @Override
            public void run() {
                try {
                    update(layout);
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void saveEntry(Context activity) throws IOException, JSONException {
        super.saveEntry(activity);
        markManager.saveEntry(activity);
    }

    public void filter(ArrayList<String> uuids, long now) {
        for (Entry entry : entryList) {
            if (!uuids.contains(((Event) entry).getUUID().toString()) ||
                    (entry instanceof Task && !((Task) entry).isDateInRange(now)) ||
                    (entry.getClass() == Event.class && ((Event) entry).isFinished()))
                entryList.remove(entry);
        }
    }

    private void setNotification(Context activity, int index) {
        Event event = (Event) entryList.get(index);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(activity)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(event.getLabel());

        if (event instanceof Practice) builder.setContentText(((Practice) event).getDesc(activity));
        if (event instanceof Task) {
            Task task = (Task) event;
            builder.setContentText(task.getDesc(activity))
                    .setProgress(task.getDuration(), task.getProgress(), false);
        }

        Intent intent = new Intent(activity, MainActivity.class);
        intent.putExtra(NOTIF, event.getUUID().toString());

        PendingIntent pendingIntent = PendingIntent.getActivity(
                activity,
                event.getUUID().hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        builder.setContentIntent(pendingIntent);
        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;

        NotificationManager manager =
                (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(event.getUUID().hashCode(), notification);
    }

    private void cancelNotification(Context activity, int index) {
        NotificationManager manager =
                (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(((Event) entryList.get(index)).getUUID().hashCode());

        Intent intent = new Intent(activity, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                activity,
                ((Event) entryList.get(index)).getUUID().hashCode(),
                intent,
                PendingIntent.FLAG_NO_CREATE
        );
        pendingIntent.cancel();
    }

    public void notification(Context context, int index) {
        if (isNotificationSet(context, index))
            cancelNotification(context, index);
        else
            setNotification(context, index);
    }

    public boolean isNotificationSet(Context activity, int index) {
        Intent intent = new Intent(activity, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                activity,
                ((Event) entryList.get(index)).getUUID().hashCode(),
                intent,
                PendingIntent.FLAG_NO_CREATE
        );
        return pendingIntent != null;
    }

    public int getIndexByUUID(UUID uuid) {
        int l = entryList.size();
        for (int i = 0; i < l; i++)
            if (((Event) entryList.get(i)).getUUID().equals(uuid))
                return i;
        return -1;
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
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null)
                    convertView = context.getLayoutInflater().inflate(R.layout.event_item, null);
                final Event event = (Event) entryList.get(position);
                TextView label = (TextView) convertView.findViewById(R.id.event_label);
                TextView desc = (TextView) convertView.findViewById(R.id.event_desc);
                ProgressBar progressBar = (ProgressBar) convertView.findViewById(R.id.task_progress);

                CheckBox finish = (CheckBox) convertView.findViewById(R.id.check_finish);

                label.setText(event.getLabel());
                if (event.getClass() == Practice.class) {
                    final Practice practice = (Practice) event;
                    markManager.update(practice);
                    progressBar.setVisibility(View.GONE);
                    desc.setVisibility(View.VISIBLE);
                    String string = practice.getDesc(context);
                    desc.setText(string);
                    finish.setChecked(markManager.isMarked(practice));
                    finish.setText(context.getResources().getString(R.string.mark_practice));
                    finish.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked)
                                markManager.addEntry(practice, System.currentTimeMillis());
                            else
                                markManager.removeEntry(practice, (practice).getNext(System.currentTimeMillis()));
                        }
                    });
                } else {
                    finish.setText(context.getResources().getString(R.string.finished));
                    finish.setChecked(event.isFinished());
                    finish.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            event.setFinished(isChecked);
                        }
                    });
                    if (event.getClass() == Event.class) {
                        desc.setVisibility(View.GONE);
                        progressBar.setVisibility(View.GONE);
                    } else if (event.getClass() == Task.class) {
                        desc.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.VISIBLE);
                        Task task = (Task) event;
                        progressBar.setMax(task.getDuration());
                        progressBar.setProgress(task.getProgress());
                        desc.setText(task.getDesc(context));
                    }
                }

                return convertView;
            }
        };
    }

    public ArrayList<String> getLabels(Context context) {
        ArrayList<String> list = new ArrayList<>();
        for (Entry event : entryList) {
            StringBuilder builder = new StringBuilder();
            builder.append(((Event) event).getLabel());
            if (event instanceof Task)
                builder.append(context.getResources().getString(R.string.separator))
                        .append(context.getResources().getString(R.string.expire_date))
                        .append(((Task) event).getExpire().getLocale(context));
            list.add(builder.toString());
        }
        return list;
    }

    public ArrayList<String> getUUIDs() {
        ArrayList<String> list = new ArrayList<>();
        for (Entry event : entryList) list.add(((Event) event).getUUID().toString());
        return list;
    }

    @Override
    public Bundle edit(int index) throws JSONException {
        Bundle bundle = super.edit(index);
        if (index != -1 && entryList.get(index) instanceof Practice)
            bundle.putString(PRACTICE_MARK, markManager.toJson((Practice) entryList.get(index)).toString());
        return bundle;
    }

    @Override
    public void deleteEntry(Context activity, int index) {
        if (entryList.get(index) instanceof Practice)
            markManager.removePractice((Practice) entryList.get(index));
        super.deleteEntry(activity, index);
    }

    @Override
    protected Entry fromJson(JSONObject object) throws JSONException {
        return Event.fromJson(object);
    }

    @Override
    protected String fileName() {
        return FILE_NAME;
    }

    @Override
    protected void editEntry(Context activity, int index, Bundle bundle) {
        if (entryList.get(index) instanceof Practice) {
            if (bundle.getString(PRACTICE_MARK) != null)
                try {
                    markManager.fromJson((Practice) entryList.get(index), new JSONObject(bundle.getString(PRACTICE_MARK)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            else
                markManager.update((Practice) entryList.get(index));
        }
    }
}
