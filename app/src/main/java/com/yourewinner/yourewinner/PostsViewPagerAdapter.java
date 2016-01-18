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
        switch (position) {
            case 0:
                // Recent
                return RecentPostsFragment.newInstance(position + 1);
            case 1:
                // Unread
                return UnreadPostsFragment.newInstance(position + 1);
            case 2:
                // Participated
                if (mForum.getLogin()) {
                    return ParticipatedPostsFragment.newInstance(mForum.getUsername());
                } else {
                    return new Fragment();
                }
            case 3:
                // Subscribed
                return SubscribedPostsFragment.newInstance(position + 1);
            default:
                return new Fragment();
        }
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
