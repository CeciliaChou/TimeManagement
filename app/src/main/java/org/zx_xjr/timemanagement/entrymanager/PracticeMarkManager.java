package org.zx_xjr.timemanagement.entrymanager;

import android.content.Context;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import org.zx_xjr.timemanagement.event.Date;
import org.zx_xjr.timemanagement.event.Practice;

import java.io.*;
import java.util.*;

class PracticeMarkManager {
    private Map<UUID, List<MarkEntry>> map = new HashMap<>();
    private static final String FILE_NAME = "mark.json";

    void loadEntry(Context activity) throws IOException, JSONException {
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = null;
        try {
            InputStream in = activity.openFileInput(FILE_NAME);
            reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null)
                builder.append(line);
            JSONObject object = new JSONObject(builder.toString());
            Iterator<String> iterator = object.keys();
            JSONObject inner;
            UUID uuid;
            String string, strInner;
            List<MarkEntry> list;
            while (iterator.hasNext()) {
                string = iterator.next();
                uuid = UUID.fromString(string);
                inner = object.getJSONObject(string);
                list = new ArrayList<>();
                Iterator<String> iteInner = inner.keys();
                while (iteInner.hasNext()) {
                    strInner = iteInner.next();
                    list.add(new MarkEntry(new Date(Long.valueOf(strInner)), inner.getLong(strInner)));
                }
                map.put(uuid, list);
            }
        } finally {
            if (reader != null)
                reader.close();
        }
    }

    void saveEntry(Context activity) throws JSONException, IOException {
        JSONObject object = new JSONObject();
        Log.i("mm", "saveEntry: " + map);
        for (Map.Entry<UUID, List<MarkEntry>> entry : map.entrySet()) {
            JSONObject inner = new JSONObject();
            for (MarkEntry markEntry : entry.getValue()) {
                inner.put(String.valueOf(markEntry.getDue().getValue()), markEntry.getMark());
            }
            object.put(entry.getKey().toString(), inner);
        }

        Writer writer = null;
        try {
            OutputStream out = activity.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            writer = new BufferedWriter(new OutputStreamWriter(out));
            writer.write(object.toString());
        } finally {
            if (writer != null)
                writer.close();
        }
    }

    void addEntry(Practice practice, long date) {
        UUID uuid = practice.getUUID();
        List<MarkEntry> list = map.get(uuid);
        if (list == null) {
            list = new ArrayList<>();
            map.put(uuid, list);
        }
        list.add(new MarkEntry(practice.getNext(date), date));
    }

    void removeEntry(Practice practice, Date due) {
        UUID uuid = practice.getUUID();
        List<MarkEntry> list = map.get(uuid);
        if (list != null) {
            for (MarkEntry mark : list)
                if (due.equals(mark.getDue()))
                    mark.setMark(0);
            if (list.size() == 0)
                map.remove(uuid);
        }
    }

    void removePractice(Practice practice) {
        UUID uuid = practice.getUUID();
        map.remove(uuid);
    }

    JSONObject toJson(Practice practice) throws JSONException {
        UUID uuid = practice.getUUID();
        List<MarkEntry> list = map.get(uuid);
        if (list != null) {
            JSONObject inner = new JSONObject();
            for (MarkEntry markEntry : list)
                inner.put(String.valueOf(markEntry.getDue().getValue()), markEntry.getMark());
            return inner;
        }
        return null;
    }

    void fromJson(Practice practice, JSONObject object) throws JSONException {
        UUID uuid = practice.getUUID();
        List<MarkEntry> list = new ArrayList<>();
        Iterator<String> iterator = object.keys();
        while (iterator.hasNext()) {
            String s = iterator.next();
            list.add(new MarkEntry(new Date(Long.valueOf(s)), object.getLong(s)));
        }
        map.put(uuid, list);
    }

    boolean isMarked(Practice practice) {
        List<MarkEntry> list = map.get(practice.getUUID());
        if (list != null) {
            Date next = practice.getNext(System.currentTimeMillis());
            for (MarkEntry entry : list)
                if (entry.getDue().equals(next) && entry.getMark() != 0)
                    return true;
        }
        return false;
    }

    /**
     * Add to misses from the latest update till now
     *
     * @param practice The practice to update
     */
    void update(Practice practice) {
        List<MarkEntry> list = map.get(practice.getUUID());
        if (list == null) {
            list = new ArrayList<>();
            map.put(practice.getUUID(), list);
        }

        Date date;
        if (list.size() != 0) {
            date = list.get(0).getDue();
            for (MarkEntry entry : list)
                if (Date.diff(date, entry.getDue()) > 0)
                    date = entry.getDue();
        } else
            date = practice.getIntervalStart();
        List<Date> dates = practice.getDueList(date);
        for (Date another : dates)
            list.add(new MarkEntry(another, 0));
    }

    private static class MarkEntry implements Serializable {
        private Date due;
        private long mark;

        MarkEntry(Date date, long mark) {
            this.due = date;
            this.mark = mark;
        }

        Date getDue() {
            return due;
        }

        long getMark() {
            return mark;
        }

        public void setMark(long mark) {
            this.mark = mark;
        }
    }
}