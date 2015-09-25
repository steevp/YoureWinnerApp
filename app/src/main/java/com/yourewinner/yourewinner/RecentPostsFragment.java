package com.yourewinner.yourewinner;

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

public class RecentPostsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, AdapterView.OnItemClickListener, AbsListView.OnScrollListener {
    public static final String ARG_POSITION = "ARG_POSITION";
    int mPosition;

    Context mContext;
    ListView mPostsList;
    PostAdapter mPostAdapter;
    SwipeRefreshLayout mSwipeContainer;
    Forum mForum;
    View mFooter;

    private int lastCount = 0;
    private int currentPage = 1;
    private boolean isLoading = false;
    private boolean userScrolled = false;

    public static RecentPostsFragment newInstance(int position) {
        Bundle args = new Bundle();
        args.putInt(ARG_POSITION, position);
        RecentPostsFragment fragment = new RecentPostsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPosition = getArguments().getInt(ARG_POSITION);
        mContext = getActivity().getApplicationContext();
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
        mPostsList.removeFooterView(mFooter);
        mSwipeContainer.setRefreshing(true);
        lastCount = 0;
        currentPage = 1;
        userScrolled = false;
        isLoading = true;
        getRecent();
        return view;
    }

    public void getRecent() {
        //mPostsList.addFooterView(mFooter);
        //mSwipeContainer.setRefreshing(true);

        mForum.getRecent(currentPage, new XMLRPCCallback() {
            @Override
            public void onResponse(long id, Object result) {
                Map<String, Object> r = (Map<String, Object>) result;
                final Object[] topics = (Object[]) r.get("topics");

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mPostsList.removeFooterView(mFooter);
                        mPostAdapter.updateData(topics);
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
                        Toast.makeText(getActivity().getApplicationContext(), "FUCK: yourewinner.com might be down!", Toast.LENGTH_LONG).show();
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
                        Toast.makeText(getActivity().getApplicationContext(), "FUCK: yourewinner.com might be down!", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    public void addMoreItems() {
        currentPage++;
        mPostsList.addFooterView(mFooter);
        getRecent();
    }

    @Override
    public void onRefresh() {
        mPostAdapter.clear();
        mPostsList.removeFooterView(mFooter);
        lastCount = 0;
        currentPage = 1;
        getRecent();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Map<String,Object> topic = (Map<String,Object>) mPostAdapter.getItem(position);
        if (topic != null) {
            String topicID = (String) topic.get("topic_id");
            Intent intent = new Intent(mContext, TopicViewActivity.class);
            intent.putExtra("topicID", topicID);
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
}
