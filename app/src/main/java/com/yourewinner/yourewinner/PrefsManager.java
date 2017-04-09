package com.yourewinner.yourewinner;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.HashMap;
import java.util.Map;

public class PrefsManager {
    private final static String PREF_THEME = "theme";
    private final static String PREF_USERNAME = "username";
    private final static String PREF_PASSWORD = "password";
    private final static String PREF_AVATAR = "avatar";
    private final static String PREF_SMFCOOKIE = "SMFCookie557";
    private final static String PREF_PHPSESSID = "PHPSESSID";
    private final static String PREF_MODERATOR = "PREF_MODERATOR";
    private final static String PREF_BANNER = "rotating_banner";

    private SharedPreferences mPrefs;

    public PrefsManager(Context context) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public String getTheme() {
        return mPrefs.getString(PREF_THEME, "0");
    }

    public void setTheme(String themeId) {
        writePref(PREF_THEME, themeId);
    }

    public String getUsername() {
        return mPrefs.getString(PREF_USERNAME, "");
    }

    public void setUsername(String username) {
        writePref(PREF_USERNAME, username);
    }

    public String getPassword() {
        return mPrefs.getString(PREF_PASSWORD, "");
    }

    public void setPassword(String password) {
        writePref(PREF_PASSWORD, password);
    }

    public String getAvatar() {
        return mPrefs.getString(PREF_AVATAR, "");
    }

    public void setAvatar(String avatar) {
        writePref(PREF_AVATAR, avatar);
    }

    public void setModerator(Boolean canModerate) {
        writePref(PREF_MODERATOR, canModerate);
    }

    public Boolean getModerator() {
        return mPrefs.getBoolean(PREF_MODERATOR, false);
    }

    public void setBanner(Boolean banner) {
        writePref(PREF_BANNER, banner);
    }
    public Boolean getBanner() {
        return mPrefs.getBoolean(PREF_BANNER, false);
    }

    public Map<String,String> getCookies() {
        Map<String,String> map = new HashMap<>();
        map.put(PREF_SMFCOOKIE, getSmfCookie());
        map.put(PREF_PHPSESSID, getPhpSessId());
        return map;
    }

    public String getSmfCookie() {
        return mPrefs.getString(PREF_SMFCOOKIE, "");
    }

    public String getPhpSessId() {
        return mPrefs.getString(PREF_PHPSESSID, "");
    }

    public void setCookies(Map<String,String> cookies) {
        writePref(PREF_SMFCOOKIE, cookies.get(PREF_SMFCOOKIE));
        writePref(PREF_PHPSESSID, cookies.get(PREF_PHPSESSID));
    }

    public void clearAllCookies() {
        writePref(PREF_USERNAME, "");
        writePref(PREF_PASSWORD, "");
        writePref(PREF_AVATAR, "");
        writePref(PREF_SMFCOOKIE, "");
        writePref(PREF_PHPSESSID, "");
        writePref(PREF_MODERATOR, false);
    }

    private void writePref(String key, String value) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(key, value);
        editor.commit();
    }

    private void writePref(String key, Boolean value) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }
}
