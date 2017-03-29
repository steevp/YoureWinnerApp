package com.yourewinner.yourewinner;

import android.content.Context;

import java.util.Map;

public class Config {

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
        PrefsManager prefs = new PrefsManager(context);
        String theme = prefs.getTheme();
        int themeId = Integer.parseInt(theme);
        context.setTheme(THEMES[themeId]);

        Forum forum = Forum.getInstance();
        if (!forum.getLogin()) {
            String username = prefs.getUsername();
            Map<String,String> cookies = prefs.getCookies();
            boolean canModerate = prefs.getModerator();
            if (username.length() > 0 && prefs.getSmfCookie().length() > 0 && prefs.getPhpSessId().length() > 0) {
                forum.setUsername(username);
                forum.setCookies(cookies);
                forum.setLogin(true);
                forum.setModerator(canModerate);
            }
        }
    }
}
