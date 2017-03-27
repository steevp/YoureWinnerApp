package com.yourewinner.yourewinner;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

/**
 * Created by steven on 3/25/17.
 */

public class EmoteDialog extends DialogFragment {
    private final static String ARG_PAGE = "ARG_PAGE";
    private int mPage;

    public static EmoteDialog newInstance(int page) {
        EmoteDialog fragment = new EmoteDialog();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPage = getArguments().getInt(ARG_PAGE);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogSlideAnim);
    }

    @Override
    public void onResume() {
        // Fill bottom half of screen
        Window window = getDialog().getWindow();
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels / 2;
        window.setLayout(width, height);
        window.setGravity(Gravity.BOTTOM);
        /*WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
        wlp.gravity = Gravity.BOTTOM;
        // Needed to make the dialog fill the screen width apparently
        /*TypedArray a = getDialog().getContext().obtainStyledAttributes(new int[]{android.R.attr.layout_width});
        try {
            wlp.width = a.getLayoutDimension(0, WindowManager.LayoutParams.MATCH_PARENT);
        } finally {
            a.recycle();
        }
        window.setAttributes(wlp);*/
        super.onResume();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.emote_pager, container, false);

        ViewPager viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        viewPager.setAdapter(new EmotePagerAdapter(getChildFragmentManager(), getActivity()));
        viewPager.setCurrentItem(mPage);

        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);
        return view;
    }
}
