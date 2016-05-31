package com.yourewinner.yourewinner;

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

import java.util.Map;

import de.timroes.axmlrpc.XMLRPCCallback;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLRPCServerException;

public class ParticipatedPostsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, AbsListView.OnScrollListener, AdapterView.OnItemClickListener {

    public final static String ARG_USERNAME = "ARG_USERNAME";

    private Forum mForum;
    private String mUsername;
    private ListView mPostsList;
    private PostAdapter mPostAdapter;
    private SwipeRefreshLayout mSwipeContainer;
    private View mFooter;

    private int lastCount = 0;
    private int currentPage = 1;
    private boolean isLoading = false;
    private boolean userScrolled = false;

    public static ParticipatedPostsFragment newInstance(String username) {
        Bundle args = new Bundle();
        args.putString(ARG_USERNAME, username);
        ParticipatedPostsFragment fragment = new ParticipatedPostsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mForum = Forum.getInstance();
        mUsername = getArguments().getString(ARG_USERNAME);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_posts_view, container, false);
        mPostsList = (ListView) view.findViewById(R.id.posts_list);
        mPostsList.setOnScrollListener(this);
        mPostsList.setOnItemClickListener(this);
        mSwipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        mSwipeContainer.setOnRefreshListener(this);
        mPostAdapter = new PostAdapter(getActivity(), getActivity().getLayoutInflater());
        mPostsList.setAdapter(mPostAdapter);
        mFooter = inflater.inflate(R.layout.loading, null);

        lastCount = 0;
        currentPage = 1;
        userScrolled = false;
        isLoading = true;

        getParticipatedPosts();
        return view;
    }

    private void getParticipatedPosts() {
        mForum.getParticipatedTopic(mUsername, currentPage, new XMLRPCCallback() {
            @Override
            public void onResponse(long id, Object result) {
                Map<String,Object> r = (Map<String,Object>) result;
                final Object[] topics = (Object[]) r.get("topics");

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mPostAdapter.updateData(topics);
                        mPostsList.removeFooterView(mFooter);
                        isLoading = false;
                        mSwipeContainer.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onError(long id, XMLRPCException error) {
                error.printStackTrace();

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mPostsList.removeFooterView(mFooter);
                        isLoading = false;
                        mSwipeContainer.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onServerError(long id, XMLRPCServerException error) {
                error.printStackTrace();

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mPostsList.removeFooterView(mFooter);
                        isLoading = false;
                        mSwipeContainer.setRefreshing(false);
                    }
                });
            }
        });
    }

    @Override
    public void onRefresh() {
        mPostAdapter.clear();
        mPostsList.removeFooterView(mFooter);
        lastCount = 0;
        currentPage = 1;
        getParticipatedPosts();
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

    private void addMoreItems() {
        currentPage++;
        mPostsList.addFooterView(mFooter);
        getParticipatedPosts();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Map<String,Object> topic = (Map<String,Object>) mPostAdapter.getItem(position);
        if (topic != null) {
            String topicID = (String) topic.get("topic_id");
            String boardID = (String) topic.get("forum_id");
            Intent intent = new Intent(getActivity(), TopicViewActivity.class);
            intent.putExtra(TopicViewActivity.ARG_BOARD_ID, boardID);
            intent.putExtra(TopicViewActivity.ARG_TOPIC_ID, topicID);
            startActivity(intent);
        }
    }
}
