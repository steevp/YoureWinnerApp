package com.yourewinner.yourewinner;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import de.timroes.axmlrpc.XMLRPCCallback;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLRPCServerException;

public class ComposePrivateMessageActivity extends AppCompatActivity {

    public final static String ARG_MSGTO = "ARG_MSGTO";
    public final static String ARG_SUBJECT = "ARG_SUBJECT";

    private Forum mForum;
    private EditText mMessageTo;
    private EditText mMessageSubject;
    private EditText mMessageBody;
    private ProgressDialog mDialog;

    public static Intent createIntent(Context context, String recipients, @Nullable  String subject) {
        Intent intent = new Intent(context, ComposePrivateMessageActivity.class);
        intent.putExtra(ARG_MSGTO, recipients);
        intent.putExtra(ARG_SUBJECT, subject);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Config.loadTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_private_message);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mForum = Forum.getInstance();

        String msgTo, subject;
        if (savedInstanceState != null) {
            msgTo = savedInstanceState.getString(ARG_MSGTO);
            subject = savedInstanceState.getString(ARG_SUBJECT);
        } else {
            Intent intent = getIntent();
            msgTo = intent.getStringExtra(ARG_MSGTO);
            subject = intent.getStringExtra(ARG_SUBJECT);
        }

        mMessageTo = (EditText) findViewById(R.id.message_to);
        mMessageSubject = (EditText) findViewById(R.id.message_subject);
        mMessageBody = (EditText) findViewById(R.id.message_body);

        mDialog = new ProgressDialog(this);
        mDialog.setMessage(getString(R.string.loading));
        mDialog.setCancelable(false);

        if (msgTo != null) {
            mMessageTo.setText(msgTo);
        }

        if (subject != null) {
            mMessageSubject.setText(subject);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(ARG_MSGTO, mMessageTo.getText().toString());
        outState.putString(ARG_SUBJECT, mMessageSubject.getText().toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_compose_pm, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_reply:
                sendReply();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendReply() {
        final String[] split = mMessageTo.getText().toString().split(",");
        final String[] to = new String[split.length];
        for (int i=0; i<split.length; i++) {
            to[i] = split[i].trim();
        }
        final String subject = mMessageSubject.getText().toString();
        final String body = mMessageBody.getText().toString();
        if (to[0].length() > 0 && subject.length() > 0 && body.length() > 0) {
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
                            Toast.makeText(getApplicationContext(), "Error sending message!", Toast.LENGTH_LONG).show();
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
}
