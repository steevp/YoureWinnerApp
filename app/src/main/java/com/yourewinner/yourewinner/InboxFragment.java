package com.yourewinner.yourewinner;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class InboxFragment extends Fragment {

    private ViewPager mViewPager;
    private TabLayout mTabLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inbox, container, false);
        mViewPager = (ViewPager) view.findViewById(R.id.viewpager);
        mTabLayout = (TabLayout) view.findViewById(R.id.sliding_tabs);
        setupTabLayout();
        return view;
    }

    private void setupTabLayout() {
        mViewPager.setAdapter(new InboxViewPagerAdapter(getActivity().getSupportFragmentManager(), getActivity()));
        mTabLayout.setupWithViewPager(mViewPager);
    }
}
