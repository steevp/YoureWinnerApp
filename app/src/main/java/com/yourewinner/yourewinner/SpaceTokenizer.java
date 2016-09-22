package com.yourewinner.yourewinner;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.widget.MultiAutoCompleteTextView;

/**
 * Created by steven on 9/17/16.
 */
public class SpaceTokenizer implements MultiAutoCompleteTextView.Tokenizer {
    @Override
    public int findTokenStart(CharSequence text, int cursor) {
        int i = cursor;
        while (i > 0 && !Character.isWhitespace(text.charAt(i - 1))) {
            i--;
        }
        while (i < cursor && Character.isWhitespace(text.charAt(i))) {
            i++;
        }
        return i;
    }

    @Override
    public int findTokenEnd(CharSequence text, int cursor) {
        int i = cursor;
        int len = text.length();
        while (i < len) {
            if (Character.isWhitespace(text.charAt(i))) {
                return i;
            } else {
                i++;
            }
        }
        return len;
    }

    @Override
    public CharSequence terminateToken(CharSequence text) {
        int i = text.length();
        while (i > 0 && Character.isWhitespace(text.charAt(i - 1))) {
            i--;
        }
        if (i > 0 && Character.isWhitespace(text.charAt(i - 1))) {
            return text;
        } else {
            if (text instanceof Spanned) {
                SpannableString sp = new SpannableString(text + " ");
                TextUtils.copySpansFrom((Spanned) text, 0, text.length(), Object.class, sp, 0);
                return sp;
            } else {
                return text + " ";
            }
        }
    }
}
