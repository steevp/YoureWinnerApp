package com.yourewinner.yourewinner;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.text.Html;
import android.util.DisplayMetrics;

public class EmoteImageGetter implements Html.ImageGetter {
    private Context mContext;

    public EmoteImageGetter(Context context) {
        mContext = context;
    }

    @Override
    public Drawable getDrawable(String source) {
        final Resources res = mContext.getResources();
        final int id = res.getIdentifier(source, "drawable", mContext.getPackageName());
        if (id == 0) return null;

        final Drawable drawable = ResourcesCompat.getDrawable(res, id, null);
        final DisplayMetrics metrics = res.getDisplayMetrics();
        final int originalWidthScaled = (int) (drawable.getIntrinsicWidth() * metrics.density + 0.5f);
        final int originalHeightScaled = (int) (drawable.getIntrinsicHeight() * metrics.density + 0.5f);
        final int maxWidth = (int) (metrics.widthPixels - 48 * metrics.density + 0.5f);
        int width, height;

        if (originalWidthScaled > maxWidth) {
            width = maxWidth;
            height = originalHeightScaled * maxWidth / originalWidthScaled;
        } else {
            width = originalWidthScaled;
            height = originalHeightScaled;
        }

        drawable.setBounds(0, 0, width, height);
        return drawable;
    }
}
