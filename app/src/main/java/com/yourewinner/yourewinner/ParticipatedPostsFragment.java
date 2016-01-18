package com.yourewinner.yourewinner;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.Map;

import de.timroes.axmlrpc.XMLRPCCallback;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLRPCServerException;

public class ParticipatedPostsFragment extends Fragment {

    public final static String ARG_USERNAME = "ARG_USERNAME";

    private Forum mForum;
    private String mUsername;
    private ListView mPostsList;
    private PostAdapter mPostAdapter;

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
        View view = inflater.inflate(R.layout.fragment_participated_posts, container, false);
        mPostsList = (ListView) view.findViewById(R.id.posts_list);
        mPostAdapter = new PostAdapter(getActivity(), getActivity().getLayoutInflater());
        mPostsList.setAdapter(mPostAdapter);
        getParticipatedPosts();
        return view;
    }

    private void getParticipatedPosts() {
        mForum.getParticipatedTopic(mUsername, 1, new XMLRPCCallback() {
            @Override
            public void onResponse(long id, Object result) {
                Map<String,Object> r = (Map<String,Object>) result;
                final Object[] topics = (Object[]) r.get("topics");

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mPostAdapter.updateData(topics);
                    }
                });
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
