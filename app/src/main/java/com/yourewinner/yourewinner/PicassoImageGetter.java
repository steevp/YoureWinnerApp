package com.yourewinner.yourewinner;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class PicassoImageGetter implements Html.ImageGetter {

    final Resources resources;

    final Picasso pablo;

    final TextView textView;

    public PicassoImageGetter(final TextView textView, final Resources resources, final Picasso pablo) {
        this.textView = textView;
        this.resources = resources;
        this.pablo = pablo;
    }

    @Override
    public Drawable getDrawable(final String source) {
        final BitmapDrawablePlaceHolder result = new BitmapDrawablePlaceHolder();

        new AsyncTask<Void, Void, Bitmap>() {

            @Override
            protected Bitmap doInBackground(final Void... meh) {
                try {
                    return pablo.load(source).get();
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(final Bitmap bitmap) {
                try {
                    final BitmapDrawable drawable = new BitmapDrawable(resources, bitmap);

                    final DisplayMetrics metrics = resources.getDisplayMetrics();
                    final int originalWidthScaled = (int) (drawable.getIntrinsicWidth() * metrics.density + 0.5f);
                    final int originalHeightScaled = (int) (drawable.getIntrinsicHeight() * metrics.density + 0.5f);

                    int width, height;

                    if (originalWidthScaled > metrics.widthPixels) {
                        height = originalHeightScaled * metrics.widthPixels / originalWidthScaled;
                        width = metrics.widthPixels;
                    } else {
                        height = originalHeightScaled;
                        width = originalWidthScaled;
                    }

                    drawable.setBounds(0, 0, width, height);

                    result.setDrawable(drawable);
                    result.setBounds(0, 0, width, height);

                    textView.setText(textView.getText()); // invalidate() doesn't work correctly...
                    textView.setMovementMethod(LinkMovementMethod.getInstance());
                } catch (Exception e) {
                /* nom nom nom*/
                }
            }

        }.execute((Void) null);

        return result;
    }

    static class BitmapDrawablePlaceHolder extends BitmapDrawable {

        protected Drawable drawable;

        @Override
        public void draw(final Canvas canvas) {
            if (drawable != null) {
                drawable.draw(canvas);
            }
        }

        public void setDrawable(Drawable drawable) {
            this.drawable = drawable;
        }

    }
}