package com.yourewinner.yourewinner;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.View;
import android.widget.ListView;

/**
 * Custom ListView with OnItemCheckedListener
 */
public class CustomListView extends ListView {
    private SparseBooleanArray mCheckStates;
    private OnItemCheckedListener mCallback;

    public CustomListView(Context context) {
        super(context);
        init();
    }

    public CustomListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(21)
    public CustomListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        mCheckStates = new SparseBooleanArray(0);
        this.setChoiceMode(CHOICE_MODE_MULTIPLE);
    }

    public void setOnItemCheckedListener(ActionMode.Callback callback) {
        try {
            mCallback = (OnItemCheckedListener) callback;
        } catch (ClassCastException e) {
            throw new ClassCastException(callback.toString() + " must implement OnItemCheckedListener!");
        }
    }

    @Override
    public boolean performItemClick(View view, int position, long id) {
        boolean result = super.performItemClick(view, position, id);
        if (mCallback != null) {
            boolean checked = !mCheckStates.get(position, false);
            mCheckStates.put(position, checked);
            mCallback.onItemChecked(position, checked);
        }
        return result;
    }

    @Override
    public void clearChoices() {
        mCheckStates.clear();
        super.clearChoices();
    }

    public interface OnItemCheckedListener {
        public void onItemChecked(int position, boolean checked);
    }
}
