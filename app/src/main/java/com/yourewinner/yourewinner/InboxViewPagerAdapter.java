package com.yourewinner.yourewinner;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;

public class InboxViewPagerAdapter extends FragmentStatePagerAdapter {

    private final static String[] tabTitles = {"inbox", "sent"};
    private Context mContext;

    public InboxViewPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = new PrivateMessageFragment();
        Bundle args = new Bundle();
        args.putString(PrivateMessageFragment.ARG_BOXID, tabTitles[position]);
        fragment.setArguments(args);
        return fragment;
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
