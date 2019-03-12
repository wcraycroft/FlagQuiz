package edu.miracostacollege.cs134.flagquiz;

import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Display the fragment as the main content
        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new SettingsActivityFragment()).commit();
    }

    public static class SettingsActivityFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);
        }
    }
}
