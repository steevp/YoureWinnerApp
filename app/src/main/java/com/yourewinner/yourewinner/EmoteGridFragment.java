package com.yourewinner.yourewinner;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

/**
 * Created by steven on 3/25/17.
 */

public class EmoteGridFragment extends Fragment {
    public final static String ARG_PAGE = "ARG_PAGE";

    private final static int PAGE_EMOTE = 0;
    private final static int PAGE_BBCODE = 1;

    private int mPage;
    private OnEmotePickedListener mCallback;

    public interface OnEmotePickedListener {
        public void onEmotePicked(String emote, boolean isBbcode);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (OnEmotePickedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnEmotePickedListener!");
        }
    }

    public static EmoteGridFragment newInstance(int position) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, position);
        EmoteGridFragment fragment = new EmoteGridFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPage = getArguments().getInt(ARG_PAGE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final GridView gridView = (GridView) inflater.inflate(R.layout.fragment_emote_grid, container, false);
        if (mPage == PAGE_EMOTE) {
            gridView.setAdapter(new EmoteGridAdapter(getActivity()));
        } else if (mPage == PAGE_BBCODE) {
            gridView.setAdapter(new BBCodeGridAdapter(getActivity()));
        }
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String emoteCode = gridView.getAdapter().getItem(i).toString();
                mCallback.onEmotePicked(emoteCode, mPage == PAGE_BBCODE);
            }
        });
        return gridView;
    }
}
