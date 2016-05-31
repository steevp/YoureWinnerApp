package com.yourewinner.yourewinner;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.timroes.axmlrpc.XMLRPCCallback;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLRPCServerException;

/**
 * Created by steven on 5/19/16.
 */
public class TopicViewPageFragment extends Fragment
        implements UpdatableFragment, AdapterView.OnItemClickListener {

    public final static String ARG_BOARD_ID = "ARG_BOARD_ID";
    public final static String ARG_TOPIC_ID = "ARG_TOPIC_ID";
    public final static String ARG_PAGE = "ARG_PAGE";

    private String mBoardID;
    private String mTopicID;
    private int mPage;
    private String mTagName;
    private int mScrollPos;

    private Forum mForum;
    private ListView mPostsList;
    private TopicViewAdapter mPostsAdapter;
    private TopicViewDataFragment mDataFragment;
    private View mLoadingBar;
    private PageLoadedListener mCallback;
    private ProgressDialog mDialog;

    public interface PageLoadedListener {
        public void onPageCountChanged(int pageCount);
        public void jumpToUnread(int page, int scrollPos);
        public void onSubscribedStateChanged(boolean subscribedState);
        public void onCreateActionMode(ActionMode actionMode);
        public void onDestroyActionMode(ActionMode actionMode);
    }

    public static TopicViewPageFragment newInstance(String boardID, String topicID, int page) {
        TopicViewPageFragment fragment = new TopicViewPageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_BOARD_ID, boardID);
        args.putString(ARG_TOPIC_ID, topicID);
        args.putInt(ARG_PAGE, page);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        mPostsList.setItemChecked(position, true);
    }

    @Override
    public void update(SparseIntArray unreadPos) {
        mScrollPos = unreadPos.get(mPage, 0);
        mPostsList.setVisibility(View.GONE);
        mLoadingBar.setVisibility(View.VISIBLE);
        getTopic();
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
        mBoardID = args.getString(ARG_BOARD_ID);
        mTopicID = args.getString(ARG_TOPIC_ID);
        mPage = args.getInt(ARG_PAGE);
        mScrollPos = 0;

        mForum = Forum.getInstance();

        FragmentManager fm = getFragmentManager();
        mTagName = "page" + mPage;
        mDataFragment = (TopicViewDataFragment) fm.findFragmentByTag(mTagName);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_topic_view_page, container, false);
        mPostsList = (ListView) view.findViewById(R.id.posts_list);
        mPostsList.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        mPostsList.setOnItemClickListener(this);
        mPostsList.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode actionMode, int position, long id, boolean checked) {
                actionMode.invalidate(); // triggers onPrepareActionMode
            }

            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                MenuInflater inflater = actionMode.getMenuInflater();
                inflater.inflate(R.menu.menu_topic_view_contextual, menu);
                mCallback.onCreateActionMode(actionMode);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                MenuItem rate = menu.findItem(R.id.action_rate);
                MenuItem viewRatings = menu.findItem(R.id.action_view_rating);
                MenuItem quote = menu.findItem(R.id.action_quote);
                MenuItem edit = menu.findItem(R.id.action_edit);

                int selected = mPostsList.getCheckedItemCount();
                boolean loggedIn = mForum.getLogin();

                // Make them all invisible and selectively turn them on
                rate.setVisible(false);
                viewRatings.setVisible(false);
                quote.setVisible(false);
                edit.setVisible(false);

                if (loggedIn && selected == 1) {
                    rate.setVisible(true);
                    viewRatings.setVisible(true);
                    quote.setVisible(true);

                    SparseBooleanArray checkedStates = mPostsList.getCheckedItemPositions();
                    for (int i=0;i<checkedStates.size();i++) {
                        int key = checkedStates.keyAt(i);
                        if (checkedStates.get(key)) {
                            Map<String,Object> post = (Map<String,Object>) mPostsAdapter.getItem(key - 1);
                            boolean canEdit = (boolean) post.get("can_edit");
                            edit.setVisible(canEdit);
                            String username = new String((byte[]) post.get("post_author_name"), StandardCharsets.UTF_8);
                            actionMode.setTitle(username);
                            break;
                        }
                    }
                } else if (loggedIn && selected > 1) {
                    rate.setVisible(true);
                    actionMode.setTitle(selected + " selected");
                }

                return true;
            }

            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.action_rate:
                        showRateDialog();
                        return true;
                    case R.id.action_quote:
                        quotePost();
                        return true;
                    case R.id.action_edit:
                        editPost();
                        return true;
                    case R.id.action_view_rating:
                        viewRatings();
                        return true;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {
                mCallback.onDestroyActionMode(actionMode);
            }
        });
        mPostsAdapter = new TopicViewAdapter(getActivity(), inflater);
        mPostsList.setAdapter(mPostsAdapter);
        View header = inflater.inflate(R.layout.pagelinks, null);
        View footer = inflater.inflate(R.layout.pagelinks, null);
        mPostsList.addHeaderView(header);
        mPostsList.addFooterView(footer);
        TextView headerTextView = (TextView) header.findViewById(R.id.curpage);
        TextView footerTextView = (TextView) footer.findViewById(R.id.curpage);
        headerTextView.setText("Page " + mPage);
        footerTextView.setText("Page " + mPage);
        mLoadingBar = view.findViewById(R.id.loading_content);
        mDialog = new ProgressDialog(getActivity());
        mDialog.setIndeterminate(true);
        mDialog.setCancelable(false);
        mDialog.setMessage(getString(R.string.loading));
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
            if (mPage == 1) {
                getTopicByUnread();
            } else {
                getTopic();
            }
        } else {
            Object[] data = mDataFragment.getData();
            if (data.length > 0) {
                // Restore saved data
                mPostsAdapter.updateData(data);
                mLoadingBar.setVisibility(View.GONE);
                mPostsList.setVisibility(View.VISIBLE);
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
                            if (mScrollPos > 0) {
                                mPostsList.smoothScrollToPosition(mScrollPos + 1);
                            }
                            // Tell the activity to update the page count
                            mCallback.onPageCountChanged(pageCount);
                            mCallback.onSubscribedStateChanged(subscribedState);
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
                            mCallback.onPageCountChanged(pageCount);
                            if (pageCount > 1) {
                                // Save data for later
                                TopicViewDataFragment fragment = new TopicViewDataFragment();
                                fragment.setData(posts);
                                fm.beginTransaction().add(fragment, tagName).commit();
                                fm.executePendingTransactions();
                                // Jump to page
                                mCallback.jumpToUnread(page, scrollPos);
                            } else {
                                // 1 page, no need to switch
                                mPostsAdapter.updateData(posts);
                                mLoadingBar.setVisibility(View.GONE);
                                mPostsList.setVisibility(View.VISIBLE);
                                mPostsList.smoothScrollToPosition(scrollPos + 1);
                            }
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

    private void showRateDialog() {
        final AlertDialog alert = new AlertDialog.Builder(getActivity()).create();
        ListView alertView = new ListView(getActivity());
        final RatingListAdapter adapter = new RatingListAdapter(getActivity().getLayoutInflater());
        alertView.setAdapter(adapter);
        alertView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                mDialog.show();
                SparseBooleanArray checked = mPostsList.getCheckedItemPositions();
                List<Map<String,String>> ratingList = new ArrayList<Map<String, String>>();
                for (int i = 0, size = checked.size(); i < size; i++) {
                    final int key = checked.keyAt(i);
                    if (checked.get(key)) {
                        Map<String, Object> post = (Map<String, Object>) mPostsAdapter.getItem(key - 1);
                        String postID = post.get("post_id").toString();
                        String ratingID = Long.toString(adapter.getItemId(position));
                        Map<String,String> rating = new HashMap<String, String>();
                        rating.put("post_id", postID);
                        rating.put("rate_id", ratingID);
                        ratingList.add(rating);
                    }
                }
                new RatePostsTask().execute(ratingList);
                alert.dismiss();
            }
        });
        alert.setView(alertView);
        alert.setTitle(getString(R.string.rate_post));
        alert.show();
    }

    private void quotePost() {
        final Intent intent = new Intent(getActivity(), ReplyTopicActivity.class);
        intent.putExtra(ReplyTopicActivity.ARG_TOPIC_TITLE, getActivity().getTitle());
        intent.putExtra(ReplyTopicActivity.ARG_TOPIC_ID, mTopicID);
        intent.putExtra(ReplyTopicActivity.ARG_BOARD_ID, mBoardID);
        final SparseBooleanArray checked = mPostsList.getCheckedItemPositions();
        for (int i=0, size=checked.size();i<size;i++) {
            final int key = checked.keyAt(i);
            if (checked.get(key)) {
                final Map<String,Object> post = (Map<String,Object>) mPostsAdapter.getItem(key - 1);
                final String postID = post.get("post_id").toString();
                intent.putExtra("postID", postID);
                break;
            }
        }
        intent.putExtra("quote", true);
        startActivityForResult(intent, 666);
    }

    private void editPost() {
        final SparseBooleanArray checked = mPostsList.getCheckedItemPositions();
        for (int i = 0, size = checked.size(); i < size; i++) {
            final int key = checked.keyAt(i);
            if (checked.get(key)) {
                final Map<String, Object> post = (Map<String, Object>) mPostsAdapter.getItem(key - 1);
                final String postID = post.get("post_id").toString();
                final Intent intent = new Intent(getActivity(), EditPostActivity.class);
                intent.putExtra("postID", postID);
                startActivityForResult(intent, 666);
                break;
            }
        }
    }

    private void viewRatings() {
        final SparseBooleanArray checked = mPostsList.getCheckedItemPositions();
        for (int i=0, size=checked.size(); i<size; i++) {
            final int key = checked.keyAt(i);
            if (checked.get(key)) {
                final Map<String,Object> post = (Map<String,Object>) mPostsAdapter.getItem(key - 1);
                final String postID = (String) post.get("post_id");
                mForum.viewRatings(postID, new XMLRPCCallback() {
                    @Override
                    public void onResponse(long id, Object result) {
                        final Map<String,Object> r = (Map<String,Object>) result;
                        if ((boolean) r.get("result")) {
                            final Object[] ratings = (Object[]) r.get("ratings");
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    final AlertDialog alert = new AlertDialog.Builder(getActivity()).create();
                                    alert.setTitle("Ratings for this post");
                                    final ListView listView = new ListView(getActivity());
                                    final RatingViewAdapter adapter = new RatingViewAdapter(getActivity(), getActivity().getLayoutInflater(), ratings);
                                    listView.setAdapter(adapter);
                                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                            alert.dismiss();
                                        }
                                    });
                                    alert.setView(listView);
                                    alert.show();
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
                break;
            }
        }
    }

    private class RatePostsTask extends AsyncTask<List<Map<String,String>>,Void,Boolean> {
        @Override
        protected Boolean doInBackground(List<Map<String,String>>... params) {
            for (Map<String,String> post : params[0]) {
                String postID = post.get("post_id");
                String rateID = post.get("rate_id");
                try {
                    boolean result = mForum.ratePost(postID, rateID);
                    if (!result) {
                        // End early
                        return false;
                    }
                } catch (XMLRPCException e) {
                    e.printStackTrace();
                    return false;
                }
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            mDialog.dismiss();
            if (result) {
                Toast.makeText(getActivity(), "Rating successful!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getActivity(), "Rating failed!", Toast.LENGTH_LONG).show();
            }
            getTopic();
        }
    }
}
