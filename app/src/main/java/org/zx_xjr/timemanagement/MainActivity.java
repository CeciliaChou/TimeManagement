package org.zx_xjr.timemanagement;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import static org.zx_xjr.timemanagement.MainFragment.MAIN_FRAGMENT_TAG;
import static org.zx_xjr.timemanagement.entrymanager.EventManager.NOTIF;

public class MainActivity extends AppCompatActivity {
    private final MainFragment fragment = new MainFragment();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        System.out.println("on create");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        Log.i("mm", "onCreate: " + intent.getStringExtra(NOTIF));
        if (intent.hasExtra(NOTIF))
            fragment.getArguments().putString(NOTIF, intent.getStringExtra(NOTIF));

        FragmentManager manager = getFragmentManager();
        Fragment fragment = manager.findFragmentById(R.id.fragmentContainer);
        if (fragment == null) {
            manager.beginTransaction()
                    .add(R.id.fragmentContainer, this.fragment, MAIN_FRAGMENT_TAG)
                    .addToBackStack(MAIN_FRAGMENT_TAG)
                    .commit();
        }

        Log.i("mm", "onCreate: main activity");
    }
}
