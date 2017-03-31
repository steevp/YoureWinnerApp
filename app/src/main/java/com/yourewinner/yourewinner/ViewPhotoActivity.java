package com.yourewinner.yourewinner;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.github.chrisbanes.photoview.PhotoView;

public class ViewPhotoActivity extends AppCompatActivity {

    private PhotoView mPhoto;
    private ProgressBar mProgress;
    private String mPhotoUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Config.loadTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_photo);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mPhotoUrl = getIntent().getStringExtra("imageURL");

        mPhoto = (PhotoView) findViewById(R.id.photo);
        mProgress = (ProgressBar) findViewById(R.id.progress);
        Glide.with(this)
                .load(mPhotoUrl)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        mProgress.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        mProgress.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(mPhoto);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_view_photo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share:
                shareLink();
                return true;
            case R.id.action_copy:
                copyToClipboard();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void shareLink() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, mPhotoUrl);
        intent.setType("image/*");
        startActivity(Intent.createChooser(intent, getResources().getText(R.string.action_share)));
    }

    private void copyToClipboard() {
        ClipboardManager clip = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData data = ClipData.newPlainText("image", mPhotoUrl);
        clip.setPrimaryClip(data);
        Toast.makeText(this, "Copied to clipboard!", Toast.LENGTH_LONG).show();
    }
}
