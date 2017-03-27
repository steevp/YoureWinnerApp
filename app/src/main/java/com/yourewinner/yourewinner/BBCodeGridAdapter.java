package com.yourewinner.yourewinner;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.ColorInt;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

/**
 * Created by steven on 3/26/17.
 */

public class BBCodeGridAdapter extends BaseAdapter {
    private Context mContext;

    private String[] mBBCodes = {
            "[IMG]", "[B]", "[I]", "[U]", "[S]",
            "[URL]", "[MOVE]", "[QUOTE]", "[CODE]",
    };

    public BBCodeGridAdapter(Context c) {
        mContext = c;
    }

    @Override
    public int getCount() {
        return mBBCodes.length;
    }

    @Override
    public Object getItem(int i) {
        return mBBCodes[i];
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        TextView button;
        if (view == null) {
            button = new TextView(mContext);
            button.setTextSize(14);
            button.setLayoutParams(new GridView.LayoutParams(125, 125));
            button.setGravity(Gravity.CENTER);
            button.setSingleLine(true);
            // Change background color to match theme
            TypedValue typedValue = new TypedValue();
            Resources.Theme theme = mContext.getTheme();
            theme.resolveAttribute(R.attr.colorPrimary, typedValue, true);
            @ColorInt int color = typedValue.data;
            button.setBackgroundColor(color);
        } else {
            // Recycle
            button = (TextView) view;
        }

        button.setText(mBBCodes[i]);
        return button;
    }
}
