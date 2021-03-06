package com.apps.creativesource.censio;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Inject
    SharedPreferences sharedPreferences;
    private DatabaseReference realtimeRef;
    private String userId;

    @Override
    public void onAttach(@NonNull Context context) {
        ((MyApp) getActivity().getApplication()).getAppComponent().inject(this);
        super.onAttach(context);
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        userId = sharedPreferences.getString("userFireId", "");
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        realtimeRef = FirebaseDatabase.getInstance().getReference();
        addPreferencesFromResource(R.xml.pref_settings);

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals("notifications")) {
            DatabaseReference userRef = realtimeRef.child("users")
                    .child(userId);

            Map<String, Object> notifications = new HashMap<>();
            notifications.put("notifications", sharedPreferences.getBoolean(key, true));

            userRef.updateChildren(notifications);
        }
    }

    @Override
    public void onDestroy() {
        PreferenceManager.getDefaultSharedPreferences(getContext())
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }
}
