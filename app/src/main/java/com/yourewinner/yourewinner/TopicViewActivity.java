package com.yourewinner.yourewinner;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.SparseIntArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import de.timroes.axmlrpc.XMLRPCCallback;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLRPCServerException;

public class TopicViewActivity extends AppCompatActivity
        implements TopicViewPageFragment.PageLoadedListener, ViewPager.OnPageChangeListener {

    public final static String ARG_TOPIC_TITLE = "ARG_TOPIC_TITLE";
    public final static String ARG_TOPIC_ID = "ARG_TOPIC_ID";
    public final static String ARG_BOARD_ID = "ARG_BOARD_ID";
    public final static String ARG_PAGE = "ARG_PAGE";
    public final static String ARG_PAGE_COUNT = "ARG_PAGE_COUNT";
    public final static String ARG_SUBSCRIBED = "ARG_SUBSCRIBED";
    public final static int RESULT_RELOAD = 666;

    private Forum mForum;
    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;
    private ActionMode mActionMode;
    private int mPage;
    private int mPageCount;
    private SparseIntArray mUnreadPos;

    private String mTopicID;
    private String mBoardID;
    private boolean mSubscribed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Config.loadTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic_view2);

        String topicTitle;
        if (savedInstanceState != null) {
            topicTitle = savedInstanceState.getString(ARG_TOPIC_TITLE);
            mTopicID = savedInstanceState.getString(ARG_TOPIC_ID);
            mBoardID = savedInstanceState.getString(ARG_BOARD_ID);
            mPage = savedInstanceState.getInt(ARG_PAGE);
            mPageCount = savedInstanceState.getInt(ARG_PAGE_COUNT);
            mSubscribed = savedInstanceState.getBoolean(ARG_SUBSCRIBED);
        } else {
            Intent intent = getIntent();
            topicTitle = "";
            mPage = 0;
            mPageCount = 1;
            mTopicID = intent.getStringExtra(ARG_TOPIC_ID);
            mBoardID = intent.getStringExtra(ARG_BOARD_ID);
            mSubscribed = false;
        }

        mUnreadPos = new SparseIntArray(0);

        mForum = Forum.getInstance();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(topicTitle);

        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new TopicViewPagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPager.addOnPageChangeListener(this);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem actionReply = menu.findItem((R.id.action_reply));
        MenuItem actionSubscribe = menu.findItem(R.id.action_subscribe);
        MenuItem actionUnsubscribe = menu.findItem(R.id.action_unsubscribe);

        if (mForum.getLogin()) {
            actionReply.setVisible(true);
            actionUnsubscribe.setVisible(mSubscribed);
            actionSubscribe.setVisible(!mSubscribed);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_topic_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_reply:
                startReplyTopic();
                return true;
            case R.id.action_subscribe:
                subscribeTopic();
                return true;
            case R.id.action_unsubscribe:
                unsubscribeTopic();
                return true;
        }
        return false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ARG_TOPIC_TITLE, getTitle().toString());
        outState.putString(ARG_TOPIC_ID, mTopicID);
        outState.putString(ARG_BOARD_ID, mBoardID);
        outState.putInt(ARG_PAGE, mPage);
        outState.putInt(ARG_PAGE_COUNT, mPageCount);
        outState.putBoolean(ARG_SUBSCRIBED, mSubscribed);
    }

    @Override
    public void onPageCountChanged(int pageCount) {
        if (mPageCount != pageCount) {
            mPageCount = pageCount;
            mPagerAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onSubscribedStateChanged(boolean subscribedState) {
        mSubscribed = subscribedState;
        supportInvalidateOptionsMenu(); // triggers onPrepareOptionsMenu
    }

    @Override
    public void onCreateActionMode(ActionMode actionMode) {
        mActionMode = actionMode;
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {
        mActionMode = null;
    }

    @Override
    public void jumpToUnread(int page, int scrollPos) {
        mUnreadPos.put(page, scrollPos);
        mPager.setCurrentItem(page - 1, false);
        mPagerAdapter.notifyDataSetChanged();
    }

    public void selectPage(View v) {
        final AlertDialog alert = new AlertDialog.Builder(this).create();
        alert.setTitle("Jump to");
        List<String> pageList = new ArrayList<String>(mPageCount);
        for (int i=0;i<mPageCount;i++) {
            pageList.add(i, "Page " + (i + 1));
        }
        ListView alertView = new ListView(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, pageList);
        alertView.setAdapter(adapter);
        alertView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mPager.setCurrentItem(position);
                alert.dismiss();
            }
        });
        alert.setView(alertView);
        alert.show();
    }

    public void goFirstPage(View v) {
        mPager.setCurrentItem(0);
    }

    public void previousPage(View v) {
        if (mPage > 0) {
            mPager.setCurrentItem(mPage - 1);
        }
    }

    public void nextPage(View v) {
        if (mPage < mPageCount - 1) {
            mPager.setCurrentItem(mPage + 1);
        }
    }

    public void goLastPage(View v) {
        mPager.setCurrentItem(mPageCount - 1);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        mPage = position;
        if (mActionMode != null) {
            mActionMode.finish();
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private void startReplyTopic() {
        Intent intent = new Intent(this, ReplyTopicActivity.class);
        intent.putExtra(ReplyTopicActivity.ARG_TOPIC_TITLE, getTitle().toString());
        intent.putExtra(ReplyTopicActivity.ARG_TOPIC_ID, mTopicID);
        intent.putExtra(ReplyTopicActivity.ARG_BOARD_ID, mBoardID);
        startActivityForResult(intent, 666);
    }

    private void subscribeTopic() {
        mForum.subscribeTopic(mTopicID, new XMLRPCCallback() {
            @Override
            public void onResponse(long id, Object result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Subscribed!", Toast.LENGTH_LONG).show();
                        mSubscribed = true;
                        supportInvalidateOptionsMenu();
                    }
                });
            }

            @Override
            public void onError(long id, XMLRPCException error) {
                error.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Failed to subscribe to topic!", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onServerError(long id, XMLRPCServerException error) {
                error.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Please check your internet connection!", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void unsubscribeTopic() {
        mForum.unsubscribeTopic(mTopicID, new XMLRPCCallback() {
            @Override
            public void onResponse(long id, Object result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Unsubscribed!", Toast.LENGTH_LONG).show();
                        mSubscribed = false;
                        supportInvalidateOptionsMenu();
                    }
                });
            }

            @Override
            public void onError(long id, XMLRPCException error) {
                error.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Failed to unsubscribe from topic!", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onServerError(long id, XMLRPCServerException error) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Please check your internet connection!", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_RELOAD) {
            mPager.setCurrentItem(mPageCount - 1, false);
            mPagerAdapter.notifyDataSetChanged();
        }
    }

    private class TopicViewPagerAdapter extends FragmentStatePagerAdapter {

        public TopicViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return TopicViewPageFragment.newInstance(mBoardID, mTopicID, position + 1);
        }

        @Override
        public int getItemPosition(Object object) {
            UpdatableFragment fragment = (UpdatableFragment) object;
            fragment.update(mUnreadPos);
            return super.getItemPosition(object);
        }

        @Override
        public int getCount() {
            return mPageCount;
        }
    }
}
