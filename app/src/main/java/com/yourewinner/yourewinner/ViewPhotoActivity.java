package com.yourewinner.yourewinner;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.chrisbanes.photoview.PhotoView;

public class ViewPhotoActivity extends AppCompatActivity {

    private PhotoView mPhoto;
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
        Glide.with(this).load(mPhotoUrl).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(mPhoto);
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
