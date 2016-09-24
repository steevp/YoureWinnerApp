package com.yourewinner.yourewinner;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.res.ResourcesCompat;
import android.text.Html;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeThumbnailLoader;
import com.google.android.youtube.player.YouTubeThumbnailView;
import com.squareup.picasso.Picasso;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class TopicViewAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mInflater;
    private Object[] mPosts;
    private SparseArray mImageSizes;

    public TopicViewAdapter(Context context, LayoutInflater inflater) {
        mContext = context;
        mInflater = inflater;
        mPosts = new Object[0];
        mImageSizes = new SparseArray(0);
    }

    public void updateData(Object[] data) {
        mPosts = data;
        // Add usernames to @mentions
        for (final Object post: mPosts) {
            final Map<String,Object> p = (Map<String,Object>) post;
            final String username = new String((byte[]) p.get("post_author_name"), StandardCharsets.UTF_8);
            Mentions.getInstance().addMention(username);
        }
        mImageSizes.clear();
        notifyDataSetChanged();
    }

    public Object[] getData() {
        return mPosts;
    }

    @Override
    public int getCount() {
        return mPosts.length;
    }

    @Override
    public Object getItem(int position) {
        int totalCount = getCount();
        if (totalCount == 0 || position > totalCount) {
            return null;
        }

        return mPosts[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        final DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.row_topic_view, null);
            holder = new ViewHolder();
            holder.usernameTextView = (TextView) convertView.findViewById(R.id.username);
            holder.usernameImageView = (ImageView) convertView.findViewById(R.id.image_username);
            holder.avatarImageView = (CircleImageView) convertView.findViewById(R.id.avatar);
            holder.postContentTextView = (LinearLayout) convertView.findViewById(R.id.post_content);
            holder.postTimeTextView = (TextView) convertView.findViewById(R.id.post_time);
            holder.ratingBar = (LinearLayout) convertView.findViewById(R.id.rating_bar);

            holder.avatarImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //String username = (String) holder.avatarImageView.getTag();
                    String username = holder.usernameTextView.getText().toString();
                    //Toast.makeText(mContext, username + " clicked!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(mContext, ProfileViewActivity.class);
                    intent.putExtra("username", username);
                    mContext.startActivity(intent);
                }
            });

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Map<String,Object> post = (Map<String,Object>) getItem(position);

        holder.ratingBar.removeAllViews();

        Object[] ratings = (Object[]) post.get("ratings");
        if (ratings != null && ratings.length > 0) {

            Map<String,Object> rating;
            int count;
            int ratingId;

            TextView ratingCount;
            ImageView ratingImage;

            final int pixels = (int) (16 * metrics.density + 0.5f);
            final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(pixels, pixels);

            for (int i=0;i<ratings.length;i++) {
                rating = (Map<String,Object>) ratings[i];
                count = (int) rating.get("count");
                ratingId = (int) rating.get("rate_id");

                ratingCount = new TextView(mContext);
                ratingImage = new ImageView(mContext);
                ratingImage.setLayoutParams(layoutParams);

                switch(ratingId) {
                    case 23:
                        ratingImage.setImageResource(R.drawable.rating_trophy);
                        break;
                    case 22:
                        ratingImage.setImageResource(R.drawable.rating_thumbup);
                        break;
                    case 21:
                        ratingImage.setImageResource(R.drawable.rating_thumbdown);
                        break;
                    case 25:
                        ratingImage.setImageResource(R.drawable.rating_turd);
                        break;
                    case 26:
                        ratingImage.setImageResource(R.drawable.rating_gay);
                        break;
                    case 20:
                        ratingImage.setImageResource(R.drawable.rating_heart);
                        break;
                    case 19:
                        ratingImage.setImageResource(R.drawable.rating_smug);
                        break;
                    case 24:
                        ratingImage.setImageResource(R.drawable.rating_cheese);
                        break;
                    case 101:
                        ratingImage.setImageResource(R.drawable.rating_aggro);
                        break;
                    case 18:
                        ratingImage.setImageResource(R.drawable.rating_twisted);
                        break;
                    case 17:
                        ratingImage.setImageResource(R.drawable.rating_zigsd);
                        break;
                    case 16:
                        ratingImage.setImageResource(R.drawable.rating_old);
                        break;
                    case 102:
                        ratingImage.setImageResource(R.drawable.rating_baby);
                        break;
                    case 104:
                        ratingImage.setImageResource(R.drawable.rating_weed);
                        break;
                    case 103:
                        ratingImage.setImageResource(R.drawable.rating_meta);
                        break;
                    case 111:
                        ratingImage.setImageResource(R.drawable.rating_ballin);
                        break;
                    case 116:
                        ratingImage.setImageResource(R.drawable.rating_coorslight);
                        break;
                    case 117:
                        ratingImage.setImageResource(R.drawable.rating_america);
                        break;
                    case 118:
                        ratingImage.setImageResource(R.drawable.rating_hipster);
                        break;
                    case 119:
                        ratingImage.setImageResource(R.drawable.rating_scroogled);
                        break;
                    case 120:
                        ratingImage.setImageResource(R.drawable.rating_mistletoe);
                        break;
                    default:
                        break;
                }

                ratingCount.setText(count + "x");

                holder.ratingBar.addView(ratingCount);
                holder.ratingBar.addView(ratingImage);

            }

        }

        try {
            String username = new String((byte[]) post.get("post_author_name"), "UTF-8");
            String imageUsername = post.get("image_username").toString();
            if (imageUsername.length() > 0) {
                holder.usernameTextView.setVisibility(View.GONE);
                holder.usernameImageView.setVisibility(View.VISIBLE);
                Glide.with(mContext).load(imageUsername).into(holder.usernameImageView);
                holder.usernameTextView.setText(username);
            } else {
                holder.usernameTextView.setVisibility(View.VISIBLE);
                holder.usernameImageView.setVisibility(View.GONE);
                holder.usernameTextView.setText(username);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String postContent = new String((byte[]) post.get("post_content"), StandardCharsets.UTF_8);
        String[] postContentSplit = postContent.split("(?=\\[(img|quote|spoiler)\\])|(?<=\\[/(img|quote|spoiler)\\])");
        holder.postContentTextView.removeAllViews();
        Pattern imgPattern = Pattern.compile("\\[img\\](.+)\\[/img\\]");
        Pattern youtubePattern = Pattern.compile("\\[url=https?://(?:www\\.)?youtu(?:\\.be/|be\\.com/watch\\?v=)([\\w\\-]{11})\\].+\\[/url\\]");
        Matcher imgMatcher, youtubeMatcher;
        LinearLayout blockQuote = null;
        RelativeLayout spoiler = null;
        LinearLayout spoilerContent = null;
        int numQuotes = 0;
        int numSpoilers = 0;
        for (int i=0;i<postContentSplit.length;i++) {
            String content = postContentSplit[i].trim();
            imgMatcher = imgPattern.matcher(content);
            youtubeMatcher = youtubePattern.matcher(content);

            if (content.startsWith("[quote]")) {
                if (blockQuote == null) {
                    blockQuote = (LinearLayout) mInflater.inflate(R.layout.blockquote, null);
                }
                numQuotes++;
            } else if (content.startsWith("[spoiler]")) {
                if (spoiler == null) {
                    spoiler = (RelativeLayout) mInflater.inflate(R.layout.spoiler, null);
                    spoilerContent = (LinearLayout) spoiler.findViewById(R.id.spoiler_content);
                }
                numSpoilers++;
            }

            boolean endQuote = content.endsWith("[/quote]");
            boolean endSpoiler = content.endsWith("[/spoiler]");

            content = content.replace("[quote]", "")
                    .replace("[/quote]", "")
                    .replace("[spoiler]", "")
                    .replace("[/spoiler]", "")
                    .trim();

            if (imgMatcher.find()) {
                final int postID = position;
                final int imageID = i;

                final String imageURL = imgMatcher.group(1);
                final ImageView imageView = new ImageView(mContext);

                SparseArray imageSizes = (SparseArray) mImageSizes.get(postID);
                if (imageSizes != null) {
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) imageSizes.get(imageID);
                    if (params != null) {
                        imageView.setLayoutParams(params);
                    }
                }

                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, ViewPhotoActivity.class);
                        intent.putExtra("imageURL", imageURL);
                        mContext.startActivity(intent);
                    }
                });
                Glide.with(mContext).load(imageURL).into(new GlideDrawableImageViewTarget(imageView) {
                    @Override
                    public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> animation) {
                        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) imageView.getLayoutParams();
                        final int originalWidthScaled = (int) (resource.getIntrinsicWidth() * metrics.density + 0.5f);
                        final int originalHeightScaled = (int) (resource.getIntrinsicHeight() * metrics.density + 0.5f);
                        final int maxWidth = (int) (metrics.widthPixels - 48 * metrics.density + 0.5f);
                        int width, height;

                        if (originalWidthScaled > maxWidth) {
                            width = maxWidth;
                            height = originalHeightScaled * maxWidth / originalWidthScaled;
                        } else {
                            width = originalWidthScaled;
                            height = originalHeightScaled;
                        }

                        params.width = width;
                        params.height = height;
                        SparseArray imageSizes = (SparseArray) mImageSizes.get(postID, new SparseArray(0));
                        imageSizes.put(imageID, params);
                        mImageSizes.put(postID, imageSizes);
                        super.onResourceReady(resource, animation);
                    }
                });

                if (blockQuote != null) {
                    blockQuote.addView(imageView);
                } else if (spoilerContent != null) {
                    spoilerContent.addView(imageView);
                } else {
                    holder.postContentTextView.addView(imageView);
                }
            } else if (youtubeMatcher.find()) {
                final String videoID = youtubeMatcher.group(1);

                final RelativeLayout youtubeLayout = new RelativeLayout(mContext);
                int originalWidthScaled = (int) (560 * metrics.density + 0.5f);
                int originalHeightScaled = (int) (315 * metrics.density + 0.5f);
                int maxWidth = (int) (metrics.widthPixels - 48 * metrics.density + 0.5f);
                int width, height;
                if (originalWidthScaled > maxWidth) {
                    width = maxWidth;
                    height = originalHeightScaled * maxWidth / originalWidthScaled;
                } else {
                    width = originalWidthScaled;
                    height = originalHeightScaled;
                }
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width, height);
                youtubeLayout.setLayoutParams(layoutParams);

                YouTubeThumbnailView thumbnailView = new YouTubeThumbnailView(mContext);
                RelativeLayout.LayoutParams thumbnailParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                thumbnailView.setLayoutParams(thumbnailParams);
                thumbnailView.setBackgroundColor(ResourcesCompat.getColor(mContext.getResources(), R.color.grey, null));

                thumbnailView.setTag(videoID);
                thumbnailView.initialize(Secrets.YOUTUBE_API_KEY, new YouTubeThumbnailView.OnInitializedListener() {
                    @Override
                    public void onInitializationSuccess(YouTubeThumbnailView youTubeThumbnailView, YouTubeThumbnailLoader youTubeThumbnailLoader) {
                        youTubeThumbnailLoader.setVideo(youTubeThumbnailView.getTag().toString());
                        ImageView youtubePlayIcon = new ImageView(mContext);
                        int pixels = (int) (64 * metrics.density + 0.5f);
                        RelativeLayout.LayoutParams playIconParams = new RelativeLayout.LayoutParams(pixels, pixels);
                        playIconParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                        youtubePlayIcon.setLayoutParams(playIconParams);
                        youtubePlayIcon.setImageResource(R.drawable.youtube_play_icon);
                        youtubeLayout.addView(youTubeThumbnailView);
                        youtubeLayout.addView(youtubePlayIcon);
                    }

                    @Override
                    public void onInitializationFailure(YouTubeThumbnailView youTubeThumbnailView, YouTubeInitializationResult youTubeInitializationResult) {

                    }
                });
                thumbnailView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(mContext, YouTubePlayerActivity.class);
                        intent.putExtra(YouTubePlayerActivity.ARG_VIDEO_ID, view.getTag().toString());
                        mContext.startActivity(intent);
                    }
                });

                if (blockQuote != null) {
                    blockQuote.addView(youtubeLayout);
                } else if (spoilerContent != null) {
                    spoilerContent.addView(youtubeLayout);
                } else {
                    holder.postContentTextView.addView(youtubeLayout);
                }
            } else {
                LinkifyTextView textView = new LinkifyTextView(mContext);
                textView.setTextSize(18);
                textView.setText(Html.fromHtml(BBCodeConverter.process(content), new EmoteImageGetter(mContext), null));
                textView.setMovementMethod(LinkMovementMethod.getInstance());
                if (blockQuote != null) {
                    blockQuote.addView(textView);
                } else if (spoilerContent != null) {
                    spoilerContent.addView(textView);
                } else {
                    holder.postContentTextView.addView(textView);
                }
            }

            if (endQuote) {
                numQuotes--;
                if (numQuotes <= 0) {
                    holder.postContentTextView.addView(blockQuote);
                    blockQuote = null;
                }
            } else if (endSpoiler) {
                numSpoilers--;
                if (numSpoilers <= 0) {
                    holder.postContentTextView.addView(spoiler);
                    spoiler = null;
                    spoilerContent = null;
                }
            }
        }

        boolean loggedIn = (boolean) post.get("is_online");

        String avatar = (String) post.get("icon_url");
        if (avatar.length() > 0) {
            Picasso.with(mContext).load(avatar).placeholder(R.drawable.no_avatar).fit().into(holder.avatarImageView);
        } else {
            holder.avatarImageView.setImageResource(R.drawable.no_avatar);
        }

        Date then = (Date) post.get("post_time");
        Log.d("ywtag", then.toString() + " " + avatar);
        long now = System.currentTimeMillis();
        String postTime = DateUtils.getRelativeTimeSpanString(then.getTime(), now, DateUtils.MINUTE_IN_MILLIS).toString();

        holder.postTimeTextView.setText(postTime);

        return convertView;
    }

    private static class ViewHolder {
        public TextView usernameTextView;
        public ImageView usernameImageView;
        public CircleImageView avatarImageView;
        public LinearLayout postContentTextView;
        public TextView postTimeTextView;
        public LinearLayout ratingBar;
    }
}
