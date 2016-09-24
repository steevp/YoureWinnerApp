package com.yourewinner.yourewinner;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Config {
    private final static String PREF_THEME = "theme";
    private final static String PREF_SMFCOOKIE = "SMFCookie557";
    private final static String PREF_PHPSESSID = "PHPSESSID";

    private final static int[] THEMES = {
            R.style.AppTheme,
            R.style.GayPrideTheme,
            R.style.StonerTheme,
            R.style.DarkTheme,
            R.style.LightTheme
    };

    /**
     * This method is called in each activity's onCreate().
     *
     * Restores the theme and session cookies from SharedPreferences
     */
    public static void loadTheme(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String theme = prefs.getString(PREF_THEME, "0");
        int themeId = Integer.parseInt(theme);
        context.setTheme(THEMES[themeId]);

        /*Forum forum = Forum.getInstance();
        if (!forum.getLogin()) {
            String smfCookie = prefs.getString(PREF_SMFCOOKIE, "");
            String phpSessId = prefs.getString(PREF_PHPSESSID, "");
            if (smfCookie.length() > 0 && phpSessId.length() > 0) {
                Map<String,String> cookies = new HashMap<>();
                cookies.put(PREF_SMFCOOKIE, smfCookie);
                cookies.put(PREF_PHPSESSID, phpSessId);
                forum.setCookies(cookies);
                forum.setLogin(true);
            }
        }*/
    }
}
