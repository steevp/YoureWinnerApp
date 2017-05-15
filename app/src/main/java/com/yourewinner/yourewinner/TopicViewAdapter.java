package com.yourewinner.yourewinner;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeThumbnailLoader;
import com.google.android.youtube.player.YouTubeThumbnailView;
import com.squareup.picasso.Picasso;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class TopicViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final static int TYPE_ITEM = 0;
    private final static int TYPE_PAGE_LINKS = 1;

    public interface ItemCheckedStateListener {
        public void onItemCheckedStateChanged();
    }

    private final static SparseIntArray RATINGS = new SparseIntArray() {
        {
            append(23, R.drawable.rating_trophy);
            append(22, R.drawable.rating_thumbup);
            append(21, R.drawable.rating_thumbdown);
            append(25, R.drawable.rating_turd);
            append(26, R.drawable.rating_gay);
            append(20, R.drawable.rating_heart);
            append(19, R.drawable.rating_smug);
            append(24, R.drawable.rating_cheese);
            append(101, R.drawable.rating_aggro);
            append(18, R.drawable.rating_twisted);
            append(17, R.drawable.rating_zigsd);
            append(16, R.drawable.rating_old);
            append(102, R.drawable.rating_baby);
            append(104, R.drawable.rating_weed);
            append(103, R.drawable.rating_meta);
            append(111, R.drawable.rating_ballin);
            append(116, R.drawable.rating_beer);
            append(117, R.drawable.rating_america);
            append(118, R.drawable.rating_hipster);
            append(119, R.drawable.rating_scroogled);
            append(120, R.drawable.rating_mistletoe);
        }
    };

    private Context mContext;
    private List<Object> mDataSet;
    private ItemCheckedStateListener mCallback;
    private int mPage;
    private Pattern mImgPattern = Pattern.compile("\\[img\\](.+)\\[/img\\]");
    private Pattern mYtPattern = Pattern.compile("\\[yt\\]([\\w\\-]{11})\\[/yt\\]");

    // Keep a reference to some dynamically added Views so we can reuse them
    private List<LinearLayout> mBlockQuotePool = new LinkedList<>();
    private List<VHSpoiler> mSpoilerPool = new LinkedList<>();

    // Keep track of scaled image sizes so we can restore them later
    private SparseArray<List<LinearLayout.LayoutParams>> mImageSizes = new SparseArray<>();
    // Keep track of youtube loaders to release them
    private SparseArray<List<VHYoutube>> mYtThumbnails = new SparseArray<>();

    // Checked states
    private SparseBooleanArray mCheckStates = new SparseBooleanArray();
    private SparseArray<View> mCheckedViews = new SparseArray<>();

    public class VHItem extends RecyclerView.ViewHolder {
        private TextView mUsername;
        private ImageView mImageUsername;
        private CircleImageView mAvatar;
        private ImageView mOnline;
        private LinearLayout mPostContent;
        private TextView mDate;
        private LinearLayout mRatingBar;
        private List<LinearLayout> mBlockQuotes = new ArrayList<>();
        private List<VHSpoiler> mSpoilers = new ArrayList<>();

        // Reference to current Blockquote/Spoiler
        private LinearLayout mBlockQuoteContainer;
        private VHSpoiler mSpoilerContainer;

        public VHItem(final View itemView) {
            super(itemView);
            mUsername = (TextView) itemView.findViewById(R.id.username);
            mImageUsername = (ImageView) itemView.findViewById(R.id.image_username);
            mAvatar = (CircleImageView) itemView.findViewById(R.id.avatar);
            mOnline = (ImageView) itemView.findViewById(R.id.online_now);
            mPostContent = (LinearLayout) itemView.findViewById(R.id.post_content);
            mDate = (TextView) itemView.findViewById(R.id.post_time);
            mRatingBar = (LinearLayout) itemView.findViewById(R.id.rating_bar);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Toggle checked state
                    boolean checked = !mCheckStates.get(getAdapterPosition(), false);
                    setChecked(getAdapterPosition(), itemView, checked);
                    mCallback.onItemCheckedStateChanged();
                }
            });

            // Open profile when avatar clicked
            mAvatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String username = mUsername.getText().toString();
                    Intent intent = new Intent(mContext, ProfileViewActivity.class);
                    intent.putExtra("username", username);
                    mContext.startActivity(intent);
                }
            });
        }

        private void addView(View v) {
            // Detach from parent first
            ViewGroup parent = (ViewGroup) v.getParent();
            if (parent != null) {
                parent.removeView(v);
            }
            if (mSpoilerContainer != null) {
                mSpoilerContainer.addView(v);
            } else if (mBlockQuoteContainer != null) {
                mBlockQuoteContainer.addView(v);
            } else  {
                mPostContent.addView(v);
            }
        }

        public void bind(Map<String,Object> post) {
            // Highlight item if checked
            boolean checked = mCheckStates.get(getAdapterPosition(), false);
            setChecked(getAdapterPosition(), itemView, checked);

            recycleViews();
            final DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();

            // Set username
            String username = new String((byte[]) post.get("post_author_name"), Charset.forName("UTF-8"));
            mUsername.setText(username);

            // Set image username if available
            final String imageUsername = (String) post.get("image_username");
            if (imageUsername.length() > 0) {
                // Hide the text image username
                mUsername.setVisibility(View.GONE);
                mImageUsername.setVisibility(View.VISIBLE);
                Glide.with(mContext).load(imageUsername).into(mImageUsername);
            } else {
                // Text username only
                mImageUsername.setVisibility(View.GONE);
                mUsername.setVisibility(View.VISIBLE);
            }

            // Load avatar
            String avatar = (String) post.get("icon_url");
            if (avatar.length() > 0) {
                Picasso.with(mContext)
                        .load(avatar)
                        .placeholder(R.drawable.no_avatar)
                        .fit()
                        .into(mAvatar);
            } else {
                mAvatar.setImageResource(R.drawable.no_avatar);
            }

            // Show online status
            boolean loggedIn = (boolean) post.get("is_online");
            mOnline.setVisibility(loggedIn ? View.VISIBLE : View.GONE);

            // Set post time
            Date then = (Date) post.get("post_time");
            long now = System.currentTimeMillis();
            String postTime = DateUtils.getRelativeTimeSpanString(then.getTime(), now, DateUtils.MINUTE_IN_MILLIS).toString();
            mDate.setText(postTime);

            // Add post ratings
            mRatingBar.removeAllViews();
            Object[] ratings = (Object[]) post.get("ratings");
            if (ratings != null) {
                for (Object r : ratings) {
                    @SuppressWarnings("unchecked")
                    Map<String,Object> rating = (Map<String,Object>) r;

                    int ratingCount = (int) rating.get("count");
                    int ratingId = (int) rating.get("rate_id");
                    int ratingImageResource = RATINGS.get(ratingId, -1);

                    if (ratingImageResource == -1) {
                        // Invalid rating
                        continue;
                    }

                    TextView countView = new TextView(mContext);
                    countView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                    countView.setText(ratingCount + "x");
                    ImageView ratingView = new ImageView(mContext);
                    ratingView.setScaleType(ImageView.ScaleType.FIT_XY);
                    // Make the rating 16dp
                    int pixels = (int) (16 * metrics.density + 0.5f);
                    LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(pixels, pixels);
                    ratingView.setLayoutParams(rlp);
                    ratingView.setImageResource(ratingImageResource);
                    mRatingBar.addView(countView);
                    mRatingBar.addView(ratingView);
                }
            }

            // Set post content
            String postContent = new String((byte[]) post.get("post_content"), Charset.forName("UTF-8"));

            mBlockQuoteContainer = null;
            mSpoilerContainer = null;

            // Number of nested Quotes/Spoilers
            int numQuotes = 0;
            int numSpoilers = 0;

            // position of img in post
            int imgPos = 0;
            int ytPos = 0;

            for (String content : BBCodeConverter.split(postContent)) {
                content = content.trim();
                Matcher imgMatcher = mImgPattern.matcher(content);
                Matcher ytMatcher = mYtPattern.matcher(content);

                if (content.startsWith("[quote]")) {
                    // Start of a quote
                    if (mBlockQuoteContainer == null) {
                        mBlockQuoteContainer = getBlockQuote();
                        mBlockQuotes.add(mBlockQuoteContainer);
                    }
                    numQuotes++;
                } else if (content.startsWith("[spoiler]")) {
                    // Start of a spoiler
                    if (mSpoilerContainer == null) {
                        mSpoilerContainer = getSpoiler();
                        mSpoilers.add(mSpoilerContainer);
                        mSpoilerContainer.hideSpoiler();
                    }
                    numSpoilers++;
                }

                // Also the end?
                boolean endQuote = content.endsWith("[/quote]");
                boolean endSpoiler = content.endsWith("[/spoiler]");

                // Done with these
                content = content.replaceAll("\\[/?(quote|spoiler)\\]", "").trim();

                if (imgMatcher.find()) {
                    final String imgUrl = imgMatcher.group(1);
                    final ImageView imgView = getImageView();

                    // Restore layout params
                    final List<LinearLayout.LayoutParams> imageSizes = mImageSizes.get(getAdapterPosition());
                    if (imageSizes != null) {
                        LinearLayout.LayoutParams params = null;
                        if (imgPos < imageSizes.size()) {
                            params = imageSizes.get(imgPos);
                        }
                        imgPos++;
                        if (params != null) {
                            imgView.setLayoutParams(params);
                        }
                    }


                    imgView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(mContext, ViewPhotoActivity.class);
                            intent.putExtra("imageURL", imgUrl);
                            mContext.startActivity(intent);
                        }
                    });
                    Glide.with(mContext)
                            .load(imgUrl)
                            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                            .into(new GlideDrawableImageViewTarget(imgView) {
                                @Override
                                public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> animation) {
                                    int originalWidthScaled = (int) (resource.getIntrinsicWidth() * metrics.density + 0.5f);
                                    int originalHeightScaled = (int) (resource.getIntrinsicHeight() * metrics.density + 0.5f);
                                    int maxWidth = (int) (metrics.widthPixels - 48 * metrics.density + 0.5f);
                                    int width, height;

                                    if (originalWidthScaled > maxWidth) {
                                        width = maxWidth;
                                        height = originalHeightScaled * maxWidth / originalWidthScaled;
                                    } else {
                                        width = originalWidthScaled;
                                        height = originalHeightScaled;
                                    }

                                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) imgView.getLayoutParams();
                                    params.height = height;
                                    params.width = width;

                                    List<LinearLayout.LayoutParams> sizes = mImageSizes.get(getAdapterPosition(), new ArrayList<LinearLayout.LayoutParams>());
                                    sizes.add(params);
                                    mImageSizes.put(getAdapterPosition(), sizes);

                                    super.onResourceReady(resource, animation);
                                }
                            });
                    addView(imgView);
                } else  if (ytMatcher.find()) {
                    final String videoId = ytMatcher.group(1);
                    final List<VHYoutube> thumbnails = mYtThumbnails.get(getAdapterPosition(),
                            new ArrayList<VHYoutube>());
                    VHYoutube thumbnail = null;
                    if (ytPos < thumbnails.size()) {
                        thumbnail = thumbnails.get(ytPos);
                    }
                    if (thumbnail == null) {
                        thumbnail = getYoutubeThumbnail();
                        thumbnail.itemView.setTag(videoId);
                        thumbnail.initialize();
                        thumbnails.add(thumbnail);
                        mYtThumbnails.put(getAdapterPosition(), thumbnails);
                    } else {
                        YouTubeThumbnailLoader loader = thumbnail.getLoader();
                        if (loader != null) {
                            loader.setVideo(videoId);
                        }
                    }
                    ytPos++;
                    addView(thumbnail.itemView);
                } else {
                    final LinkifyTextView textView = getTextView();
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        textView.setText(Html.fromHtml(BBCodeConverter.process(content), Html.FROM_HTML_MODE_LEGACY, new EmoteImageGetter(mContext), null));
                    } else {
                        //noinspection deprecation
                        textView.setText(Html.fromHtml(BBCodeConverter.process(content), new EmoteImageGetter(mContext), null));
                    }
                    textView.setMovementMethod(LinkMovementMethod.getInstance());
                    addView(textView);
                }

                if (endQuote) {
                    numQuotes--;
                    if (numQuotes <= 0) {
                        // Done with this container, add it to the post content
                        if (mSpoilerContainer != null) {
                            mSpoilerContainer.addView(mBlockQuoteContainer);
                        } else {
                            mPostContent.addView(mBlockQuoteContainer);
                        }
                        mBlockQuoteContainer = null;
                    }
                } else if (endSpoiler) {
                    numSpoilers--;
                    if (numSpoilers <= 0) {
                        // Done with this container, add it to the post content
                        if (mBlockQuoteContainer != null) {
                            mBlockQuoteContainer.addView(mSpoilerContainer.itemView);
                        } else {
                            mPostContent.addView(mSpoilerContainer.itemView);
                        }
                        mSpoilerContainer = null;
                    }
                }
            }
        }

        private ImageView getImageView() {
            // Create an ImageView
            ImageView iv = new ImageView(mContext);
            return iv;
        }

        private LinearLayout getBlockQuote() {
            if (mBlockQuotePool.isEmpty()) {
                return (LinearLayout) LayoutInflater.from(mPostContent.getContext())
                        .inflate(R.layout.blockquote, mPostContent, false);
            }
            return mBlockQuotePool.remove(0);
        }

        private VHSpoiler getSpoiler() {
            if (mSpoilerPool.isEmpty()) {
                View v = LayoutInflater.from(mPostContent.getContext())
                        .inflate(R.layout.spoiler, mPostContent, false);
                return new VHSpoiler(v);
            }
            return mSpoilerPool.remove(0);
        }

        private LinkifyTextView getTextView() {
            // Can't recycle here since TextViews don't recompute size when content changes
            LinkifyTextView tv = new LinkifyTextView(mContext);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            tv.setLayoutParams(lp);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            return tv;
        }

        private VHYoutube getYoutubeThumbnail() {
            View v = LayoutInflater.from(mPostContent.getContext())
                    .inflate(R.layout.youtube_thumbnail, mPostContent, false);
            return new VHYoutube(v);
        }

        public void recycleViews() {
            for (LinearLayout quote : mBlockQuotes) {
                quote.removeAllViews();
            }
            for (VHSpoiler spoiler : mSpoilers) {
                spoiler.removeAllViews();
            }
            mBlockQuotePool.addAll(mBlockQuotes);
            mSpoilerPool.addAll(mSpoilers);
            mBlockQuotes.clear();
            mSpoilers.clear();
            mPostContent.removeAllViews();
        }
    }

    public static class VHPageLinks extends RecyclerView.ViewHolder {
        private TextView mPage;

        public VHPageLinks(View itemView) {
            super(itemView);
            mPage = (TextView) itemView.findViewById(R.id.curpage);
        }

        public void setText(String text) {
            mPage.setText(text);
        }
    }

    public static class VHSpoiler extends RecyclerView.ViewHolder {
        private TextView mSpoilerReveal;
        private LinearLayout mSpoilerContent;

        public VHSpoiler(View itemView) {
            super(itemView);
            mSpoilerReveal = (TextView) itemView.findViewById(R.id.spoiler_reveal);
            mSpoilerContent = (LinearLayout) itemView.findViewById(R.id.spoiler_content);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    revealSpoiler();
                }
            });
        }

        private void revealSpoiler() {
            mSpoilerReveal.setVisibility(View.GONE);
            mSpoilerContent.setVisibility(View.VISIBLE);
        }

        public void hideSpoiler() {
            mSpoilerReveal.setVisibility(View.VISIBLE);
            mSpoilerContent.setVisibility(View.GONE);
        }

        public void addView(View v) {
            // Detach from parent first
            mSpoilerContent.addView(v);
        }

        public void removeAllViews() {
            mSpoilerContent.removeAllViews();
        }
    }

    public static class VHYoutube extends RecyclerView.ViewHolder {
        private YouTubeThumbnailView mThumbnail;
        private YouTubeThumbnailLoader mLoader;
        private ImageView mPlayIcon;

        public VHYoutube(View itemView) {
            super(itemView);
            mThumbnail = (YouTubeThumbnailView) itemView.findViewById(R.id.youtube_thumbnail);
            mPlayIcon = (ImageView) itemView.findViewById(R.id.play_icon);
        }

        public YouTubeThumbnailLoader getLoader() {
            return mLoader;
        }

        public void initialize() {
            mThumbnail.initialize(Secrets.YOUTUBE_API_KEY, new YouTubeThumbnailView.OnInitializedListener() {
                @Override
                public void onInitializationSuccess(YouTubeThumbnailView youTubeThumbnailView, YouTubeThumbnailLoader youTubeThumbnailLoader) {
                    mLoader = youTubeThumbnailLoader;
                    youTubeThumbnailLoader.setVideo(itemView.getTag().toString());
                    mPlayIcon.setVisibility(View.VISIBLE);
                }

                @Override
                public void onInitializationFailure(YouTubeThumbnailView youTubeThumbnailView, YouTubeInitializationResult youTubeInitializationResult) {

                }
            });
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(itemView.getContext(), YouTubePlayerActivity.class);
                    intent.putExtra(YouTubePlayerActivity.ARG_VIDEO_ID, itemView.getTag().toString());
                    itemView.getContext().startActivity(intent);
                }
            });
        }

        public void releaseLoader() {
            if (mLoader != null) {
                mLoader.release();
            }
        }
    }

    public TopicViewAdapter(Context context, int page, ItemCheckedStateListener listener) {
        mContext = context;
        mDataSet = new ArrayList<>();
        mPage = page;
        mCallback = listener;
    }

    public void updateData(Object[] data) {
        mDataSet = new ArrayList<>(Arrays.asList(data));
        notifyDataSetChanged();
        //notifyItemRangeInserted(1, data.length);

        // Add usernames to the @mention list
        for (Object p : mDataSet) {
            @SuppressWarnings("unchecked")
            Map<String, Object> post = (Map<String, Object>) p;
            String username = new String((byte[]) post.get("post_author_name"), Charset.forName("UTF-8"));
            Mentions.getInstance().addMention(username);
        }
    }

    public Object[] getData() {
        return mDataSet.toArray();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        if (viewType == TYPE_PAGE_LINKS) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.pagelinks, parent, false);
            return new VHPageLinks(v);
        } else if (viewType == TYPE_ITEM) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_topic_view, parent, false);
            return new VHItem(v);
        }

        throw new RuntimeException("Unknown View Type: " + viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof VHItem) {
            VHItem item = (VHItem) holder;
            @SuppressWarnings("unchecked")
            Map<String, Object> post = (Map<String, Object>) mDataSet.get(position - 1);
            item.bind(post);
        } else if (holder instanceof VHPageLinks) {
            VHPageLinks pageLinks = (VHPageLinks) holder;
            pageLinks.setText("Page " + mPage);
        }
    }

    @Override
    public int getItemCount() {
        return mDataSet.size() + 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 || position == getItemCount() - 1) {
            return TYPE_PAGE_LINKS;
        }
        return TYPE_ITEM;
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof VHItem) {
            VHItem item = (VHItem) holder;
            item.recycleViews();
        }
    }

    public void releaseLoaders() {
        for (int i=0, size=mYtThumbnails.size(); i<size; i++) {
            int key = mYtThumbnails.keyAt(i);
            List<VHYoutube> thumbnails = mYtThumbnails.get(key);
            for (VHYoutube yt: thumbnails) {
                yt.releaseLoader();
            }
        }
        mYtThumbnails.clear();
    }

    /**
     * Sets an item's checked state
     * @param position Position of item
     * @param itemView The item's View
     * @param checked The item's checked state
     */
    private void setChecked(int position, View itemView, boolean checked) {
        mCheckStates.put(position, checked);
        itemView.setActivated(checked);
        if (checked) {
            mCheckedViews.put(position, itemView);
        } else {
            mCheckedViews.remove(position);
        }
    }

    /**
     * Clear all item's checked states
     */
    public void clearCheckedItems() {
        mCheckStates.clear();
        for (int i=0, size=mCheckedViews.size();i<size;i++) {
            int key = mCheckedViews.keyAt(i);
            View item = mCheckedViews.get(key);
            item.setActivated(false);
        }
        mCheckedViews.clear();
    }

    /**
     * Get all checked item positions
     * @return SparseBooleanArray An array of checked states
     */
    public SparseBooleanArray getCheckedItemPositions() {
        return mCheckStates;
    }

    public int getCheckedItemCount() {
        int count = 0;
        for (int i=0, size=mCheckStates.size();i<size;i++) {
            int key = mCheckStates.keyAt(i);
            if (mCheckStates.get(key)) {
                count++;
            }
        }
        return count;
    }

    public Map<String,Object> getItem(int position) {
        //noinspection unchecked
        return (Map<String,Object>) mDataSet.get(position);
    }

    public void removeItem(int position) {
        mDataSet.remove(position - 1);
        notifyItemRemoved(position);
    }
}
