package com.yourewinner.yourewinner;

import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;

public class PostsViewPagerAdapter extends FragmentPagerAdapter {
    private String[] tabTitles;
    private Context mContext;

    public PostsViewPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        mContext = context;
        tabTitles = mContext.getResources().getStringArray(R.array.tab_titles);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return RecentPostsFragment.newInstance(position + 1);
            case 1:
                return UnreadPostsFragment.newInstance(position + 1);
            case 3:
                return SubscribedPostsFragment.newInstance(position + 1);

        }
        return SubscribedPostsFragment.newInstance(position + 1);
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
