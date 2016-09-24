package com.yourewinner.yourewinner;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import de.timroes.axmlrpc.XMLRPCCallback;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLRPCServerException;

public class ProfileViewActivity extends AppCompatActivity implements View.OnClickListener {

    private Forum mForum;
    private String mUsername;
    private CircleImageView mProfileAvatar;
    private TextView mProfileUsername;
    private TextView mProfilePosts;
    private TextView mProfileRegistered;
    private TextView mProfileGroup;
    private TextView mProfileAge;
    private TextView mProfileEmail;
    private TextView mProfileIP;
    private TextView mProfileHostname;
    private LinearLayout mProfilePostsContainer;
    private LinearLayout mProfileEmailContainer;
    private LinearLayout mProfileIPContainer;
    private LinearLayout mProfileHostnameContainer;
    private LinearLayout mLoadingBar;
    private ScrollView mProfileScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Config.loadTheme(this);
        mForum = Forum.getInstance();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_view);

        Intent intent = getIntent();
        mUsername = intent.getStringExtra("username");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        getSupportActionBar().setTitle(mUsername);

        mProfileAvatar = (CircleImageView) findViewById(R.id.profile_avatar);
        mProfileUsername = (TextView) findViewById(R.id.profile_username);
        mProfileUsername.setText(mUsername);

        mProfilePosts = (TextView) findViewById(R.id.profile_posts);
        mProfileRegistered = (TextView) findViewById(R.id.profile_registered);
        mProfileGroup = (TextView) findViewById(R.id.profile_group);
        mProfileAge = (TextView) findViewById(R.id.profile_age);
        mProfileEmail = (TextView) findViewById(R.id.profile_email);
        mProfileIP = (TextView) findViewById(R.id.profile_ip);
        mProfileHostname = (TextView) findViewById(R.id.profile_hostname);

        mLoadingBar = (LinearLayout) findViewById(R.id.loading_bar);
        mProfileScrollView = (ScrollView) findViewById(R.id.profile_scrollview);

        mProfilePostsContainer = (LinearLayout) findViewById(R.id.profile_posts_container);
        mProfilePostsContainer.setOnClickListener(this);
        mProfileEmailContainer = (LinearLayout) findViewById(R.id.profile_email_container);
        mProfileIPContainer = (LinearLayout) findViewById(R.id.profile_ip_container);
        mProfileHostnameContainer = (LinearLayout) findViewById(R.id.profile_hostname_container);

        getProfileInfo();
    }

    public void getProfileInfo() {
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
                        if (iconUrl.length() > 0) {
                            Picasso.with(getApplicationContext())
                                    .load(iconUrl)
                                    .placeholder(R.drawable.no_avatar)
                                    .fit()
                                    .into(mProfileAvatar);
                        } else {
                            mProfileAvatar.setImageResource(R.drawable.no_avatar);
                        }
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
                        mLoadingBar.setVisibility(View.GONE);
                        mProfileScrollView.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void onError(long id, XMLRPCException error) {
                error.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
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
                        Toast.makeText(getApplicationContext(), "FUCK: yourewinner.com might be down!", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    @Override
    public void onClick(View v) {
        getUserPosts();
    }

    private void getUserPosts() {
        Intent intent = new Intent(this, ParticipatedPostsActivity.class);
        intent.putExtra(ParticipatedPostsActivity.ARG_USERNAME, mUsername);
        startActivity(intent);
    }
}
