package com.yourewinner.yourewinner;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by steven on 3/25/17.
 */

public class EmotePagerAdapter extends FragmentPagerAdapter {
    private final int PAGE_COUNT = 2;
    private String[] tabTitles = new String[]{"Emotes", "BBCode"};
    private Context mContext;

    public EmotePagerAdapter(FragmentManager fm, Context c) {
        super(fm);
        mContext = c;
    }

    @Override
    public Fragment getItem(int position) {
        return EmoteGridFragment.newInstance(position);
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }
}
