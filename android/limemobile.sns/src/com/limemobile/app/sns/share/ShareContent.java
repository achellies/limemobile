package com.limemobile.app.sns.share;

import android.graphics.Bitmap;
import android.text.TextUtils;

public class ShareContent {
    final String title;
    final String description;
    final String comment;
    final String tweet;
    final Bitmap image;
    final Bitmap thumbImage;
    final String imageUrl;
    final String videoUrl;
    final String audioUrl;
    final String webUrl;

    public ShareContent(final Builder builder) {
        title = builder.title;
        description = builder.description;
        comment = builder.comment;
        tweet = builder.tweet;
        image = builder.image;
        thumbImage = builder.thumbImage;
        imageUrl = builder.imageUrl;
        videoUrl = builder.videoUrl;
        audioUrl = builder.audioUrl;
        webUrl = builder.webUrl;
    }
    
    public static class Builder {
        public static final int SNS_SHARE_THUMB_SIZE = 100;
        public static final int TITLE_MAX_LEN = 30;
        public static final int DESCRIPTION_MAX_LEN = 80;
        public static final int COMMENT_MAX_LEN = 40;
        public static final int TWEET_MAX_LEN = 140;

        private String title;
        private String description;
        private String comment;
        private String tweet;
        private Bitmap image;
        private Bitmap thumbImage;
        private String imageUrl;
        private String videoUrl;
        private String audioUrl;
        private String webUrl;
        public Builder() {
        }

        public Builder setTitle(String title) {
            if (TextUtils.isEmpty(title)) {
                this.title = null;
            } else {
                if (title.length() > TITLE_MAX_LEN) {
                    this.title = title.substring(0, TITLE_MAX_LEN);
                } else {
                    this.title = title;
                }
            }
            return this;
        }

        public Builder setDescription(String description) {
            if (TextUtils.isEmpty(description)) {
                this.description = null;
            } else {
                if (description.length() > DESCRIPTION_MAX_LEN) {
                    this.description = description.substring(0,
                            DESCRIPTION_MAX_LEN);
                } else {
                    this.description = description;
                }
            }
            return this;
        }

        public Builder setComment(String comment) {
            if (TextUtils.isEmpty(comment)) {
                this.comment = null;
            } else {
                if (comment.length() > COMMENT_MAX_LEN) {
                    this.comment = comment.substring(0, COMMENT_MAX_LEN);
                } else {
                    this.comment = comment;
                }
            }
            return this;
        }

        public Builder setTweet(String tweet) {
            if (TextUtils.isEmpty(tweet)) {
                this.tweet = null;
            } else {
                this.tweet = tweet;
            }
            return this;
        }

        public Builder setBitmap(Bitmap bitmap) {
            if(bitmap != null && !bitmap.isRecycled() && bitmap.getWidth() > 0) {
                this.image = bitmap;
                this.thumbImage = Bitmap.createScaledBitmap(this.image, SNS_SHARE_THUMB_SIZE, SNS_SHARE_THUMB_SIZE, true);
            } else {
                this.image = null;
                this.thumbImage = null;
            }
            return this;
        }

        public Builder setWebUrl(String url) {
            if(TextUtils.isEmpty(url) || (!url.startsWith("http://") && !url.startsWith("https://"))) {
                this.webUrl = null;
            } else {
                this.webUrl = url;
            }
            return this;
        }

        public Builder setImageUrl(String url) {
            if(TextUtils.isEmpty(url) || (!url.startsWith("http://") && !url.startsWith("https://"))) {
                this.imageUrl = null;
            } else {
                this.imageUrl = url;
            }
            return this;
        }

        public Builder setAudioUrl(String url) {
            if(TextUtils.isEmpty(url) || (!url.startsWith("http://") && !url.startsWith("https://"))) {
                this.audioUrl = null;
            } else {
                this.audioUrl = url;
            }
            return this;
        }

        public Builder setVideoUrl(String url) {
            if(TextUtils.isEmpty(url) || (!url.startsWith("http://") && !url.startsWith("https://"))) {
                this.videoUrl = null;
            } else {
                this.videoUrl = url;
            }
            return this;
        }

        public ShareContent build() {
            initEmptyFiledsWithDefaultValues();
            return new ShareContent(this);
        }

        private void initEmptyFiledsWithDefaultValues() {

        }
    }
}
