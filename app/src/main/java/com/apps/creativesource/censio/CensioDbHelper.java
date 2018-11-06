package com.apps.creativesource.censio;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class CensioDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "censio.db";
    private static final int DATABASE_VERSION = 1;

    public CensioDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE_USER_TABLE = "CREATE TABLE " +
                CensioContract.UserInfo.TABLE_NAME + " (" +
                CensioContract.UserInfo._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                CensioContract.UserInfo.COLUMN_USER_NAME + " TEXT NOT NULL," +
                CensioContract.UserInfo.COLUMN_USER_LIKES_COUNT + " INTEGER," +
                CensioContract.UserInfo.COLUMN_USER_DISLIKES_COUNT + " INTEGER" +
                ");";

        final String CREATE_POST_TABLE = "CREATE TABLE " +
                CensioContract.Posts.TABLE_NAME + " (" +
                CensioContract.Posts._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                CensioContract.Posts.COLUMN_POST_AUTHOR + " TEXT NOT NULL," +
                CensioContract.Posts.COLUMN_POST_TITLE + " TEXT NOT NULL," +
                CensioContract.Posts.COLUMN_POST_TIMESTAMP + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                CensioContract.Posts.COLUMN_POST_INTERACTION_COUNT + " INTEGER," +
                CensioContract.Posts.COLUMN_POST_BODY + " TEXT," +
                CensioContract.Posts.COLUMN_POST_COMMENTS + " TEXT" +
                ");";

        db.execSQL(CREATE_POST_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + CensioContract.Posts.TABLE_NAME);
        onCreate(db);
    }
}
