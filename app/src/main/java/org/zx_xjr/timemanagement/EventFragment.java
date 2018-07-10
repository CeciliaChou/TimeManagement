package org.zx_xjr.timemanagement;


import android.os.Bundle;
import android.preference.*;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONException;
import org.json.JSONObject;
import org.zx_xjr.timemanagement.R;
import org.zx_xjr.timemanagement.event.*;

import static org.zx_xjr.timemanagement.entrymanager.EntryManager.ADD_STRING;
import static org.zx_xjr.timemanagement.entrymanager.EntryManager.JSON_STRING;
import static org.zx_xjr.timemanagement.entrymanager.EventManager.PRACTICE_MARK;

public class EventFragment extends EntryFragment {
    private EditTextPreference prefIntervalValue, prefLabel;
    private ListPreference prefIntervalUnit, prefType;
    private DatePickerPreference prefStartDate, prefExpireDate, prefIntervalStart;
    private MarkPreference prefMark;
    private PreferenceCategory catPractice, catTask;
    private PreferenceScreen screen;
    private int type = 3;

    private static final int EVENT = 0, PRACTICE = 1, TASK = 2;

    private Event oldEvent;

    public EventFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        addPreferencesFromResource(R.xml.preference_event);
        screen = getPreferenceScreen();

        prefIntervalValue = (EditTextPreference) findPreference(getActivity().getResources().getString(R.string.entry_interval_value));
        prefLabel = (EditTextPreference) findPreference(getActivity().getResources().getString(R.string.entry_label));
        prefIntervalUnit = (ListPreference) findPreference(getActivity().getResources().getString(R.string.entry_interval_unit));
        prefType = (ListPreference) findPreference(getActivity().getResources().getString(R.string.type));
        prefStartDate = (DatePickerPreference) findPreference(getActivity().getResources().getString(R.string.start_date));
        prefExpireDate = (DatePickerPreference) findPreference(getActivity().getResources().getString(R.string.expire_date));
        prefMark = (MarkPreference) findPreference(getActivity().getResources().getString(R.string.mark_history));
        prefIntervalStart = (DatePickerPreference) findPreference(getActivity().getResources().getString(R.string.first_practice_date));
        catPractice = (PreferenceCategory) findPreference(getActivity().getResources().getString(R.string.practice));
        catTask = (PreferenceCategory) findPreference(getActivity().getResources().getString(R.string.task));

        prefType.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                changeType(prefType.findIndexOfValue(newValue.toString()));
                return true;
            }
        });

        prefStartDate.setListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (Date.diff((Date) newValue, prefExpireDate.getDate()) < 0)
                    prefStartDate.setDate(prefExpireDate.getDate());
                return false;
            }
        });

        prefExpireDate.setListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (Date.diff(prefStartDate.getDate(), (Date) newValue) < 0)
                    prefExpireDate.setDate(prefStartDate.getDate());
                return false;
            }
        });

        Bundle bundle = getArguments();
        System.out.println(bundle.getBoolean(ADD_STRING));
        if (!bundle.getBoolean(ADD_STRING)) {
            try {
                oldEvent = Event.fromJson(new JSONObject(bundle.getString(JSON_STRING)));

                if (oldEvent != null) {
                    prefLabel.setText(oldEvent.getLabel());

                    int type = TASK | PRACTICE;
                    if (oldEvent.getClass() == Event.class) type = EVENT;
                    else if (oldEvent.getClass() == Practice.class) type = PRACTICE;
                    else if (oldEvent.getClass() == Task.class) type = TASK;

                    changeType(type);
                    screen.removePreference(prefType);

                    switch (type) {
                        case PRACTICE:
                            prefIntervalValue.setText(String.valueOf(((Practice) oldEvent).getIntervalValue()));
                            prefIntervalUnit.setValueIndex(((Practice) oldEvent).getIntervalUnit());
                            prefIntervalStart.setDate(((Practice) oldEvent).getIntervalStart());
                            prefMark.load(new JSONObject(getArguments().getString(PRACTICE_MARK)),
                                    (Practice) oldEvent, getActivity());

                            prefIntervalValue.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                                @Override
                                public boolean onPreferenceChange(Preference preference, Object newValue) {
                                    updatePractice(); return true;
                                }
                            });
                            prefIntervalUnit.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                                @Override
                                public boolean onPreferenceChange(Preference preference, Object newValue) {
                                    updatePractice(); return true;
                                }
                            });
                            prefIntervalValue.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                                @Override
                                public boolean onPreferenceChange(Preference preference, Object newValue) {
                                    updatePractice(); return true;
                                }
                            });
                            break;
                        case TASK:
                            prefStartDate.setDate(((Task) oldEvent).getStart());
                            prefExpireDate.setDate(((Task) oldEvent).getExpire());
                            Log.i("mm", "onCreate: " + ((Task) oldEvent).getStart().getLocale(getActivity()));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            add = true;
            edited = true;

            prefLabel.setText(getActivity().getResources().getString(R.string.default_event_label));
            prefIntervalUnit.setValueIndex(DEFAULT_UNIT);
            prefIntervalValue.setText(String.valueOf(DEFAULT_INTERVAL));
            prefIntervalStart.setDate(new Date(System.currentTimeMillis()));

            screen.removePreference(prefMark);

            changeType(EVENT);
            prefType.setValueIndex(type);
            System.out.println(prefType.getValue());
        }

        updateSummary(prefLabel);
        updateSummary(prefType);
        updateSummary(prefIntervalUnit);
        updateSummary(prefIntervalValue);
    }

    private void changeType(int newType) {
        int change = type ^ newType;
        if ((change & PRACTICE) != 0) {
            if ((type & PRACTICE) != 0) screen.removePreference(catPractice);
            else screen.addPreference(catPractice);
        }
        if ((change & TASK) != 0) {
            if ((type & TASK) != 0) screen.removePreference(catTask);
            else screen.addPreference(catTask);
        }
        type = newType;
    }

    private void updatePractice() {
        oldEvent = new Practice(oldEvent, Integer.parseInt(prefIntervalValue.getText()), prefIntervalUnit.findIndexOfValue(prefIntervalUnit.getValue()), prefIntervalStart.getDate());
        prefMark.setPractice((Practice) oldEvent);
    }

    @Override
    protected void setBundle(Bundle bundle) throws JSONException {
        String label = prefLabel.getText().equals("") ? getActivity().getResources().getString(R.string.default_reminder_label) : prefLabel.getText();

        if (oldEvent == null) {
            switch (type) {
                case EVENT:
                    oldEvent = new Event(label);
                    break;
                case PRACTICE:
                    oldEvent = new Practice(label, Integer.parseInt(prefIntervalValue.getText()), prefIntervalUnit.findIndexOfValue(prefIntervalUnit.getValue()), prefIntervalStart.getDate());
                    break;
                case TASK:
                    oldEvent = new Task(label, prefStartDate.getDate(), prefExpireDate.getDate());
            }
        } else {
            oldEvent.setLabel(label);
            if (type == TASK)
                oldEvent = new Task(oldEvent, prefStartDate.getDate(), prefExpireDate.getDate());
        }

        bundle.putString(JSON_STRING, oldEvent.toJson().toString());
        if (oldEvent instanceof Practice && !add)
            bundle.putString(PRACTICE_MARK, prefMark.toJson().toString());
    }
}
