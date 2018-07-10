package org.zx_xjr.timemanagement;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import static org.zx_xjr.timemanagement.entrymanager.ReminderManager.INTENT_EVENTS;

public class ReminderReceiver extends BroadcastReceiver {
    public ReminderReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("mm", "onReceive: " + intent.getStringArrayListExtra(INTENT_EVENTS));
        intent.setClass(context, ReminderActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
