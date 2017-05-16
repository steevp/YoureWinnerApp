package com.yourewinner.yourewinner;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Toast;

import java.util.Map;

import de.timroes.axmlrpc.XMLRPCCallback;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLRPCServerException;

public class PostsFragment extends BaseFragment
        implements Loadable, SwipeRefreshLayout.OnRefreshListener, XMLRPCCallback, PostsAdapter.OnItemClickedListener {
    private final static String ARG_POSITION = "ARG_POSITION";
    // Needed for Participated
    private final static String ARG_USERNAME = "ARG_USERNAME";

    private final static String ARG_CURPAGE = "ARG_CURPAGE";
    private final static String ARG_LASTCOUNT = "ARG_LASTCOUNT";

    public final static int POS_RECENT = 0;
    public final static int POS_UNREAD = 1;
    public final static int POS_PARTICIPATED = 2;
    public final static int POS_SUBSCRIBED = 3;

    private int mPosition;
    private String mUsername;
    private Context mContext;

    private RecyclerView mRecyclerView;
    private PostsAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;

    private DataFragment mDataFragment;
    private String mTagName;

    private SwipeRefreshLayout mSwipeContainer;
    private Forum mForum;
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
        mTagName = "page" + mPosition;
        mDataFragment = (DataFragment) getFragmentManager().findFragmentByTag(mTagName);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDataFragment.setData(mAdapter.getData());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_posts_view, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.posts_recycler);
        mLayoutManager = new LinearLayoutManager(mRecyclerView.getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new PostsAdapter(mRecyclerView.getContext(), this);
        mRecyclerView.setAdapter(mAdapter);
        DividerItemDecoration divider = new DividerItemDecoration(mRecyclerView.getContext(), mLayoutManager.getOrientation());
        mRecyclerView.addItemDecoration(divider);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    userScrolled = true;
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int lastVisibleItem = mLayoutManager.findFirstVisibleItemPosition() + mRecyclerView.getChildCount();
                int totalItemCount = mLayoutManager.getItemCount();

                if (userScrolled && lastVisibleItem == totalItemCount && totalItemCount > lastCount && !isLoading) {
                    isLoading = true;
                    lastCount = totalItemCount;
                    addMoreItems();
                }
            }
        });

        mSwipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        mSwipeContainer.setOnRefreshListener(this);
        mEmptyView = view.findViewById(R.id.empty_list_item);

        if (savedInstanceState != null) {
            lastCount = savedInstanceState.getInt(ARG_LASTCOUNT);
            currentPage = savedInstanceState.getInt(ARG_CURPAGE);
        } else {
            lastCount = 0;
            currentPage = 1;
        }

        userScrolled = false;
        isLoading = true;

        loadData();

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ARG_LASTCOUNT, lastCount);
        outState.putInt(ARG_CURPAGE, currentPage);
    }

    @Override
    public void loadData() {
        if (mDataFragment == null) {
            // Create a data fragment to retain data during configuration changes
            mDataFragment = new DataFragment();
            getFragmentManager().beginTransaction().add(mDataFragment, mTagName).commit();
            // Workaround to show indicator
            mSwipeContainer.post(new Runnable() {
                @Override
                public void run() {
                    mSwipeContainer.setRefreshing(true);
                }
            });
            getPosts();
        } else {
            // Restore data from fragment
            final Object[] topics = mDataFragment.getData();
            if (topics != null) {
                addItems(topics);
                stopLoading();
            } else {
                // Data fragment empty? Try fetching data
                mSwipeContainer.post(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeContainer.setRefreshing(true);
                    }
                });
                getPosts();
            }
        }
    }
    public void getPosts() {
        switch (mPosition) {
            case POS_RECENT:
                setThreadId(mForum.getRecent(currentPage, this));
                break;
            case POS_UNREAD:
                setThreadId(mForum.getUnread(currentPage, this));
                break;
            case POS_PARTICIPATED:
                if (mForum.getLogin()) {
                    setThreadId(mForum.getParticipatedTopic(mUsername, currentPage, this));
                }
                break;
            case POS_SUBSCRIBED:
                if (mForum.getLogin()) {
                    setThreadId(mForum.getSubscribed(currentPage, this));
                }
                break;
        }
    }

    @Override
    protected void resumeThread() {
        loadData();
    }

    private void addItems(Object[] items) {
        if ((mLayoutManager.getItemCount() + items.length) == 0) {
            // Empty RecyclerView
            mEmptyView.setVisibility(View.VISIBLE);
        } else {
            mEmptyView.setVisibility(View.GONE);
            mAdapter.updateData(items);
        }
    }
    private void addMoreItems() {
        currentPage++;
        mAdapter.setFooterEnabled(true);
        getPosts();
    }

    private void stopLoading() {
        mAdapter.setFooterEnabled(false);
        isLoading = false;
        mSwipeContainer.post(new Runnable() {
            @Override
            public void run() {
                mSwipeContainer.setRefreshing(false);
            }
        });
    }

    @Override
    public void onRefresh() {
        mAdapter.clear();
        lastCount = 0;
        currentPage = 1;
        getPosts();
    }

    @Override
    public void onResponse(long id, Object result) {
        setThreadId(0);
        Map<String, Object> r = (Map<String, Object>) result;
        final Object[] topics = (Object[]) r.get("topics");

        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                stopLoading();
                addItems(topics);
            }
        });
    }

    @Override
    public void onError(long id, XMLRPCException error) {
        setThreadId(0);
        error.printStackTrace();
        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                stopLoading();
                Toast.makeText(mContext, "ERROR: yourewinner.com might be down!", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onServerError(long id, XMLRPCServerException error) {
        setThreadId(0);
        error.printStackTrace();
        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                stopLoading();
                Toast.makeText(mContext, "ERROR: yourewinner.com might be down!", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == TopicViewActivity.RESULT_DELETED) {
            String topicId = data.getStringExtra(TopicViewActivity.ARG_TOPIC_ID);
            mAdapter.removeItem(topicId);
        }
    }

    @Override
    public void onItemClicked(Map<String, Object> item) {
        // Load topic
        String topicId = (String) item.get("topic_id");
        String boardId = (String) item.get("forum_id");
        Intent intent = TopicViewActivity.createIntent(getActivity(), topicId, boardId);
        startActivityForResult(intent, 1);
    }
}
