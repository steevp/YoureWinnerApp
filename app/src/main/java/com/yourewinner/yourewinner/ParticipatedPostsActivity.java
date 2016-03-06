package com.yourewinner.yourewinner;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class ParticipatedPostsActivity extends AppCompatActivity {

    public final static String ARG_USERNAME = "ARG_USERNAME";

    private String mUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Config.loadTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_participated_posts);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

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
