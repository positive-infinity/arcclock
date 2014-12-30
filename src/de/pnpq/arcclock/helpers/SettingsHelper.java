package de.pnpq.arcclock.helpers;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import de.pnpq.arcclock.R;
import de.pnpq.arcclock.preferences.ColorPickerPreference;
import de.pnpq.arcclock.preferences.SliderPreference;

public class SettingsHelper
{
    public static void transferPreferences(Context context, PreferenceManager prefManager, String oldTheme, String newTheme)
    {
        String packageName = PackageHelper.getUniquePackageName(context);
        Resources resources = context.getResources();

        // build list of relevant keys
        ArrayList<String> keys = new ArrayList<String>();
        keys.add(resources.getString(R.string.border_width_key));
        keys.add(resources.getString(R.string.arc_gap_key));
        keys.add(resources.getString(R.string.arc_width_key));
        keys.add(resources.getString(R.string.twenty_four_hour_mode_key));
        keys.add(resources.getString(R.string.inverse_mode_key));
        keys.add(resources.getString(R.string.draw_outline_key));
        keys.add(resources.getString(R.string.background_color_key));
        keys.add(resources.getString(R.string.hour_arc_color_key));
        keys.add(resources.getString(R.string.minute_arc_color_key));
        keys.add(resources.getString(R.string.second_arc_color_key));

        // save current preferences and load theme preferences
        Editor editor = context.getSharedPreferences(oldTheme, Context.MODE_PRIVATE).edit();
        SharedPreferences themePrefs = context.getSharedPreferences(newTheme, Context.MODE_PRIVATE);
        for (String currentKey : keys)
        {
            String themeKey = newTheme + "_" + currentKey + "_default";
            Preference pref = prefManager.findPreference(currentKey);

            if (pref instanceof SliderPreference)
            {
                editor.putFloat(currentKey, ((SliderPreference) pref).getValue());
                String themeDefaultValueString = resources.getString(resources.getIdentifier(themeKey, "string", packageName));
                ((SliderPreference) pref).setValue(themePrefs.getFloat(currentKey, Float.parseFloat(themeDefaultValueString)));
            }
            else if (pref instanceof ListPreference)
            {
                editor.putString(currentKey, ((ListPreference) pref).getValue());
                String themeDefaultValueString = resources.getString(resources.getIdentifier(themeKey, "string", packageName));
                ((ListPreference) pref).setValue(themePrefs.getString(currentKey, themeDefaultValueString));
            }
            else if (pref instanceof CheckBoxPreference)
            {
                editor.putBoolean(currentKey, ((CheckBoxPreference) pref).isChecked());
                boolean themeDefaultValueBoolean = resources.getBoolean(resources.getIdentifier(themeKey, "bool", packageName));
                ((CheckBoxPreference) pref).setChecked(themePrefs.getBoolean(currentKey, themeDefaultValueBoolean));
            }
            else if (pref instanceof ColorPickerPreference)
            {
                editor.putInt(currentKey, ((ColorPickerPreference) pref).getValue());
                int themeDefaultValueInt = resources.getInteger(resources.getIdentifier(themeKey, "integer", packageName));
                ((ColorPickerPreference) pref).setValue(themePrefs.getInt(currentKey, themeDefaultValueInt));
            }
        }
        editor.commit();
    }

    public static void resetPreferences(Context context, PreferenceManager prefManager, String currentTheme)
    {
        Editor editor = context.getSharedPreferences(currentTheme, Context.MODE_PRIVATE).edit();
        editor.clear();
        editor.commit();

        transferPreferences(context, prefManager, "dummy", currentTheme);
    }

}
