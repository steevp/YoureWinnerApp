package com.yourewinner.yourewinner;

import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * Retains data during configuration changes
 */
public class DataFragment extends Fragment {
    private Object[] mData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public void setData(Object[] data) {
        mData = data;
    }

    public Object[] getData() {
        return mData;
    }
}
