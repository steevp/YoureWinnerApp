package com.yourewinner.yourewinner.wrapper;

import android.os.Parcel;
import android.os.Parcelable;

import java.nio.charset.Charset;
import java.util.Date;
import java.util.Map;

public class PrivateMessageWrapper implements Parcelable {

    private String mAvatar;
    private String mSender;
    private String mRecipients;
    private String mSubject;
    private String mContent;
    private Date mSentDate;
    private String mMessageId;

    public PrivateMessageWrapper(Object data) {
        @SuppressWarnings("unchecked")
        final Map<String,Object> map = (Map<String,Object>) data;

        mAvatar = (String) map.get("icon_url");

        // Get recipients
        final StringBuilder builder = new StringBuilder();
        final Object[] recipients = (Object[]) map.get("msg_to");
        for (int i=0, size=recipients.length;i<size; i++) {
            @SuppressWarnings("unchecked")
            final Map<String,Object> r = (Map<String, Object>) recipients[i];
            builder.append(createString(r.get("username")));
            if (i + 1 < size) {
                builder.append(", ");
            }
        }

        mRecipients = builder.toString();
        mSender = createString(map.get("msg_from"));
        mSubject = createString(map.get("msg_subject"));
        mContent = createString(map.get("short_content"));
        mSentDate = (Date) map.get("sent_date");
        mMessageId = (String) map.get("msg_id");
    }

    private PrivateMessageWrapper(Parcel parcel) {
        mAvatar = parcel.readString();
        mSender = parcel.readString();
        mRecipients = parcel.readString();
        mSubject = parcel.readString();
        mContent = parcel.readString();
        mSentDate = new Date(parcel.readLong());
        mMessageId = parcel.readString();
    }

    public String getAvatar() {
        return mAvatar;
    }

    public String getSender() {
        return mSender;
    }

    public String getRecipients() {
        return mRecipients;
    }

    public String getSubject() {
        return mSubject;
    }

    public String getContent() {
        return mContent;
    }

    public Date getSentDate() {
        return mSentDate;
    }

    public String getMessageId() {
        return mMessageId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(mAvatar);
        parcel.writeString(mSender);
        parcel.writeString(mRecipients);
        parcel.writeString(mSubject);
        parcel.writeString(mContent);
        parcel.writeLong(mSentDate.getTime());
        parcel.writeString(mMessageId);
    }

    public final static Creator<PrivateMessageWrapper> CREATOR = new Creator<PrivateMessageWrapper>() {
        @Override
        public PrivateMessageWrapper createFromParcel(Parcel parcel) {
            return new PrivateMessageWrapper(parcel);
        }

        @Override
        public PrivateMessageWrapper[] newArray(int size) {
            return new PrivateMessageWrapper[size];
        }
    };

    /**
     * Create UTF-8 String from Object
     * @param data The Object to convert into a String
     * @return String The String
     */
    private String createString(Object data) {
        return new String((byte[]) data, Charset.forName("UTF-8"));
    }
}
