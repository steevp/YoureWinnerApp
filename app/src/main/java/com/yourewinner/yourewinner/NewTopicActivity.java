package com.yourewinner.yourewinner;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Map;

import de.timroes.axmlrpc.XMLRPCCallback;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLRPCServerException;

public class NewTopicActivity extends AppCompatActivity
        implements EmoteGridFragment.OnEmotePickedListener, View.OnFocusChangeListener {

    public final static String ARG_BOARDID = "ARG_BOARDID";
    public final static String ARG_BOARDNAME = "ARG_BOARDNAME";
    private String mBoardID;
    private String mBoardName;
    private Forum mForum;
    private EditText mSubject;
    private EditText mPostContent;
    private EditText mFocusedEditText;
    private ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Config.loadTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_topic);

        mForum = Forum.getInstance();
        mBoardID = getIntent().getStringExtra(ARG_BOARDID);
        mBoardName = getIntent().getStringExtra(ARG_BOARDNAME);

        mDialog = new ProgressDialog(this);
        mDialog.setMessage(getString(R.string.loading));
        mDialog.setCancelable(false);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        getSupportActionBar().setTitle(mBoardName);

        mSubject = (EditText) findViewById(R.id.subject);
        mSubject.setOnFocusChangeListener(this);
        mPostContent = (EditText) findViewById(R.id.body);
        mPostContent.setOnFocusChangeListener(this);
    }

    private void createNewTopic() {
        String subject = mSubject.getText().toString();
        String body = mPostContent.getText().toString();

        if (subject.length() > 0 && body.length() > 0) {
            mDialog.show();
            mForum.newTopic(mBoardID, subject, body, new XMLRPCCallback() {
                @Override
                public void onResponse(long id, Object result) {
                    Map<String,Object> r = (Map<String,Object>) result;
                    final String topicID = (String) r.get("topic_id");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(getApplicationContext(), TopicViewActivity.class);
                            intent.putExtra(TopicViewActivity.ARG_TOPIC_ID, topicID);
                            intent.putExtra(TopicViewActivity.ARG_BOARD_ID, mBoardID);
                            startActivity(intent);
                            mDialog.dismiss();
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
                            Toast.makeText(getApplicationContext(), "Failed to create topic!", Toast.LENGTH_LONG).show();
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
                            Toast.makeText(getApplicationContext(), "FUCK: yourewinner.com might be down!", Toast.LENGTH_LONG);
                        }
                    });
                }
            });
        } else {
            Toast.makeText(this, "Please fill in subject AND body!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_reply_topic, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_reply) {
            createNewTopic();
            return true;
        } else if (id == android.R.id.home) {
            finish();
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

    @Override
    public void onEmotePicked(String emote, boolean isBbcode) {
        if (mFocusedEditText == null) {
            return;
        }
        if (isBbcode) {
            insertBbcode(emote);
        } else {
            insertText(emote);
        }
    }

    private void insertBbcode(String bbcode) {
        // Get selected text so we can wrap the bbcode around it
        int selectionStart = mFocusedEditText.getSelectionStart();
        int selectionEnd = mFocusedEditText.getSelectionEnd();
        String selectedText = mFocusedEditText.getText().toString().substring(selectionStart, selectionEnd);
        bbcode = bbcode + selectedText + bbcode.replace("[", "[/");
        insertText(bbcode);
    }

    private void insertText(String textToInsert) {
        // Insert text at cursor position
        int start = Math.max(mFocusedEditText.getSelectionStart(), 0);
        int end = Math.max(mFocusedEditText.getSelectionEnd(), 0);
        if (start > 0) {
            textToInsert = " " + textToInsert;
        }
        mFocusedEditText.getText().replace(Math.min(start, end), Math.max(start, end),
                textToInsert, 0, textToInsert.length());
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (hasFocus) {
            mFocusedEditText = (EditText) view;
        } else {
            mFocusedEditText = null;
        }
    }
}
