package org.zx_xjr.timemanagement;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.LayoutRes;
import android.support.annotation.MenuRes;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.*;
import org.json.JSONException;

import static org.zx_xjr.timemanagement.MainFragment.MAIN_FRAGMENT_TAG;
import static org.zx_xjr.timemanagement.entrymanager.EntryManager.DELETE_STRING;
import static org.zx_xjr.timemanagement.entrymanager.EntryManager.JSON_STRING;


/**
 * A simple {@link Fragment} subclass.
 */
public abstract class EntryFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    protected boolean add = false, edited = false;

    protected static final int DEFAULT_INTERVAL = 1;
    protected static final int DEFAULT_UNIT = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_entry, container, false);

        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    onExit();
                    return true;
                }
                return false;
            }
        });
        return view;
    }


    private void onExit() {
        final android.app.Fragment fragment = getFragmentManager().findFragmentByTag(MAIN_FRAGMENT_TAG);
        if (edited) {
            new AlertDialog.Builder(getActivity()).setMessage(getActivity().getResources().getString(R.string.save_prompt))
                    .setPositiveButton(getActivity().getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                saveAndExit(fragment);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    })
                    .setNegativeButton(getActivity().getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            exitWithoutSaving(fragment);
                        }
                    })
                    .setNeutralButton(getActivity().getResources().getString(android.R.string.cancel), null)
                    .create().show();
        } else
            exitWithoutSaving(fragment);
    }

    private void saveAndExit(android.app.Fragment fragment) throws JSONException {
        Log.i("mm", "save: frg, " + fragment + "\nbundle, " + fragment.getArguments());
        setBundle(fragment.getArguments());
        getActivity().getFragmentManager().popBackStack();
    }

    private void exitWithoutSaving(android.app.Fragment fragment) {
        fragment.getArguments().remove(JSON_STRING);
        getActivity().getFragmentManager().popBackStack();
    }

    private void deleteAndExit() {
        new AlertDialog.Builder(getActivity()).setMessage(getActivity().getResources().getString(R.string.delete_prompt))
                .setPositiveButton(getActivity().getResources().getString(android.R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        android.app.Fragment fragment = getFragmentManager().findFragmentByTag(MAIN_FRAGMENT_TAG);
                        Bundle bundle = fragment.getArguments();
                        bundle.clear();
                        bundle.putBoolean(DELETE_STRING, true);
                        getActivity().getFragmentManager().popBackStack();
                    }
                })
                .setNegativeButton(getActivity().getResources().getString(android.R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create().show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        android.app.Fragment fragment = getFragmentManager().findFragmentByTag(MAIN_FRAGMENT_TAG);
        switch (item.getItemId()) {
            case android.R.id.home:
                onExit();
                return true;
            case R.id.save:
                try {
                    saveAndExit(fragment);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return true;
            case R.id.delete:
                deleteAndExit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_entry, menu);

        if (add) menu.findItem(R.id.delete).setVisible(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference pref = findPreference(key);
        edited = true;
        updateSummary(pref);
    }

    protected void updateSummary(Preference pref) {
        if (pref instanceof ListPreference) {
            pref.setSummary(((ListPreference) pref).getEntry());
        } else if (pref instanceof EditTextPreference) {
            pref.setSummary(((EditTextPreference) pref).getText());
        }
    }

    protected abstract void setBundle(Bundle bundle) throws JSONException;
}
