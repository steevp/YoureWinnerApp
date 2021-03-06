package com.yourewinner.yourewinner;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Toast;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import de.timroes.axmlrpc.XMLRPCCallback;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLRPCServerException;

public class EditPostActivity extends AppCompatActivity implements EmoteGridFragment.OnEmotePickedListener {

    private Forum mForum;
    private ProgressDialog mDialog;
    private String postID;
    private String postTitle;
    private String postContent;
    private MultiAutoCompleteTextView mPostContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Config.loadTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply_topic);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mForum = Forum.getInstance();
        mPostContent = (MultiAutoCompleteTextView) findViewById(R.id.post_content);
        mPostContent.setRawInputType(InputType.TYPE_CLASS_TEXT
                |InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                |InputType.TYPE_TEXT_FLAG_AUTO_CORRECT
                |InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        List<String> mentions = Mentions.getInstance().getMentions();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, mentions);
        mPostContent.setAdapter(adapter);
        mPostContent.setTokenizer(new SpaceTokenizer());

        mDialog = new ProgressDialog(this);
        mDialog.setCancelable(false);
        mDialog.setMessage(getString(R.string.loading));
        mDialog.show();

        Intent intent = getIntent();
        postID = intent.getStringExtra("postID");
        getEditPost();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_reply_topic, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_reply) {
            savePost();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void bbButtons(View v) {
        int page;
        switch (v.getId()) {
            case R.id.btn_emotes:
                page = 0;
                break;
            case R.id.btn_bbcode:
                page = 1;
                break;
            default:
                page = 0;
                break;
        }
        EmoteDialog dlg = EmoteDialog.newInstance(page);
        dlg.show(getSupportFragmentManager(), "dialog");
    }

    public void getEditPost() {
        mForum.getRawPost(postID, new XMLRPCCallback() {
            @Override
            public void onResponse(long id, Object result) {
                @SuppressWarnings("unchecked")
                Map<String,Object> r = (Map<String,Object>) result;

                postTitle = new String((byte[]) r.get("post_title"), Charset.forName("UTF-8"));
                postContent = new String((byte[]) r.get("post_content"), Charset.forName("UTF-8"));

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mDialog.dismiss();
                        getSupportActionBar().setTitle(postTitle);
                        mPostContent.append(postContent);
                    }
                });
            }

            @Override
            public void onError(long id, XMLRPCException error) {
                error.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mDialog.dismiss();
                        Toast.makeText(getApplicationContext(), getString(R.string.edit_error), Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onServerError(long id, XMLRPCServerException error) {
                error.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mDialog.dismiss();
                        Toast.makeText(getApplicationContext(), getString(R.string.server_error), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    public void savePost() {
        mDialog.show();
        mForum.saveRawPost(postID, postTitle, mPostContent.getText().toString(), new XMLRPCCallback() {
            @Override
            public void onResponse(long id, Object result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mDialog.dismiss();
                        setResult(TopicViewActivity.RESULT_RELOAD);
                        finish();
                    }
                });
            }

            @Override
            public void onError(long id, XMLRPCException error) {
                error.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mDialog.dismiss();
                        Toast.makeText(getApplicationContext(), getString(R.string.edit_error), Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onServerError(long id, XMLRPCServerException error) {
                error.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mDialog.dismiss();
                        Toast.makeText(getApplicationContext(), getString(R.string.server_error), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    @Override
    public void onEmotePicked(String emote, boolean isBbcode) {
        if (isBbcode) {
            insertBbcode(emote);
        } else {
            insertText(emote);
        }
    }

    private void insertBbcode(String bbcode) {
        // Get selected text so we can wrap the bbcode around it
        int selectionStart = mPostContent.getSelectionStart();
        int selectionEnd = mPostContent.getSelectionEnd();
        String selectedText = mPostContent.getText().toString().substring(selectionStart, selectionEnd);
        bbcode = bbcode + selectedText + bbcode.replace("[", "[/");
        insertText(bbcode);
    }

    private void insertText(String textToInsert) {
        // Insert text at cursor position
        int start = Math.max(mPostContent.getSelectionStart(), 0);
        int end = Math.max(mPostContent.getSelectionEnd(), 0);
        if (start > 0) {
            textToInsert = " " + textToInsert;
        }
        mPostContent.getText().replace(Math.min(start, end), Math.max(start, end),
                textToInsert, 0, textToInsert.length());
    }
}
