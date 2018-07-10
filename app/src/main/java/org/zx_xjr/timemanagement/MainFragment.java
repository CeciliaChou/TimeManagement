package org.zx_xjr.timemanagement;

import android.app.Fragment;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TabHost;
import org.json.JSONException;
import org.zx_xjr.timemanagement.entrymanager.EventManager;
import org.zx_xjr.timemanagement.entrymanager.ReminderManager;

import java.io.IOException;
import java.util.UUID;

import static org.zx_xjr.timemanagement.entrymanager.EntryManager.JSON_STRING;
import static org.zx_xjr.timemanagement.entrymanager.EventManager.NOTIF;

public class MainFragment extends Fragment {
    private ReminderManager reminderManager;
    private EventManager eventManager;
    static final String MAIN_FRAGMENT_TAG = "main";
    private static final String TAB_EVENT = "eventList";
    private static final String TAB_REMINDER = "reminderList";
    static final String EVENT_LIST = "events", UUID_LIST = "uuids";
    private static final int DELETE_REMINDER = 0, DELETE_EVENT = 1, EVENT_NOTIF = 2;

    private TabHost tabHost;
    private int tab = 0;

    private static final String STACK_REMINDER = "reminder", STACK_EVENT = "event";

    public MainFragment() {
        setArguments(new Bundle());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        reminderManager = new ReminderManager();
        eventManager = new EventManager();

        try {
            reminderManager.setUpAdapter(getActivity());
            reminderManager.loadEntry(getActivity());
            eventManager.setUpAdapter(getActivity());
            eventManager.loadEntry(getActivity());
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        Log.i("mm", "onCreate: " + getArguments().getString(NOTIF));
        if (getArguments().getString(NOTIF) != null) {
            int i;
            UUID uuid = UUID.fromString(getArguments().getString(NOTIF));
            if ((i = eventManager.getIndexByUUID(uuid)) > 0)
                try {
                    onEventClick(i);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            else {
                NotificationManager manager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                manager.cancel(uuid.hashCode());
            }
            getArguments().remove(NOTIF);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (reminderManager != null)
            try {
                reminderManager.saveEntry(getActivity());
                eventManager.saveEntry(getActivity());
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        System.out.println("oncreateview: " + this);
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        tabHost = (TabHost) view.findViewById(R.id.tabHost);
        tabHost.setup();
        tabHost.addTab(tabHost.newTabSpec(TAB_EVENT)
                .setIndicator(getActivity().getResources().getString(R.string.tab_event)).setContent(R.id.lnrEventList));
        tabHost.addTab(tabHost.newTabSpec(TAB_REMINDER)
                .setIndicator(getActivity().getResources().getString(R.string.tab_reminder)).setContent(R.id.lnrReminderList));
        tabHost.setCurrentTab(tab);
        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                tab = tabHost.getCurrentTab();
            }
        });

        ListView reminderList = (ListView) view.findViewById(R.id.list_reminder);
        reminderList.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    onReminderClick(position);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        reminderList.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                menu.add(0, DELETE_REMINDER, 0, R.string.delete);
            }
        });

        FloatingActionButton reminderAdd = (FloatingActionButton) view.findViewById(R.id.add_reminder);
        reminderAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    onReminderClick(-1);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        ListView eventList = (ListView) view.findViewById(R.id.list_event);
        eventList.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    onEventClick(position);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        eventList.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                AdapterView.AdapterContextMenuInfo info;
                try {
                    info = (AdapterView.AdapterContextMenuInfo) menuInfo;
                } catch (ClassCastException e) {
                    // If the menu object can't be cast, logs an error.
                    Log.e("MENU", "bad menuInfo", e);
                    return;
                }
                menu.add(0, DELETE_EVENT, 0, R.string.delete);
                menu.add(0, EVENT_NOTIF, 0,
                        eventManager.isNotificationSet(getActivity(), info.position) ? R.string.remove_from_notification : R.string.add_to_notification);
            }
        });

        FloatingActionButton eventAdd = (FloatingActionButton) view.findViewById(R.id.add_event);
        eventAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    onEventClick(-1);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        try {
            Log.i("mm", "onCreateView: " + getArguments());
            reminderManager.saveEdition(getActivity(), getArguments());
            reminderManager.update(reminderList);
            eventManager.saveEdition(getActivity(), getArguments());
            eventManager.update(eventList);
            eventManager.setUIUpdate(getActivity(), eventList);
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }

        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        Log.i("mm", "onCreateView: ");
        return view;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        switch (item.getItemId()) {
            case DELETE_REMINDER:
                reminderManager.deleteEntry(getActivity(), menuInfo.position);
                try {
                    reminderManager.update((ListView) getView().findViewById(R.id.list_reminder));
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
                break;
            case DELETE_EVENT:
                eventManager.deleteEntry(getActivity(), menuInfo.position);
                try {
                    eventManager.update((ListView) getView().findViewById(R.id.list_event));
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
                break;
            case EVENT_NOTIF:
                eventManager.notification(getActivity(), menuInfo.position);
        }
        return super.onContextItemSelected(item);
    }

    private void onReminderClick(int index) throws JSONException {
        if (reminderManager != null) {
            Bundle bundle = reminderManager.edit(index);
            bundle.putStringArrayList(EVENT_LIST, eventManager.getLabels(getActivity()));
            bundle.putStringArrayList(UUID_LIST, eventManager.getUUIDs());
            getArguments().remove(JSON_STRING);

            ReminderFragment fragment = new ReminderFragment();
            fragment.setArguments(bundle);
            getActivity().getFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, fragment, STACK_REMINDER)
                    .addToBackStack(STACK_REMINDER)
                    .commit();
        }
    }

    private void onEventClick(int index) throws JSONException {
        if (eventManager != null) {
            Bundle bundle = eventManager.edit(index);
            getArguments().remove(JSON_STRING);

            EventFragment fragment = new EventFragment();
            fragment.setArguments(bundle);
            getActivity().getFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, fragment, STACK_EVENT)
                    .addToBackStack(STACK_EVENT)
                    .commit();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().finish();
    }
}
