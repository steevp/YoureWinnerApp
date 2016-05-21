package com.yourewinner.yourewinner;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.TextView;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import de.timroes.axmlrpc.XMLRPCCallback;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLRPCServerException;

/**
 * Created by steven on 5/19/16.
 */
public class TopicViewPageFragment extends Fragment {

    public final static String ARG_TOPIC_ID = "ARG_TOPIC_ID";
    public final static String ARG_PAGE = "ARG_PAGE";
    public final static String ARG_JUMP_TO_UNREAD = "ARG_JUMP_TO_UNREAD";
    public final static String ARG_SCROLL_POS = "ARG_SCROLL_POS";

    private String mTopicID;
    private int mPage;
    private boolean mJumpToUnread;
    private int mScrollPos;
    private String mTagName;

    private Forum mForum;
    private CustomListView mPostsList;
    private TopicViewAdapter mPostsAdapter;
    private TopicViewDataFragment mDataFragment;
    private View mLoadingBar;
    private PageLoadedListener mCallback;

    public interface PageLoadedListener {
        void onPageCountChanged(int pageCount);
        void jumpToUnread(int page, int scrollPos);
        void onSubscribedStateChanged(boolean subscribedState);
    }

    public static TopicViewPageFragment newInstance(String topicID, int page, boolean jumpToUnread, int scrollPos) {
        TopicViewPageFragment fragment = new TopicViewPageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TOPIC_ID, topicID);
        args.putInt(ARG_PAGE, page);
        args.putBoolean(ARG_JUMP_TO_UNREAD, jumpToUnread);
        args.putInt(ARG_SCROLL_POS, scrollPos);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (PageLoadedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement PageLoadedListener!");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        mTopicID = args.getString(ARG_TOPIC_ID);
        mPage = args.getInt(ARG_PAGE);
        mJumpToUnread = args.getBoolean(ARG_JUMP_TO_UNREAD);
        mScrollPos = args.getInt(ARG_SCROLL_POS);

        mForum = Forum.getInstance();

        FragmentManager fm = getFragmentManager();
        mTagName = "page" + mPage;
        mDataFragment = (TopicViewDataFragment) fm.findFragmentByTag(mTagName);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_topic_view_page, container, false);
        mPostsList = (CustomListView) view.findViewById(R.id.posts_list);
        mPostsList.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        mPostsList.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode actionMode, int i, long l, boolean b) {

            }

            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                MenuInflater menuInflater = actionMode.getMenuInflater();
                menuInflater.inflate(R.menu.menu_topic_view_contextual, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.action_rate:
                        return true;
                    case R.id.action_quote:
                        return true;
                    case R.id.action_edit:
                        return true;
                    case R.id.action_view_rating:
                        return true;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {

            }
        });
        mPostsAdapter = new TopicViewAdapter(getActivity(), inflater);
        mPostsList.setAdapter(mPostsAdapter);
        View header = inflater.inflate(R.layout.pagelinks, null);
        View footer = inflater.inflate(R.layout.pagelinks, null);
        mPostsList.addHeaderView(header);
        mPostsList.addFooterView(footer);
        TextView pageTextView = (TextView) header.findViewById(R.id.curpage);
        pageTextView.setText("Page " + mPage);
        mLoadingBar = view.findViewById(R.id.loading_content);
        loadData();
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDataFragment.setData(mPostsAdapter.getData());
    }

    public void loadData() {
        if (mDataFragment == null) {
            mDataFragment = new TopicViewDataFragment();
            getFragmentManager().beginTransaction().add(mDataFragment, mTagName).commit();
            getTopic();
        } else {
            Object[] data = mDataFragment.getData();
            if (data.length > 0) {
                // Restore saved data
                mPostsAdapter.updateData(data);
                mLoadingBar.setVisibility(View.GONE);
                mPostsList.setVisibility(View.VISIBLE);
                mPostsList.smoothScrollToPosition(mScrollPos);
            } else {
                // Fetch data
                getTopic();
            }
        }
    }

    public void getTopic() {
        mForum.getTopic(mTopicID, mPage, new XMLRPCCallback() {
            @Override
            public void onResponse(long id, Object result) {
                Map<String,Object> r = (Map<String,Object>) result;
                int totalPosts = (int) r.get("total_post_num");
                final int pageCount = (int) Math.ceil((double) totalPosts / 15);
                final Object[] posts = (Object[]) r.get("posts");
                final String topicTitle = new String((byte[]) r.get("topic_title"), StandardCharsets.UTF_8);
                final boolean subscribedState = (boolean) r.get("is_subscribed");
                final Activity activity = getActivity();
                // Might get destroyed before thread finishes
                if (activity != null) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mPostsAdapter.updateData(posts);
                            mLoadingBar.setVisibility(View.GONE);
                            mPostsList.setVisibility(View.VISIBLE);
                            // Tell the activity to update the page count
                            mCallback.onPageCountChanged(pageCount);
                            mCallback.onSubscribedStateChanged(subscribedState);
                            activity.setTitle(topicTitle);
                            if (mJumpToUnread) {
                                mJumpToUnread = false;
                                getTopicByUnread();
                            }
                        }
                    });
                }
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

    public void getTopicByUnread() {
        mForum.getTopicByUnread(mTopicID, new XMLRPCCallback() {
            @Override
            public void onResponse(long id, Object result) {
                Map<String,Object> r = (Map<String,Object>) result;
                int totalPosts = (int) r.get("total_post_num");
                final int pageCount = (int) Math.ceil((double) totalPosts / 15);
                int pos = (int) r.get("position");
                final int page = (int) Math.ceil((double) pos / 15);
                final int scrollPos = pos % 15;
                final Object[] posts = (Object[]) r.get("posts");
                final String topicTitle = new String((byte[]) r.get("topic_title"), StandardCharsets.UTF_8);
                final Activity activity = getActivity();
                final FragmentManager fm = getFragmentManager();
                final String tagName = "page" + page;
                // Might get destroyed before thread finishes
                if (activity != null && fm != null) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Save data for later
                            TopicViewDataFragment fragment = new TopicViewDataFragment();
                            fragment.setData(posts);
                            fm.beginTransaction().add(fragment, tagName).commit();
                            fm.executePendingTransactions();
                            mCallback.onPageCountChanged(pageCount);
                            mCallback.jumpToUnread(page, scrollPos);
                            activity.setTitle(topicTitle);
                        }
                    });
                }
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
}
