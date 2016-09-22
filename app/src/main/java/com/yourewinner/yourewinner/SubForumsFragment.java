package com.yourewinner.yourewinner;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;

import de.timroes.axmlrpc.XMLRPCCallback;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLRPCServerException;

public class SubForumsFragment extends Fragment implements ExpandableListView.OnChildClickListener{
    public static final String ARG_PAGE = "ARG_PAGE";

    private Forum mForum;
    private ExpandableListView mSubForumsList;
    private SubForumsAdapter mSubForumsAdapter;
    private int mPage;

    public static SubForumsFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        SubForumsFragment fragment = new SubForumsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPage = getArguments().getInt(ARG_PAGE);
        mForum = Forum.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sub_forums, container, false);
        mSubForumsList = (ExpandableListView) view;
        mSubForumsList.setOnChildClickListener(this);
        getSubForums();
        return view;
    }

    public void getSubForums() {
        final AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.getSupportActionBar().setTitle(getString(R.string.action_browse));
        }
        mForum.getForum(new XMLRPCCallback() {
            @Override
            public void onResponse(long id, Object result) {
                final Object[] data = (Object[]) result;
                final Activity activity = getActivity();
                if (activity != null) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mSubForumsAdapter = new SubForumsAdapter(getActivity().getApplicationContext(), getActivity().getLayoutInflater(), data);
                            mSubForumsList.setAdapter(mSubForumsAdapter);
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

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        Map<String,Object> child = (Map<String,Object>) mSubForumsAdapter.getChild(groupPosition, childPosition);
        final String boardID = (String) child.get("forum_id");
        final String boardName = new String((byte[]) child.get("forum_name"), StandardCharsets.UTF_8);
        final Object[] c = (Object[]) child.get("child");
        ArrayList<Map<String,Object>> children = new ArrayList<Map<String,Object>>();
        if (c != null) {
            for (int i=0;i<c.length;i++) {
                children.add((Map<String,Object>) c[i]);
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
    public void onResume() {
        super.onResume();
        getSubForums();
    }
}