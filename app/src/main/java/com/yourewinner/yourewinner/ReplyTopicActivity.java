package com.yourewinner.yourewinner;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import de.timroes.axmlrpc.XMLRPCCallback;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLRPCServerException;

public class ReplyTopicActivity extends AppCompatActivity {

    Forum mForum;
    EditText mPostContent;

    private String topicTitle;
    private String topicID;
    private String boardID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply_topic);

        mForum = Forum.getInstance();

        Intent intent = getIntent();
        topicTitle = intent.getStringExtra("topicTitle");
        topicID = intent.getStringExtra("topicID");
        boardID = intent.getStringExtra("boardID");

        getSupportActionBar().setTitle(topicTitle);

        mPostContent = (EditText) findViewById(R.id.post_content);
    }

    public void sendReply() {
        String message = mPostContent.getText().toString();

        mForum.replyPost(boardID, topicID, topicTitle, message, new XMLRPCCallback() {
            @Override
            public void onResponse(long id, Object result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Success!", Toast.LENGTH_LONG).show();
                        finish();
                    }
                });
            }

            @Override
            public void onError(long id, XMLRPCException error) {

            }

            @Override
            public void onServerError(long id, XMLRPCServerException error) {

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
        Intent intent;
        if (id == R.id.action_settings) {
            intent = new Intent(this, MyPreferenceActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_reply) {
            sendReply();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
