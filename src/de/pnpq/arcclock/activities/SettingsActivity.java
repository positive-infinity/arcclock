package de.pnpq.arcclock.activities;

import android.app.Activity;
import android.os.Bundle;
import de.pnpq.arcclock.fragments.SettingsFragment;

public class SettingsActivity extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // display the fragment as the main content
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }
}
