package com.yourewinner.yourewinner;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Map;

import de.timroes.axmlrpc.XMLRPCCallback;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLRPCServerException;

public class NewsFragment extends BaseFragment implements XMLRPCCallback {
    private final static String NEWS_LIST = "NEWS_LIST";

    private Forum mForum;
    private ListView mNewsList;
    private NewsAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mForum = Forum.getInstance();
        View view = inflater.inflate(R.layout.fragment_news, container, false);
        mNewsList = (ListView) view.findViewById(R.id.news_list);


        if (savedInstanceState != null) {
            final ArrayList<String> newsList = savedInstanceState.getStringArrayList(NEWS_LIST);
            mAdapter = new NewsAdapter(getActivity(), newsList);
            mNewsList.setAdapter(mAdapter);
        } else {
            mAdapter = new NewsAdapter(getActivity());
            mNewsList.setAdapter(mAdapter);
            getNews();
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList(NEWS_LIST, mAdapter.getData());
    }

    private void getNews() {
        setThreadId(mForum.getNews(this));
    }

    @Override
    protected void resumeThread() {
        getNews();
    }

    @Override
    public void onResponse(long id, Object result) {
        setThreadId(0);
        final Map<String,Object> r = (Map<String,Object>) result;
        if ((boolean) r.get("result")) {
            final Object[] news = (Object[]) r.get("news");
            final Activity activity = getActivity();
            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.updateData(news);
                    }
                });
            }
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
}
