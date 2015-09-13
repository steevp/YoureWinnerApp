package com.yourewinner.yourewinner;

import android.app.Application;

/**
 * YOU'RE WINNER !
 */
public class WinnerApplication extends Application {
    private boolean isLoggedIn;

    @Override
    public void onCreate() {
        isLoggedIn = false;
        super.onCreate();
    }

    public void setLogin(boolean login) {
        isLoggedIn = login;
    }

    public boolean getLogin() {
        return isLoggedIn;
    }

}
