package com.yourewinner.yourewinner;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.Map;

import de.timroes.axmlrpc.XMLRPCCallback;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLRPCServerException;

public class NewsFragment extends BaseFragment implements XMLRPCCallback, Loadable {
    private Forum mForum;
    private ListView mNewsList;
    private NewsAdapter mAdapter;

    private DataFragment mDataFragment;
    private final static String TAG = "news";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mForum = Forum.getInstance();
        View view = inflater.inflate(R.layout.fragment_news, container, false);
        mNewsList = (ListView) view.findViewById(R.id.news_list);
        mAdapter = new NewsAdapter(getActivity(), getActivity().getLayoutInflater());
        mNewsList.setAdapter(mAdapter);
        mDataFragment = (DataFragment) getFragmentManager().findFragmentByTag(TAG);
        loadData();
        return view;
    }

    private void getNews() {
        setThreadId(mForum.getNews(this));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDataFragment.setData(mAdapter.getData());
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

    @Override
    public void loadData() {
        if (mDataFragment == null) {
            mDataFragment = new DataFragment();
            getFragmentManager().beginTransaction().add(mDataFragment, TAG).commit();
            getNews();
        } else {
            Object[] news = mDataFragment.getData();
            if (news != null && news.length > 0) {
                mAdapter.updateData(news);
            } else {
                // Fetch data
                getNews();
            }
        }
    }
}
