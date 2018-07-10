package org.zx_xjr.timemanagement;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import org.json.JSONException;
import org.zx_xjr.timemanagement.entrymanager.EventManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import static org.zx_xjr.timemanagement.entrymanager.ReminderManager.INTENT_EVENTS;

public class ReminderActivity extends AppCompatActivity {
    private EventManager eventManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);
        Log.i("mm", "onCreate: contentview set");

        Intent intent = getIntent();
        ArrayList<String> events = intent.getStringArrayListExtra(INTENT_EVENTS);

        Log.i("mm", "oncreate: " + intent.getStringArrayListExtra(INTENT_EVENTS));

        eventManager = new EventManager();
        try {
            eventManager.loadEntry(this);
            eventManager.setUpAdapter(this);
            eventManager.update((ListView) findViewById(R.id.list_reminder_event));
            eventManager.setUIUpdate(this, (ListView) findViewById(R.id.list_reminder_event));
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        eventManager.filter(events, System.currentTimeMillis());
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (eventManager != null)
            try {
                eventManager.saveEntry(this);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_reminder, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_finish) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
