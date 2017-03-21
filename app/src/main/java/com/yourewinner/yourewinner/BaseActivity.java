package com.yourewinner.yourewinner;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Created by steven on 3/21/17.
 */

public abstract class BaseActivity extends AppCompatActivity {
    private long mThreadId; // ID of running network thread
    private boolean mResumeThread; // Should we resume the thread?

    public void setThreadId(long id) {
        mThreadId = id;
    }

    @Override
    protected void onPause() {
        if (mThreadId != 0) {
            Log.i("ywtag", "canceling thread " + mThreadId);
            Forum.getInstance().cancel(mThreadId);
            mResumeThread = true;
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (mResumeThread) {
            Log.i("ywtag", "resuming thread");
            mResumeThread = false;
            resumeThread();
        }
        super.onResume();
    }

    protected abstract void resumeThread();
}
