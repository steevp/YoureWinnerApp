package com.yourewinner.yourewinner.wrapper;

import android.os.Parcel;
import android.os.Parcelable;

import java.nio.charset.Charset;
import java.util.Date;
import java.util.Map;

/**
 * Wrapper class around Objects returned by Tapatalk API
 */
public class PostsWrapper implements Parcelable {

    private String mTopicId;
    private String mAuthorName;
    private String mBoardName;
    private String mBoardId;
    private String mTopicTitle;
    private String mPostContent;
    private Date mPostTime;
    private String mAvatar;

    public PostsWrapper(Object data) {
        @SuppressWarnings("unchecked")
        Map<String,Object> map = (Map<String,Object>) data;

        mTopicId = (String) map.get("topic_id");

        if (map.get("post_author_name") != null) {
            mAuthorName = createString(map.get("post_author_name"));
        } else {
            mAuthorName = createString(map.get("topic_author_name"));
        }

        mBoardName = createString(map.get("forum_name"));
        mBoardId = (String) map.get("forum_id");
        mTopicTitle = createString(map.get("topic_title"));
        mPostContent = createString(map.get("short_content"));

        if (map.get("post_time") != null) {
            mPostTime = (Date) map.get("post_time");
        } else {
            mPostTime = (Date) map.get("last_reply_time");
        }

        mAvatar = (String) map.get("icon_url");
    }

    private PostsWrapper(Parcel parcel) {
        mTopicId = parcel.readString();
        mAuthorName = parcel.readString();
        mBoardName = parcel.readString();
        mBoardId = parcel.readString();
        mTopicTitle = parcel.readString();
        mPostContent = parcel.readString();
        mPostTime = new Date(parcel.readLong());
        mAvatar = parcel.readString();
    }

    public String getTopicId() {
        return mTopicId;
    }

    public String getAuthorName() {
        return mAuthorName;
    }

    public String getBoardName() {
        return mBoardName;
    }

    public String getBoardId() {
        return mBoardId;
    }

    public String getTopicTitle() {
        return mTopicTitle;
    }

    public String getPostContent() {
        return mPostContent;
    }

    public Date getPostTime() {
        return mPostTime;
    }

    public String getAvatar() {
        return mAvatar;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37 * result + Integer.parseInt(mTopicId);
        result = 37 * result + mAuthorName.hashCode();
        result = 37 * result + mBoardName.hashCode();
        result = 37 * result + Integer.parseInt(mBoardId);
        result = 37 * result + mTopicTitle.hashCode();
        result = 37 * result + mPostContent.hashCode();
        result = 37 * result + mPostTime.hashCode();
        result = 37 * result + mAvatar.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !PostsWrapper.class.isAssignableFrom(obj.getClass())) return false;
        final PostsWrapper other = (PostsWrapper) obj;
        return !(!mTopicId.equals(other.mTopicId) ||
                !mAuthorName.equals(other.mAuthorName) ||
                !mBoardName.equals(other.mBoardName) ||
                !mBoardId.equals(other.mBoardId) ||
                !mTopicTitle.equals(other.mTopicTitle) ||
                !mPostContent.equals(other.mPostContent) ||
                !mPostTime.equals(other.mPostTime) ||
                !mAuthorName.equals(other.mAuthorName));
    }

    @Override
    public String toString() {
        return "PostWrapper(topicId='" + mTopicId + "', " +
                "authorName='" + mAuthorName + "', " +
                "boardId='" + mBoardId + "', " +
                "topicTitle='" + mTopicTitle + "', " +
                "postContent='" + mPostContent + "', " +
                "postTime='" + mPostTime + "', " +
                "authorName='" + mAuthorName + "')";
    }

    /**
     * Create UTF-8 String from Object
     * @param data The Object to convert into a String
     * @return String The String
     */
    private String createString(Object data) {
        return new String((byte[]) data, Charset.forName("UTF-8"));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(mTopicId);
        parcel.writeString(mAuthorName);
        parcel.writeString(mBoardName);
        parcel.writeString(mBoardId);
        parcel.writeString(mTopicTitle);
        parcel.writeString(mPostContent);
        parcel.writeLong(mPostTime.getTime());
        parcel.writeString(mAvatar);
    }

    public final static Creator<PostsWrapper> CREATOR = new Creator<PostsWrapper>() {
        @Override
        public PostsWrapper createFromParcel(Parcel parcel) {
            return new PostsWrapper(parcel);
        }

        @Override
        public PostsWrapper[] newArray(int size) {
            return new PostsWrapper[size];
        }
    };
}
