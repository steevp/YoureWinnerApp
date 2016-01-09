package com.yourewinner.yourewinner;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import de.timroes.axmlrpc.XMLRPCCallback;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLRPCServerException;

public class ProfileViewActivity extends AppCompatActivity {

    private SharedPreferences mSharedPreferences;
    private Forum mForum;
    private String mUsername;
    private ImageView mProfileAvatar;
    private TextView mProfileUsername;
    private TextView mProfilePosts;
    private TextView mProfileRegistered;
    private TextView mProfileGroup;
    private TextView mProfileAge;
    private TextView mProfileEmail;
    private TextView mProfileIP;
    private TextView mProfileHostname;
    private LinearLayout mProfileEmailContainer;
    private LinearLayout mProfileIPContainer;
    private LinearLayout mProfileHostnameContainer;
    private ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mForum = Forum.getInstance();
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
        setContentView(R.layout.activity_profile_view);

        Intent intent = getIntent();
        mUsername = intent.getStringExtra("username");

        getSupportActionBar().setTitle(mUsername);

        mProfileAvatar = (ImageView) findViewById(R.id.profile_avatar);
        mProfileUsername = (TextView) findViewById(R.id.profile_username);
        mProfileUsername.setText(mUsername);

        mProfilePosts = (TextView) findViewById(R.id.profile_posts);
        mProfileRegistered = (TextView) findViewById(R.id.profile_registered);
        mProfileGroup = (TextView) findViewById(R.id.profile_group);
        mProfileAge = (TextView) findViewById(R.id.profile_age);
        mProfileEmail = (TextView) findViewById(R.id.profile_email);
        mProfileIP = (TextView) findViewById(R.id.profile_ip);
        mProfileHostname = (TextView) findViewById(R.id.profile_hostname);

        mProfileEmailContainer = (LinearLayout) findViewById(R.id.profile_email_container);
        mProfileIPContainer = (LinearLayout) findViewById(R.id.profile_ip_container);
        mProfileHostnameContainer = (LinearLayout) findViewById(R.id.profile_hostname_container);

        mDialog = new ProgressDialog(this);
        mDialog.setTitle(getString(R.string.loading));
        mDialog.setCancelable(false);

        getProfileInfo();
    }

    public void getProfileInfo() {
        mDialog.show();
        mForum.getUserInfo(mUsername, new XMLRPCCallback() {
            @Override
            public void onResponse(long id, Object result) {
                Map<String,Object> r = (Map<String,Object>) result;
                final String iconUrl = (String) r.get("icon_url");
                final Integer postCount = (Integer) r.get("post_count");
                final Date regTime = (Date) r.get("reg_time");
                final SimpleDateFormat fmt = new SimpleDateFormat("yyyy MMM dd");
                final Object[] customFieldsList = (Object[]) r.get("custom_fields_list");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Picasso.with(getApplicationContext()).load(iconUrl).fit().transform(new CircleTransform(getApplicationContext(), false)).into(mProfileAvatar);
                        mProfilePosts.setText(postCount.toString());
                        mProfileRegistered.setText(fmt.format(regTime));

                        for (int i=0;i<customFieldsList.length;i++) {
                            Map<String,Object> map = (Map<String,Object>) customFieldsList[i];
                            String name = new String((byte[]) map.get("name"), StandardCharsets.UTF_8);
                            String value = new String((byte[]) map.get("value"), StandardCharsets.UTF_8);
                            if (name.equals("Position")) {
                                mProfileGroup.setText(value);
                            } else if (name.equals("Email")) {
                                mProfileEmail.setText(value);
                                mProfileEmailContainer.setVisibility(View.VISIBLE);
                            } else if (name.equals("Age")) {
                                mProfileAge.setText(value);
                            } else if (name.equals("IP")) {
                                mProfileIP.setText(value);
                                mProfileIPContainer.setVisibility(View.VISIBLE);
                            } else if (name.equals("Hostname")) {
                                mProfileHostname.setText(value);
                                mProfileHostnameContainer.setVisibility(View.VISIBLE);
                            }
                        }

                        mDialog.dismiss();
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
                        Toast.makeText(getApplicationContext(), "Error loading profile!", Toast.LENGTH_LONG).show();
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
