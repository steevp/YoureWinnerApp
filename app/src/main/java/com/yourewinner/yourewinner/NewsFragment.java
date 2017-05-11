package com.yourewinner.yourewinner;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Map;

import de.timroes.axmlrpc.XMLRPCCallback;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLRPCServerException;

public class NewsFragment extends BaseFragment implements XMLRPCCallback {
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
            final Object[] newsObject = (Object[]) r.get("news");
            final ArrayList<String> news = new ArrayList<String>();
            for (int i=0; i<newsObject.length; i++) {
                news.add(new String((byte[]) newsObject[i], Charset.forName("UTF-8")));
            }
            final Activity activity = getActivity();
            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter = new NewsAdapter(activity, activity.getLayoutInflater(), news);
                        mNewsList.setAdapter(mAdapter);
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
