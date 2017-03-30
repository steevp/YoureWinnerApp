package com.yourewinner.yourewinner;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.kefirsf.bb.BBProcessorFactory;
import org.kefirsf.bb.TextProcessor;

import java.nio.charset.Charset;
import java.util.Date;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import de.timroes.axmlrpc.XMLRPCCallback;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLRPCServerException;

public class PrivateMessageActivity extends AppCompatActivity {

    public final static String ARG_MSGID = "ARG_MSGID";
    public final static String ARG_BOXID = "ARG_BOXID";

    private Forum mForum;

    private String mMsgID;
    private String mBoxID;

    private CircleImageView mAvatar;
    private TextView mUsername;
    private TextView mBody;
    private TextView mDate;
    private View mContent;
    private View mLoading;
    private EditText mQuickReply;
    private ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Config.loadTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_message);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mForum = Forum.getInstance();

        Intent intent = getIntent();
        mMsgID = intent.getStringExtra(ARG_MSGID);
        mBoxID = intent.getStringExtra(ARG_BOXID);

        mAvatar = (CircleImageView) findViewById(R.id.avatar);
        mUsername = (TextView) findViewById(R.id.username);
        mBody = (TextView) findViewById(R.id.message_body);
        mDate = (TextView) findViewById(R.id.timestamp);

        mLoading = findViewById(R.id.loading_content);
        mContent = findViewById(R.id.content);

        mQuickReply = (EditText) findViewById(R.id.quick_reply);

        mDialog = new ProgressDialog(this);
        mDialog.setCancelable(false);
        mDialog.setMessage(getString(R.string.loading));

        getMessage();
    }

    private void getMessage() {
        mForum.getMessage(mMsgID, mBoxID, new XMLRPCCallback() {
            @Override
            public void onResponse(long id, Object result) {
                Map<String,Object> r = (Map<String,Object>) result;
                final String avatar = (String) r.get("icon_url");
                final String username = new String((byte[]) r.get("msg_from"), Charset.forName("UTF-8"));
                final String subject = new String((byte[]) r.get("msg_subject"), Charset.forName("UTF-8"));
                final String msg = new String((byte[]) r.get("text_body"), Charset.forName("UTF-8"));
                Date timestamp = (Date) r.get("sent_date");
                long now = System.currentTimeMillis();
                final String date = DateUtils.getRelativeTimeSpanString(timestamp.getTime(), now, DateUtils.MINUTE_IN_MILLIS).toString();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (avatar.length() > 0) {
                            Picasso.with(getApplicationContext()).load(avatar).placeholder(R.drawable.no_avatar).fit().into(mAvatar);
                        } else {
                            mAvatar.setImageResource(R.drawable.no_avatar);
                        }

                        mUsername.setText(username);

                        getSupportActionBar().setTitle(subject);

                        TextProcessor processor = BBProcessorFactory.getInstance().create();
                        mBody.setText(Html.fromHtml(processor.process(msg), new PicassoImageGetter(mBody, getResources(), Picasso.with(getApplicationContext())), null));
                        mBody.setMovementMethod(LinkMovementMethod.getInstance());

                        mDate.setText(date);

                        mLoading.setVisibility(View.GONE);
                        mContent.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void onError(long id, XMLRPCException error) {
                error.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Unable to load message!", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onServerError(long id, XMLRPCServerException error) {
                error.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "FUCK: yourewinner.com might be down!", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    public void sendReply(View v) {
        String[] to = {mUsername.getText().toString()};
        String subject = "Re: " + getSupportActionBar().getTitle().toString();
        String body = mQuickReply.getText().toString();
        if (subject.length() > 0 && body.length() > 0) {
            mDialog.show();
            mForum.createMessage(to, subject, body, new XMLRPCCallback() {
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_view_pm, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_reply:
                Intent intent = new Intent(this, ComposePrivateMessageActivity.class);
                intent.putExtra(ComposePrivateMessageActivity.ARG_MSGTO, mUsername.getText().toString());
                intent.putExtra(ComposePrivateMessageActivity.ARG_SUBJECT, "Re: " + getSupportActionBar().getTitle());
                startActivityForResult(intent, 0);
                return true;
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == RESULT_OK) {
            setResult(RESULT_OK);
            finish();
        }
    }
}
