package com.yourewinner.yourewinner;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import de.timroes.axmlrpc.XMLRPCCallback;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLRPCServerException;

public class ReplyTopicActivity extends AppCompatActivity {

    public final static String ARG_TOPIC_TITLE = "ARG_TOPIC_TITLE";
    public final static String ARG_TOPIC_ID = "ARG_TOPIC_ID";
    public final static String ARG_BOARD_ID = "ARG_BOARD_ID";

    private Forum mForum;
    private EditText mPostContent;
    private ProgressDialog mDialog;
    private SharedPreferences mSharedPreferences;

    private String topicTitle;
    private String topicID;
    private String boardID;
    private String postID;
    private boolean quote;
    private boolean mSaveDraft;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Config.loadTheme(this);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply_topic);

        mForum = Forum.getInstance();

        mDialog = new ProgressDialog(this);
        mDialog.setMessage(getString(R.string.loading));
        mDialog.setCancelable(false);

        Intent intent = getIntent();
        topicTitle = intent.getStringExtra(ARG_TOPIC_TITLE);
        topicID = intent.getStringExtra(ARG_TOPIC_ID);
        boardID = intent.getStringExtra(ARG_BOARD_ID);
        postID = intent.getStringExtra("postID");
        quote = intent.getBooleanExtra("quote", false);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        getSupportActionBar().setTitle(topicTitle);

        mPostContent = (EditText) findViewById(R.id.post_content);

        if (quote && postID.length() > 0) {
            getQuote();
        } else {
            restoreSavedDraft();
        }

        mSaveDraft = true;
    }

    public void getQuote() {
        mDialog.show();
        mForum.getQuotePost(postID, new XMLRPCCallback() {
            @Override
            public void onResponse(long id, Object result) {
                Map<String, Object> r = (Map<String, Object>) result;
                try {
                    final String postContent = new String((byte[]) r.get("post_content"), "UTF-8");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mDialog.dismiss();
                            mPostContent.append(postContent);
                        }
                    });
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(long id, XMLRPCException error) {
                mDialog.dismiss();
            }

            @Override
            public void onServerError(long id, XMLRPCServerException error) {
                mDialog.dismiss();
            }
        });
    }

    public void sendReply() {
        mDialog.show();
        String message = mPostContent.getText().toString();

        mForum.replyPost(boardID, topicID, topicTitle, message, new XMLRPCCallback() {
            @Override
            public void onResponse(long id, Object result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Success!", Toast.LENGTH_LONG).show();
                        setResult(TopicViewActivity2.RESULT_RELOAD);
                        // No need to save draft if successful
                        mSaveDraft = false;
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
                        Toast.makeText(getApplicationContext(), "Unable to send reply!", Toast.LENGTH_LONG).show();
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
                        Toast.makeText(getApplicationContext(), "FUCK: yourewinner.com might be down!", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
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
            sendReply();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        if (mSaveDraft) {
            saveDraft();
        }
        super.onPause();
    }

    private void saveDraft() {
        String reply = mPostContent.getText().toString();
        if (reply.length() > 0) {
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString("reply_draft", reply);
            editor.commit();
            Toast.makeText(this, "Draft saved!", Toast.LENGTH_SHORT).show();
        }
    }

    private void restoreSavedDraft() {
        final String draft = mSharedPreferences.getString("reply_draft", "");
        if (draft.length() > 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Restore saved draft");
            builder.setMessage("Do you want to restore your saved draft?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mPostContent.append(draft);
                    SharedPreferences.Editor editor = mSharedPreferences.edit();
                    editor.remove("reply_draft");
                    editor.commit();
                }});
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    SharedPreferences.Editor editor = mSharedPreferences.edit();
                    editor.remove("reply_draft");
                    editor.commit();
                }
            });
            builder.show();
        }
    }
}
