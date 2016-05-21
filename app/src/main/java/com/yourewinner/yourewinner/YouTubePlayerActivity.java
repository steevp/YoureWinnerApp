package com.yourewinner.yourewinner;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;

public class YouTubePlayerActivity extends AppCompatActivity implements YouTubePlayer.OnInitializedListener {
    public final static String ARG_VIDEO_ID = "ARG_VIDEO_ID";
    private String mVideoID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube_player);

        Intent intent = getIntent();
        mVideoID = intent.getStringExtra(ARG_VIDEO_ID);
        setupPlayer();
    }

    private void setupPlayer() {
        YouTubePlayerSupportFragment fragment = new YouTubePlayerSupportFragment();
        fragment.initialize(Secrets.YOUTUBE_API_KEY, this);
        getSupportFragmentManager().beginTransaction().replace(R.id.youtube_container, fragment).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
        if (!b) {
            youTubePlayer.loadVideo(mVideoID);
        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
        Toast.makeText(this, "Unable to load video!", Toast.LENGTH_LONG).show();
    }

    public void closeWindow(View v) {
        finish();
    }
}
