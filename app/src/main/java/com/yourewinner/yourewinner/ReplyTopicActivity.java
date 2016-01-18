package com.yourewinner.yourewinner;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
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

    private Forum mForum;
    private EditText mPostContent;
    private ProgressDialog mDialog;
    private SharedPreferences mSharedPreferences;

    private String topicTitle;
    private String topicID;
    private String boardID;
    private String postID;
    private boolean quote;



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
        setContentView(R.layout.activity_reply_topic);

        mForum = Forum.getInstance();

        mDialog = new ProgressDialog(this);
        mDialog.setMessage(getString(R.string.loading));
        mDialog.setCancelable(false);

        Intent intent = getIntent();
        topicTitle = intent.getStringExtra("topicTitle");
        topicID = intent.getStringExtra("topicID");
        boardID = intent.getStringExtra("boardID");
        postID = intent.getStringExtra("postID");
        quote = intent.getBooleanExtra("quote", false);

        if (quote && postID.length() > 0) {
            getQuote();
        }

        getSupportActionBar().setTitle(topicTitle);

        mPostContent = (EditText) findViewById(R.id.post_content);
    }

    public void getQuote() {
        mDialog.show();
        mForum.getQuotePost(postID, new XMLRPCCallback() {
            @Override
            public void onResponse(long id, Object result) {
                Map<String,Object> r = (Map<String,Object>) result;
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
            public void onError(long id, XMLRPCException error) { mDialog.dismiss(); }

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
                        setResult(RESULT_OK);
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
}
