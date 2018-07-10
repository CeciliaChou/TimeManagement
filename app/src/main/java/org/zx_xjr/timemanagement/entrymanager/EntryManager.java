package org.zx_xjr.timemanagement.entrymanager;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.AnyRes;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.util.Log;
import android.widget.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.zx_xjr.timemanagement.MainFragment;
import org.zx_xjr.timemanagement.event.Entry;
import org.zx_xjr.timemanagement.event.Task;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/12/10.
 */
public abstract class EntryManager {
    public final static String JSON_STRING = "json", ADD_STRING = "add", DELETE_STRING = "delete", TYPE_STRING = "type";
    private boolean beingEdited = false;
    private int inEdition = -1;
    final List<Entry> entryList = new ArrayList<>();
    private BaseAdapter adapter;

    protected abstract BaseAdapter newAdapter(Activity context);

    public void update(ListView layout) throws IOException, JSONException {
        Log.i("mm", "update, adapter = " + adapter + ", me is " + this);
        if (layout.getAdapter() == null) layout.setAdapter(adapter);
        adapter.notifyDataSetInvalidated();
    }

    public void setUpAdapter(Activity activity) {
        adapter = newAdapter(activity);
        Log.i("mm", "setUpAdapter: " + adapter + ", me is " + this);
    }

    public void loadEntry(Activity activity) throws IOException, JSONException {
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = null;
        try {
            File file = new File(activity.getFilesDir() + fileName());
            if (!file.exists()) {
                if (!file.createNewFile()) throw new IOException("Create err");
            }
            InputStream in = new FileInputStream(file);//activity.openFileInput(file.getName());
            reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null)
                builder.append(line);
            if ("".equals(builder.toString())) return;
            JSONArray array = new JSONArray(builder.toString());
            int l = array.length();
            for (int i = 0; i < l; i++) {
                Entry entry = fromJson(array.getJSONObject(i));
                entryList.add(entry);
            }
        } catch (JSONException ignored) {
        } finally {
            if (reader != null)
                reader.close();
        }
    }

    public void saveEntry(Context activity) throws IOException, JSONException {
        JSONArray array = new JSONArray();
        for (Entry entry : entryList)
            array.put(entry.toJson());
        Writer writer = null;
        try {
            File file = new File(activity.getFilesDir() + fileName());
            OutputStream out = new FileOutputStream(file); //activity.openFileOutput(fileName(), Context.MODE_PRIVATE);
            writer = new BufferedWriter(new OutputStreamWriter(out));
            writer.write(array.toString());
        } finally {
            if (writer != null)
                writer.close();
        }
    }

    protected abstract Entry fromJson(JSONObject object) throws JSONException;

    protected abstract String fileName();

    public void deleteEntry(Context activity, int index) {
        entryList.remove(index);
    }

    protected abstract void editEntry(Context activity, int index, Bundle bundle);

    /**
     * Called before user begins to edit an entry. Returns a bundle
     * for {@link MainFragment} 's convenience.
     *
     * @param index the index of the reminder to be edited
     * @return a {@link Bundle} that packs the information of the reminder
     * to be edited
     */
    public Bundle edit(int index) throws JSONException {
        Bundle bundle = new Bundle();
        beingEdited = true;
        if (index == -1) {
            bundle.putBoolean(ADD_STRING, true);
        } else {
            inEdition = index;
            bundle.putString(JSON_STRING, entryList.get(index).toJson().toString());
            bundle.putBoolean(ADD_STRING, false);
        }
        Log.i("mm", "edit: " + bundle.toString());
        return bundle;
    }

    /**
     * Called when user goes back to {@link MainFragment}. An entry might have been saved.
     *
     * @param bundle bundle returned by {@link MainFragment} 's getArguments(). Might contain
     *               the entry saved.
     * @throws JSONException when a JSON cannot be successfully cast.
     */
    public void saveEdition(Context activity, Bundle bundle) throws JSONException {
        if (bundle != null && beingEdited) {
            beingEdited = false;
            Log.i("mm", "saveEdition: " + bundle.toString());
            String s;
            if (bundle.getBoolean(DELETE_STRING) && inEdition != -1) {
                deleteEntry(activity, inEdition);
                inEdition = -1;
                bundle.remove(DELETE_STRING);
            } else if ((s = bundle.getString(JSON_STRING)) != null) {
                Entry entry = fromJson(new JSONObject(s));
                if (entry instanceof Task)
                    Log.i("mm", "saveEdition: " + ((Task) entry).getStart().getLocale(activity));
                Log.i("mm", entry.toString());
                if (inEdition != -1) {
                    entryList.set(inEdition, entry);
                    editEntry(activity, inEdition, bundle);
                    inEdition = -1;
                } else {
                    entryList.add(entry);
                    editEntry(activity, entryList.size() - 1, bundle);
                }
            }
        }
    }
}
