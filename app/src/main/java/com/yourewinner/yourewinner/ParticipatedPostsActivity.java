package com.yourewinner.yourewinner;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

public class ParticipatedPostsActivity extends AppCompatActivity {

    public final static String ARG_USERNAME = "ARG_USERNAME";

    private String mUsername;
    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        final String theme = mSharedPreferences.getString("theme", "0");

        switch (theme) {
            case "0":
                setTheme(R.style.AppTheme);
                break;
            case "1":
                setTheme(R.style.GayPrideTheme);
                break;
            case "2":
                setTheme(R.style.StonerTheme);
                break;
            case "3":
                setTheme(R.style.DarkTheme);
                break;
            case "4":
                setTheme(R.style.LightTheme);
                break;
            default:
                setTheme(R.style.AppTheme);
                break;
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_participated_posts);

        mUsername = getIntent().getStringExtra(ARG_USERNAME);
        getSupportActionBar().setTitle(mUsername);

        createFragment();
    }

    private void createFragment() {
        Fragment fragment = new ParticipatedPostsFragment();
        Bundle args = new Bundle();
        args.putString(ParticipatedPostsFragment.ARG_USERNAME, mUsername);
        fragment.setArguments(args);
        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, fragment).commit();
    }
}
