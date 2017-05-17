package com.yourewinner.yourewinner;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Map;

import de.timroes.axmlrpc.XMLRPCCallback;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLRPCServerException;

public class SubForumsFragment extends BaseFragment
        implements Loadable, ExpandableListView.OnChildClickListener, XMLRPCCallback {
    private Forum mForum;
    private ExpandableListView mSubForumsList;
    private SubForumsAdapter mSubForumsAdapter;
    private DataFragment mDataFragment;
    private final static String TAG = "subforums";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mForum = Forum.getInstance();
        mDataFragment = (DataFragment) getFragmentManager().findFragmentByTag(TAG);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sub_forums, container, false);
        mSubForumsList = (ExpandableListView) view;
        mSubForumsList.setOnChildClickListener(this);
        mSubForumsAdapter = new SubForumsAdapter();
        mSubForumsList.setAdapter(mSubForumsAdapter);
        loadData();
        return view;
    }

    public void getSubForums() {
        setThreadId(mForum.getForum(this));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDataFragment.setData(mSubForumsAdapter.getData());
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        @SuppressWarnings("unchecked")
        Map<String,Object> child = (Map<String,Object>) mSubForumsAdapter.getChild(groupPosition, childPosition);
        final String boardID = (String) child.get("forum_id");
        final String boardName = new String((byte[]) child.get("forum_name"), Charset.forName("UTF-8"));
        final Object[] c = (Object[]) child.get("child");
        ArrayList<Map<String,Object>> children = new ArrayList<Map<String,Object>>();
        if (c != null) {
            for (Object aC : c) {
                //noinspection unchecked
                children.add((Map<String, Object>) aC);
            }
        }
        Intent intent = new Intent(getActivity(), BoardViewActivity.class);
        intent.putExtra("boardID", boardID);
        intent.putExtra("boardName", boardName);
        intent.putExtra("children", children);
        startActivity(intent);
        return false;
    }

    @Override
    public void loadData() {
        if (mDataFragment == null) {
            mDataFragment = new DataFragment();
            getFragmentManager().beginTransaction().add(mDataFragment, TAG).commit();
            getSubForums();
        } else {
            final Object[] data = mDataFragment.getData();
            if (data != null && data.length > 0) {
                mSubForumsAdapter.updateData(data);
            } else {
                getSubForums();
            }
        }
    }

    @Override
    public void onResponse(long id, Object result) {
        setThreadId(0);
        final Object[] data = (Object[]) result;
        final Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mSubForumsAdapter.updateData(data);
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

    @Override
    protected void resumeThread() {
        getSubForums();
    }
}