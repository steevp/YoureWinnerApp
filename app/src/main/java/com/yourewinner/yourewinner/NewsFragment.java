package com.yourewinner.yourewinner;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;

import de.timroes.axmlrpc.XMLRPCCallback;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLRPCServerException;

public class NewsFragment extends Fragment {
    private Forum mForum;
    private ListView mNewsList;
    private NewsAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mForum = Forum.getInstance();
        View view = inflater.inflate(R.layout.fragment_news, container, false);
        mNewsList = (ListView) view.findViewById(R.id.news_list);
        getNews();
        return view;
    }

    private void getNews() {
        final AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.getSupportActionBar().setTitle(getString(R.string.action_news));
        }
        mForum.getNews(new XMLRPCCallback() {
            @Override
            public void onResponse(long id, Object result) {
                final Map<String,Object> r = (Map<String,Object>) result;
                if ((boolean) r.get("result")) {
                    final Object[] newsObject = (Object[]) r.get("news");
                    final ArrayList<String> news = new ArrayList<String>();
                    for (int i=0; i<newsObject.length; i++) {
                        news.add(new String((byte[]) newsObject[i], StandardCharsets.UTF_8));
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mAdapter = new NewsAdapter(getActivity(), getActivity().getLayoutInflater(), news);
                            mNewsList.setAdapter(mAdapter);
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
