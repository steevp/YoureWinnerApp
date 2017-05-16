package com.yourewinner.yourewinner;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.timroes.axmlrpc.XMLRPCCallback;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLRPCServerException;

public class TopicViewPageFragment extends BaseFragment
        implements Loadable, UpdatableFragment, TopicViewAdapter.ItemCheckedStateListener {

    public final static String ARG_BOARD_ID = "ARG_BOARD_ID";
    public final static String ARG_TOPIC_ID = "ARG_TOPIC_ID";
    public final static String ARG_PAGE = "ARG_PAGE";

    private String mBoardID;
    private String mTopicID;
    private int mPage;
    private String mTagName;
    private int mScrollPos;

    private Forum mForum;
    private RecyclerView mRecyclerView;
    private TopicViewAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private ActionMode mActionMode;

    private DataFragment mDataFragment;
    private View mLoadingBar;
    private PageLoadedListener mCallback;
    private ProgressDialog mDialog;

    @Override
    protected void resumeThread() {
        loadData();
    }

    @Override
    public void onItemCheckedStateChanged() {
        if (mActionMode == null) {
            getActivity().startActionMode(new TopicViewCallBack());
        }
        mActionMode.invalidate(); // triggers onPrepareActionMode
    }

    public interface PageLoadedListener {
        public void onPageCountChanged(int pageCount);
        public void jumpToUnread(int page, int scrollPos);
        public void onSubscribedStateChanged(boolean subscribedState);
        public void onCreateActionMode(ActionMode actionMode);
        public void onDestroyActionMode(ActionMode actionMode);
        public void destroyActionMode();
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
    public void update(SparseIntArray unreadPos) {
        mScrollPos = unreadPos.get(mPage, 0);
        //mPostsList.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.GONE);
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
        mDataFragment = (DataFragment) fm.findFragmentByTag(mTagName);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_topic_view_page, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.posts_recycler);
        mLayoutManager = new LinearLayoutManager(mRecyclerView.getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new TopicViewAdapter(mRecyclerView.getContext(), mPage, this);
        mRecyclerView.setAdapter(mAdapter);
        // Add divider
        DividerItemDecoration divider = new DividerItemDecoration(mRecyclerView.getContext(), mLayoutManager.getOrientation());
        mRecyclerView.addItemDecoration(divider);

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
        // Release youtube loaders
        mAdapter.releaseLoaders();
        mDataFragment.setData(mAdapter.getData());
    }

    @Override
    public void loadData() {
        if (mDataFragment == null) {
            mDataFragment = new DataFragment();
            getFragmentManager().beginTransaction().add(mDataFragment, mTagName).commit();
            if (mPage == 1) {
                getTopicByUnread();
            } else {
                getTopic();
            }
        } else {
            Object[] data = mDataFragment.getData();
            if (data != null && data.length > 0) {
                // Restore saved data
                //mPostsAdapter.updateData(data);
                mAdapter.updateData(data);
                mLoadingBar.setVisibility(View.GONE);
                //mPostsList.setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.VISIBLE);
            } else {
                // Fetch data
                getTopic();
            }
        }
    }

    public void getTopic() {
        final long id = mForum.getTopic(mTopicID, mPage, new XMLRPCCallback() {
            @Override
            public void onResponse(long id, Object result) {
                setThreadId(0);
                Map<String,Object> r = (Map<String,Object>) result;
                int totalPosts = (int) r.get("total_post_num");
                final int pageCount = (int) Math.ceil((double) totalPosts / 15);
                final Object[] posts = (Object[]) r.get("posts");
                final String topicTitle = new String((byte[]) r.get("topic_title"), Charset.forName("UTF-8"));
                final boolean subscribedState = (boolean) r.get("is_subscribed");
                final Activity activity = getActivity();
                // Might get destroyed before thread finishes
                if (activity != null) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //mPostsAdapter.updateData(posts);
                            mAdapter.updateData(posts);
                            mLoadingBar.setVisibility(View.GONE);
                            //mPostsList.setVisibility(View.VISIBLE);
                            mRecyclerView.setVisibility(View.VISIBLE);
                            if (mScrollPos > 0) {
                                mRecyclerView.scrollToPosition(mScrollPos + 1);
                                mRecyclerView.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        mRecyclerView.smoothScrollToPosition(mScrollPos + 1);
                                    }
                                }, 420);
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
                setThreadId(0);
                error.printStackTrace();
            }

            @Override
            public void onServerError(long id, XMLRPCServerException error) {
                setThreadId(0);
                error.printStackTrace();
            }
        });
        setThreadId(id);
    }

    public void getTopicByUnread() {
        final long id = mForum.getTopicByUnread(mTopicID, new XMLRPCCallback() {
            @Override
            public void onResponse(long id, Object result) {
                setThreadId(0);
                Map<String,Object> r = (Map<String,Object>) result;
                int totalPosts = (int) r.get("total_post_num");
                final int pageCount = (int) Math.ceil((double) totalPosts / 15);
                int pos = (int) r.get("position");
                final int page = (int) Math.ceil((double) pos / 15);
                final int scrollPos = pos % 15;
                final Object[] posts = (Object[]) r.get("posts");
                final String topicTitle = new String((byte[]) r.get("topic_title"), Charset.forName("UTF-8"));
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
                                DataFragment fragment = new DataFragment();
                                fragment.setData(posts);
                                fm.beginTransaction().add(fragment, tagName).commit();
                                fm.executePendingTransactions();
                                // Jump to page
                                mCallback.jumpToUnread(page, scrollPos);
                            } else {
                                // 1 page, no need to switch
                                mAdapter.updateData(posts);
                                mLoadingBar.setVisibility(View.GONE);
                                mRecyclerView.setVisibility(View.VISIBLE);
                                mRecyclerView.scrollToPosition(scrollPos + 1);
                                mRecyclerView.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        mRecyclerView.smoothScrollToPosition(scrollPos + 1);
                                    }
                                }, 420);
                            }
                            activity.setTitle(topicTitle);
                        }
                    });
                }
            }

            @Override
            public void onError(long id, XMLRPCException error) {
                setThreadId(0);
                error.printStackTrace();
            }

            @Override
            public void onServerError(long id, XMLRPCServerException error) {
                setThreadId(0);
                error.printStackTrace();
            }
        });
        setThreadId(id);
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
                SparseBooleanArray checked = mAdapter.getCheckedItemPositions();
                List<Map<String,String>> ratingList = new ArrayList<Map<String, String>>();
                for (int i = 0, size = checked.size(); i < size; i++) {
                    final int key = checked.keyAt(i);
                    if (checked.get(key)) {
                        Map<String, Object> post = mAdapter.getItem(key - 1);
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
        final SparseBooleanArray checked = mAdapter.getCheckedItemPositions();
        for (int i=0, size=checked.size();i<size;i++) {
            final int key = checked.keyAt(i);
            if (checked.get(key)) {
                final Map<String,Object> post = mAdapter.getItem(key - 1);
                final String postID = post.get("post_id").toString();
                intent.putExtra("postID", postID);
                break;
            }
        }
        intent.putExtra("quote", true);
        startActivityForResult(intent, 666);
    }

    private void editPost() {
        final SparseBooleanArray checked = mAdapter.getCheckedItemPositions();
        for (int i = 0, size = checked.size(); i < size; i++) {
            final int key = checked.keyAt(i);
            if (checked.get(key)) {
                final Map<String, Object> post = mAdapter.getItem(key - 1);
                final String postID = post.get("post_id").toString();
                final Intent intent = new Intent(getActivity(), EditPostActivity.class);
                intent.putExtra("postID", postID);
                startActivityForResult(intent, 666);
                break;
            }
        }
    }

    private void deletePost() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Delete post");
        builder.setMessage("Are you sure you want to delete this post?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int position) {
                final SparseBooleanArray checked = mAdapter.getCheckedItemPositions();
                for (int i = 0, size = checked.size(); i < size; i++) {
                    final int key = checked.keyAt(i);
                    if (checked.get(key)) {
                        final Map<String, Object> post = mAdapter.getItem(key - 1);
                        final String postID = post.get("post_id").toString();
                        mForum.deletePost(postID, new XMLRPCCallback() {
                            @Override
                            public void onResponse(long id, Object result) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mCallback.destroyActionMode();
                                        Toast.makeText(getActivity(), "Post deleted!", Toast.LENGTH_LONG).show();
                                        mAdapter.removeItem(key);
                                    }
                                });
                            }

                            @Override
                            public void onError(long id, XMLRPCException error) {
                                error.printStackTrace();
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getActivity(), "Unable to delete post!", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }

                            @Override
                            public void onServerError(long id, XMLRPCServerException error) {
                                error.printStackTrace();
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getActivity(), "Unable to delete post!", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        });
                        break;
                    }
                }
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int position) {

            }
        });
        builder.show();
    }

    private void viewRatings() {
        final SparseBooleanArray checked = mAdapter.getCheckedItemPositions();
        for (int i=0, size=checked.size(); i<size; i++) {
            final int key = checked.keyAt(i);
            if (checked.get(key)) {
                final Map<String,Object> post = mAdapter.getItem(key - 1);
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

    private void shareLink() {
        final SparseBooleanArray checked = mAdapter.getCheckedItemPositions();
        for (int i=0, size=checked.size(); i<size; i++) {
            final int key = checked.keyAt(i);
            if (checked.get(key)) {
                final Map<String,Object> post = mAdapter.getItem(key - 1);
                final String postID = (String) post.get("post_id");
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT, "http://yourewinner.com/index.php?topic=" + mTopicID + ".msg" + postID + "#msg" + postID);
                intent.setType("text/plain");
                startActivity(Intent.createChooser(intent, getResources().getText(R.string.action_share)));
                break;
            }
        }
    }

    private class TopicViewCallBack implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            mActionMode = actionMode;
            actionMode.getMenuInflater().inflate(R.menu.menu_topic_view_contextual, menu);
            mCallback.onCreateActionMode(actionMode);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            if (mAdapter.getCheckedItemCount() == 0) {
                actionMode.finish();
                return false;
            }

            MenuItem rate = menu.findItem(R.id.action_rate);
            MenuItem viewRatings = menu.findItem(R.id.action_view_rating);
            MenuItem quote = menu.findItem(R.id.action_quote);
            MenuItem edit = menu.findItem(R.id.action_edit);
            MenuItem share = menu.findItem(R.id.action_share);
            MenuItem delete = menu.findItem(R.id.action_delete_post);

            int selected = mAdapter.getCheckedItemCount();
            boolean loggedIn = mForum.getLogin();

            // Make them all invisible and selectively turn them on
            rate.setVisible(false);
            viewRatings.setVisible(false);
            quote.setVisible(false);
            edit.setVisible(false);
            delete.setVisible(false);
            share.setVisible(selected == 1);

            if (loggedIn && selected == 1) {
                rate.setVisible(true);
                viewRatings.setVisible(true);
                quote.setVisible(true);

                SparseBooleanArray checkedStates = mAdapter.getCheckedItemPositions();
                for (int i=0;i<checkedStates.size();i++) {
                    int key = checkedStates.keyAt(i);
                    if (checkedStates.get(key)) {
                        Map<String,Object> post = mAdapter.getItem(key - 1);
                        boolean canEdit = (boolean) post.get("can_edit");
                        edit.setVisible(canEdit);
                        boolean canDelete = (boolean) post.get("can_delete");
                        delete.setVisible(canDelete);
                        String username = new String((byte[]) post.get("post_author_name"), Charset.forName("UTF-8"));
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
                case R.id.action_share:
                    shareLink();
                    return true;
                case R.id.action_delete_post:
                    deletePost();
                    return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            mActionMode = null;
            mAdapter.clearCheckedItems();
            mCallback.onDestroyActionMode(actionMode);
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
