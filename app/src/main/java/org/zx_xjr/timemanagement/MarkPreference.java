package org.zx_xjr.timemanagement;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.preference.Preference;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.zx_xjr.timemanagement.entrymanager.EventManager;
import org.zx_xjr.timemanagement.event.Date;
import org.zx_xjr.timemanagement.event.Practice;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MarkPreference extends DialogPreference {
    private ListView listView;
    private List<Date> dues;
    private List<Long> marks;
    private Date next;
    private Integer current = -1;
    private BaseAdapter adapter;

    public MarkPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    void load(JSONObject object, Practice practice, final Activity context) {
        List<Date> argDue = new ArrayList<>();
        List<Long> argMark = new ArrayList<>();
        Iterator<String> iterator = object.keys();
        while (iterator.hasNext()) {
            String s = iterator.next();
            argDue.add(new Date(Long.valueOf(s)));
            try {
                argMark.add(object.getLong(s));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        this.dues = argDue;
        this.marks = argMark;

        setPractice(practice);

        adapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return marks.size();
            }

            @Override
            public Object getItem(int position) {
                return dues.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                if (convertView == null)
                    convertView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.mark_item, null);
                TextView text = (TextView) convertView.findViewById(R.id.mark_text);
                Button mark = (Button) convertView.findViewById(R.id.mark_mark);
                Button delete = (Button) convertView.findViewById(R.id.mark_delete);
                CheckBox ongoing = (CheckBox) convertView.findViewById(R.id.mark_ongoing);

                StringBuilder builder = new StringBuilder();
                builder.append(context.getString(R.string.practice_date))
                        .append(dues.get(position).getLocale(context));

                // Ongoing
                if (next.equals(dues.get(position))) {
                    if (delete != null) ((ViewGroup) delete.getParent()).removeView(delete);
                    if (mark != null) ((ViewGroup) mark.getParent()).removeView(mark);
                    text.setBackgroundColor(ContextCompat.getColor(context, R.color.ongoing));
                    current = position;

                    if (ongoing != null) ongoing.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked)
                                marks.set(current, System.currentTimeMillis());
                            else
                                marks.set(current, 0L);
                            adapter.notifyDataSetChanged();
                        }
                    });
                }

                // Not ongoing
                else {
                    if (ongoing != null) ((ViewGroup) ongoing.getParent()).removeView(ongoing);
                    if (delete != null) delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new AlertDialog.Builder(context).setMessage(context.getString(R.string.delete_prompt_no_reverse))
                                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dues.remove(position);
                                            marks.remove(position);
                                            if (current > position) current--;
                                        }
                                    }).setNegativeButton(R.string.no, null)
                                    .create().show();
                        }
                    });
                }

                // Already marked
                if (marks.get(position) != 0) {
                    builder.append(context.getString(R.string.separator))
                            .append(new Date(marks.get(position)).getLocale(context))
                            .append(context.getString(R.string.marked));

                    // Not ongoing (delayed or normal)
                    if (!next.equals(dues.get(position))) {
                        if (mark != null) ((ViewGroup) mark.getParent()).removeView(mark);
                        if (marks.get(position) > dues.get(position).getValue())
                            text.setBackgroundColor(ContextCompat.getColor(context, R.color.delayed));
                        else
                            text.setBackgroundColor(ContextCompat.getColor(context, R.color.normal));
                    }
                }

                // Missed
                else {
                    if (!next.equals(dues.get(position))) {
                        text.setBackgroundColor(ContextCompat.getColor(context, R.color.missed));

                        if (mark != null) mark.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                marks.set(position, System.currentTimeMillis());
                                adapter.notifyDataSetChanged();
                            }
                        });
                    }
                }
                text.setText(builder.toString());

                return convertView;
            }
        };

        EventManager.setEventUpdate(context, new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }

    void setPractice(Practice practice) {
        this.next = practice.getNext(System.currentTimeMillis());
        if (current != -1)
            dues.set(current, next);
        if (adapter != null)
            adapter.notifyDataSetChanged();
    }

    JSONObject toJson() throws JSONException {
        JSONObject object = new JSONObject();
        int l = dues.size();
        for (int i = 0; i < l; i++) object.put(String.valueOf(dues.get(i).getValue()), marks.get(i));
        return object;
    }

    @Override
    protected View onCreateDialogView() {
        final Context context = getContext();
        listView = new ListView(context);
        listView.setAdapter(adapter);
        return listView;
    }
}
