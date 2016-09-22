package com.yourewinner.yourewinner;

import java.util.ArrayList;
import java.util.List;

/**
 * Collects list of usernames for @tagging autocomplete
 */
public class Mentions {
    private static Mentions ourInstance = new Mentions();
    private List<String> mMentions;

    public static Mentions getInstance() {
        return ourInstance;
    }

    private Mentions() {
        mMentions = new ArrayList<String>();
    }

    public void addMention(String username) {
        final String mention = "@" + username;
        if (!mMentions.contains(mention)) {
            mMentions.add(mention);
        }
    }

    public List<String> getMentions() {
        return mMentions;
    }
}
