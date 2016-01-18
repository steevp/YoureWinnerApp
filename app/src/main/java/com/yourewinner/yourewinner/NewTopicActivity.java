package com.yourewinner.yourewinner;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import java.util.Map;

import de.timroes.axmlrpc.XMLRPCCallback;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLRPCServerException;

public class NewTopicActivity extends AppCompatActivity {

    public final static String ARG_BOARDID = "ARG_BOARDID";
    private SharedPreferences mSharedPreferences;
    private String mBoardID;
    private Forum mForum;
    private EditText mSubject;
    private EditText mBody;

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
        setContentView(R.layout.activity_new_topic);

        mForum = Forum.getInstance();
        mBoardID = getIntent().getStringExtra(ARG_BOARDID);

        mSubject = (EditText) findViewById(R.id.subject);
        mBody = (EditText) findViewById(R.id.body);
    }

    private void createNewTopic() {
        String subject = mSubject.getText().toString();
        String body = mBody.getText().toString();

        mForum.newTopic(mBoardID, subject, body, new XMLRPCCallback() {
            @Override
            public void onResponse(long id, Object result) {
                Map<String,Object> r = (Map<String,Object>) result;
                final String topicID = (String) r.get("topic_id");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(getApplicationContext(), TopicViewActivity.class);
                        intent.putExtra("topicID", topicID);
                        startActivity(intent);
                        finish();
                    }
                });
            }

            @Override
            public void onError(long id, XMLRPCException error) {
                error.printStackTrace();
            }

            @Override
            public void onServerError(long id, XMLRPCServerException error) {
                error.printStackTrace();
            }
        });
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
        }

        return super.onOptionsItemSelected(item);
    }
}
