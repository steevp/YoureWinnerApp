package com.yourewinner.yourewinner;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final static int TYPE_ITEM = 0;
    private final static int TYPE_FOOTER = 1;

    private Context mContext;
    private ArrayList<PostsWrapper> mDataSet;
    private OnItemClickedListener mCallback;
    private boolean mShowFooter;

    public interface OnItemClickedListener {
        void onItemClicked(PostsWrapper post);
    }

    // Holds a reference to each view
    public static class VHItem extends RecyclerView.ViewHolder {
        public TextView mBoardName;
        public TextView mTopicTitle;
        public TextView mUsername;
        public CircleImageView mAvatar;
        public TextView mPostContent;
        public TextView mDate;

        public VHItem(View itemView) {
            super(itemView);
            mBoardName = (TextView) itemView.findViewById(R.id.board_name);
            mTopicTitle = (TextView) itemView.findViewById(R.id.topic_title);
            mUsername = (TextView) itemView.findViewById(R.id.username);
            mAvatar = (CircleImageView) itemView.findViewById(R.id.avatar);
            mPostContent = (TextView) itemView.findViewById(R.id.post_content);
            mDate = (TextView) itemView.findViewById(R.id.post_time);
        }

        public void bind(final PostsWrapper item, final OnItemClickedListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onItemClicked(item);
                }
            });
        }
    }

    // Holds the footer
    public static class VHFooter extends RecyclerView.ViewHolder {

        public VHFooter(View itemView) {
            super(itemView);
        }
    }

    public PostsAdapter(Context context, OnItemClickedListener listener) {
        mContext = context;
        mCallback = listener;
        mDataSet = new ArrayList<>();
    }

    public PostsAdapter(Context context, OnItemClickedListener listener, ArrayList<PostsWrapper> posts) {
        mContext = context;
        mCallback = listener;
        mDataSet = posts;
    }

    public void setFooterEnabled(boolean b) {
        if (!mShowFooter && b) {
            // Show footer
            mShowFooter = true;
            notifyItemInserted(mDataSet.size());
        } else if (mShowFooter && !b){
            // Remove footer
            mShowFooter = false;
            notifyItemRemoved(mDataSet.size());
        }
    }

    public void updateData(Object[] data) {
        int posStart = getItemCount();
        int itemCount = data.length;
        for (Object p: data) {
            mDataSet.add(new PostsWrapper(p));
        }
        notifyItemRangeInserted(posStart, itemCount);
    }

    /**
     * Remove a topic from the dataset
     * @param topicId ID of the topic to remove
     */
    public void removeItem(String topicId) {
        for (int i=0;i<mDataSet.size();i++) {
            PostsWrapper post = mDataSet.get(i);
            if (topicId.equals(post.getTopicId())) {
                mDataSet.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    public ArrayList<PostsWrapper> getData() {
        return mDataSet;
    }

    public void clear() {
        int itemCount = getItemCount();
        mDataSet.clear();
        notifyItemRangeRemoved(0, itemCount);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        if (viewType == TYPE_ITEM) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_post, parent, false);
            return new VHItem(v);
        } else if (viewType == TYPE_FOOTER) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.loading, parent, false);
            return new VHFooter(v);
        }

        throw new RuntimeException("Unknown View Type: " + viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof VHItem) {
            VHItem vhItem = (VHItem) holder;

            PostsWrapper post = mDataSet.get(position);

            vhItem.bind(post, mCallback);

            // Set username
            String username = post.getAuthorName();
            vhItem.mUsername.setText(username);

            // Set board name
            String boardName = post.getBoardName();
            vhItem.mBoardName.setText(boardName);

            // Set topic title
            String topicTitle = post.getTopicTitle();
            vhItem.mTopicTitle.setText(topicTitle);

            // Set post content
            String postContent = post.getPostContent();
            // Strip out spoilers
            postContent = postContent.replaceAll("\\[spoiler\\].*?\\[/spoiler\\]", "hidden");
            vhItem.mPostContent.setText(postContent);

            // Set avatar
            String avatar = post.getAvatar();
            if (avatar.length() > 0) {
                Picasso.with(mContext).load(avatar).placeholder(R.drawable.no_avatar).fit().into(vhItem.mAvatar);
            } else {
                vhItem.mAvatar.setImageResource(R.drawable.no_avatar);
            }

            // Set post time
            Date then = post.getPostTime();
            long now = System.currentTimeMillis();
            String postTime = DateUtils.getRelativeTimeSpanString(then.getTime(), now, DateUtils.MINUTE_IN_MILLIS).toString();
            vhItem.mDate.setText(postTime);
        } else if (holder instanceof VHFooter) {
            VHFooter vhFooter = (VHFooter) holder;
        }
    }

    @Override
    public int getItemCount() {
        if (mShowFooter) {
            return mDataSet.size() + 1;
        }
        return mDataSet.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == mDataSet.size()) {
            return TYPE_FOOTER;
        }
        return TYPE_ITEM;
    }
}
