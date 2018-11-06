package com.apps.creativesource.censio;

import android.provider.BaseColumns;

public class CensioContract {

    private CensioContract() {}

    public static final class UserInfo implements BaseColumns {
        public static final String TABLE_NAME = "userInfo";
        public static final String COLUMN_USER_NAME = "userName";
        public static final String COLUMN_USER_LIKES_COUNT = "userLikesCount";
        public static final String COLUMN_USER_DISLIKES_COUNT = "userDislikesCount";
    }

    public static final class Posts implements BaseColumns {
        public static final String TABLE_NAME = "posts";
        public static final String COLUMN_POST_AUTHOR = "postAuthor";
        public static final String COLUMN_POST_TITLE = "postTitle";
        public static final String COLUMN_POST_TIMESTAMP = "timestamp";
        public static final String COLUMN_POST_BODY = "postBody";
        public static final String COLUMN_POST_INTERACTION_COUNT = "postInteractionCount";
        public static final String COLUMN_POST_COMMENTS = "postComments";
    }
}
