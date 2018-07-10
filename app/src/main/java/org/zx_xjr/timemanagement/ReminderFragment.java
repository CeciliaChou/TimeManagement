package org.zx_xjr.timemanagement;


import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.zx_xjr.timemanagement.entrymanager.EntryManager;
import org.zx_xjr.timemanagement.entrymanager.ReminderManager;
import org.zx_xjr.timemanagement.event.Date;
import org.zx_xjr.timemanagement.event.Entry;
import org.zx_xjr.timemanagement.event.Reminder;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import static org.zx_xjr.timemanagement.MainFragment.EVENT_LIST;
import static org.zx_xjr.timemanagement.MainFragment.MAIN_FRAGMENT_TAG;
import static org.zx_xjr.timemanagement.MainFragment.UUID_LIST;
import static org.zx_xjr.timemanagement.entrymanager.EntryManager.ADD_STRING;
import static org.zx_xjr.timemanagement.entrymanager.EntryManager.DELETE_STRING;
import static org.zx_xjr.timemanagement.entrymanager.EntryManager.JSON_STRING;
import static org.zx_xjr.timemanagement.event.Reminder.INTERVAL;
import static org.zx_xjr.timemanagement.event.Reminder.LABEL;


public class ReminderFragment extends EntryFragment {
    private EditTextPreference prefIntervalValue, prefLabel;
    private ListPreference prefIntervalUnit;
    private MultiSelectListPreference prefEventList;
    private TimePickerPreference prefTimePicker;
    private DatePickerPreference prefStartDate;

    private Reminder oldReminder;
    private ArrayList<String> uuids;

    public ReminderFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        addPreferencesFromResource(R.xml.preference_reminder);

        prefIntervalValue = (EditTextPreference) findPreference(getActivity().getResources().getString(R.string.entry_interval_value));
        prefLabel = (EditTextPreference) findPreference(getActivity().getResources().getString(R.string.entry_label));
        prefIntervalUnit = (ListPreference) findPreference(getActivity().getResources().getString(R.string.entry_interval_unit));
        prefEventList = (MultiSelectListPreference) findPreference(getActivity().getResources().getString(R.string.bind_events));
        prefTimePicker = (TimePickerPreference) findPreference(getActivity().getResources().getString(R.string.reminder_time));
        prefStartDate = (DatePickerPreference) findPreference(getActivity().getResources().getString(R.string.first_reminder_date));

        ArrayList<String> events = getArguments().getStringArrayList(EVENT_LIST);
        uuids = getArguments().getStringArrayList(UUID_LIST);
        if (events != null && uuids != null) {
            prefEventList.setEntries(events.toArray(new CharSequence[events.size()]));
            prefEventList.setEntryValues(uuids.toArray(new CharSequence[uuids.size()]));
        }

        Bundle bundle = getArguments();
        System.out.println(bundle.getBoolean(ADD_STRING));
        if (!bundle.getBoolean(ADD_STRING)) {
            try {
                oldReminder = new Reminder(new JSONObject(bundle.getString(JSON_STRING)));

                prefTimePicker.setTime(oldReminder.getTime().getValue());
                prefLabel.setText(oldReminder.getLabel());
                prefIntervalValue.setText(String.valueOf(oldReminder.getIntervalValue()));
                prefIntervalUnit.setValueIndex(oldReminder.getIntervalUnit());
                prefStartDate.setDate(oldReminder.getIntervalStart());
                prefEventList.setValues(oldReminder.getEvents());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            add = true;
            edited = true;

            prefLabel.setText(getActivity().getResources().getString(R.string.default_reminder_label));
            prefIntervalUnit.setValueIndex(DEFAULT_UNIT);
            prefIntervalValue.setText(String.valueOf(DEFAULT_INTERVAL));
            prefStartDate.setDate(new Date(System.currentTimeMillis()));
        }

        updateSummary(prefLabel);
        updateSummary(prefIntervalUnit);
        updateSummary(prefIntervalValue);
    }

    @Override
    protected void setBundle(Bundle bundle) throws JSONException {
        String label = prefLabel.getText().equals("") ? getActivity().getResources().getString(R.string.default_reminder_label) : prefLabel.getText();

        if (oldReminder == null)
            oldReminder = new Reminder(label, prefTimePicker.getLastHour(), prefTimePicker.getLastMinute(),
                    Integer.parseInt(prefIntervalValue.getText()), prefIntervalUnit.findIndexOfValue(prefIntervalUnit.getValue()),
                    prefStartDate.getDate());
        else {
            oldReminder.setHourMinute(prefTimePicker.getLastHour(), prefTimePicker.getLastMinute());
            oldReminder.setInterval(Integer.parseInt(prefIntervalValue.getText()), prefIntervalUnit.findIndexOfValue(prefIntervalUnit.getValue()),
                    prefStartDate.getDate());
            oldReminder.setLabel(label);
        }

        int l = uuids.size();
        Set<String> set = prefEventList.getValues();
        for (String uuid: uuids) {
            if (set.contains(uuid))
                oldReminder.addEvent(UUID.fromString(uuid));
            else
                oldReminder.removeEvent(UUID.fromString(uuid));
        }

        bundle.putString(JSON_STRING, oldReminder.toJson().toString());
    }
}
