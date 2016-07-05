package com.yourewinner.yourewinner;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class PostsViewPagerAdapter extends FragmentStatePagerAdapter {
    private String[] tabTitles;
    private Context mContext;
    private Forum mForum;

    public PostsViewPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        mContext = context;
        tabTitles = mContext.getResources().getStringArray(R.array.tab_titles);
        mForum = Forum.getInstance();
    }

    @Override
    public Fragment getItem(int position) {
        return PostsFragment.newInstance(position, mForum.getUsername());
    }

    @Override
    public int getCount() {
        return tabTitles.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }
}
