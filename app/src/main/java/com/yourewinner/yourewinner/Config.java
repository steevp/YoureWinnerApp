package com.yourewinner.yourewinner;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by steven on 1/23/16.
 */
public class Config {

    private final static int[] THEMES = {
            R.style.AppTheme,
            R.style.GayPrideTheme,
            R.style.StonerTheme,
            R.style.DarkTheme,
            R.style.LightTheme
    };

    public static void loadTheme(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String theme = preferences.getString("theme", "0");
        int id = Integer.parseInt(theme);
        context.setTheme(THEMES[id]);
    }
}
