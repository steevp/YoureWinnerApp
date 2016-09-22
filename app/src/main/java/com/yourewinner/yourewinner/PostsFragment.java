package com.yourewinner.yourewinner;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.Map;

import de.timroes.axmlrpc.XMLRPCCallback;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLRPCServerException;

public class PostsFragment extends Fragment
        implements SwipeRefreshLayout.OnRefreshListener, AdapterView.OnItemClickListener, AbsListView.OnScrollListener, XMLRPCCallback {
    private final static String ARG_POSITION = "ARG_POSITION";
    // Needed for Participated
    private final static String ARG_USERNAME = "ARG_USERNAME";

    public final static int POS_RECENT = 0;
    public final static int POS_UNREAD = 1;
    public final static int POS_PARTICIPATED = 2;
    public final static int POS_SUBSCRIBED = 3;

    private int mPosition;
    private String mUsername;
    private Context mContext;
    private ListView mPostsList;
    private PostAdapter mPostAdapter;
    private SwipeRefreshLayout mSwipeContainer;
    private Forum mForum;
    private View mFooter;
    private View mEmptyView;

    private int lastCount = 0;
    private int currentPage = 1;
    private boolean isLoading = false;
    private boolean userScrolled = false;

    public static PostsFragment newInstance(int position, String username) {
        Bundle args = new Bundle();
        args.putInt(ARG_POSITION, position);
        args.putString(ARG_USERNAME, username);
        PostsFragment fragment = new PostsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mPosition = args.getInt(ARG_POSITION);
        mUsername = args.getString(ARG_USERNAME);
        mContext = getActivity();
        mForum = Forum.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_posts_view, container, false);
        mPostsList = (ListView) view.findViewById(R.id.posts_list);
        mSwipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        mSwipeContainer.setOnRefreshListener(this);
        mPostAdapter = new PostAdapter(mContext, inflater);
        mPostsList.setAdapter(mPostAdapter);
        mPostsList.setOnScrollListener(this);
        mPostsList.setOnItemClickListener(this);
        mFooter = inflater.inflate(R.layout.loading, null);
        mEmptyView = view.findViewById(R.id.empty_list_item);
        //mSwipeContainer.setRefreshing(true);
        // Workaround to show indicator
        mSwipeContainer.post(new Runnable() {
            @Override
            public void run() {
                mSwipeContainer.setRefreshing(true);
            }
        });
        lastCount = 0;
        currentPage = 1;
        userScrolled = false;
        isLoading = true;

        getPosts();

        return view;
    }

    public void getPosts() {
        switch (mPosition) {
            case POS_RECENT:
                mForum.getRecent(currentPage, this);
                break;
            case POS_UNREAD:
                mForum.getUnread(currentPage, this);
                break;
            case POS_PARTICIPATED:
                if (mForum.getLogin()) {
                    mForum.getParticipatedTopic(mUsername, currentPage, this);
                }
                break;
            case POS_SUBSCRIBED:
                if (mForum.getLogin()) {
                    mForum.getSubscribed(currentPage, this);
                }
                break;
        }
    }

    public void addMoreItems() {
        currentPage++;
        mPostsList.addFooterView(mFooter);
        getPosts();
    }

    @Override
    public void onRefresh() {
        mPostsList.setEmptyView(null);
        mPostAdapter.clear();
        mPostsList.removeFooterView(mFooter);
        lastCount = 0;
        currentPage = 1;
        getPosts();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Map<String,Object> topic = (Map<String,Object>) mPostAdapter.getItem(position);
        if (topic != null) {
            String topicID = (String) topic.get("topic_id");
            String boardID = (String) topic.get("forum_id");
            Intent intent = new Intent(mContext, TopicViewActivity.class);
            intent.putExtra(TopicViewActivity.ARG_TOPIC_ID, topicID);
            intent.putExtra(TopicViewActivity.ARG_BOARD_ID, boardID);
            startActivity(intent);
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if(scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
            userScrolled = true;
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        final int lastItem = firstVisibleItem + visibleItemCount - mPostsList.getFooterViewsCount();
        if (userScrolled && lastItem == totalItemCount && totalItemCount > lastCount && !isLoading) {
            isLoading = true;
            lastCount = totalItemCount;
            addMoreItems();
        }
    }

    @Override
    public void onResponse(long id, Object result) {
        Map<String, Object> r = (Map<String, Object>) result;
        final Object[] topics = (Object[]) r.get("topics");

        final Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mPostsList.removeFooterView(mFooter);
                    mPostAdapter.updateData(topics);
                    isLoading = false;
                    mSwipeContainer.setRefreshing(false);
                    mPostsList.setEmptyView(mEmptyView);
                }
            });
        }
    }

    @Override
    public void onError(long id, XMLRPCException error) {
        error.printStackTrace();
        final Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mPostsList.removeFooterView(mFooter);
                    isLoading = false;
                    mSwipeContainer.setRefreshing(false);
                    Toast.makeText(activity.getApplicationContext(), "ERROR: yourewinner.com might be down!", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @Override
    public void onServerError(long id, XMLRPCServerException error) {
        error.printStackTrace();
        final Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mPostsList.removeFooterView(mFooter);
                    isLoading = false;
                    mSwipeContainer.setRefreshing(false);
                    Toast.makeText(activity.getApplicationContext(), "ERROR: yourewinner.com might be down!", Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
