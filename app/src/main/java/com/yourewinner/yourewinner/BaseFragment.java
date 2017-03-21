package com.yourewinner.yourewinner;

import android.support.v4.app.Fragment;
import android.util.Log;

/**
 * Same as BaseActivity except it extends a Fragment
 */

public abstract class BaseFragment extends Fragment {
    private long mThreadId; // ID of running network thread
    private boolean mResumeThread; // Should we resume the thread?

    public void setThreadId(long id) {
        mThreadId = id;
    }

    @Override
    public void onPause() {
        if (mThreadId != 0) {
            Log.i("ywtag", "canceling thread " + mThreadId);
            Forum.getInstance().cancel(mThreadId);
            mResumeThread = true;
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        if (mResumeThread) {
            Log.i("ywtag", "resuming thread");
            mResumeThread = false;
            resumeThread();
        }
        super.onResume();
    }

    protected abstract void resumeThread();
}
