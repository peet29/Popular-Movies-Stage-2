package me.hanthong.android.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import me.hanthong.android.popularmovies.data.MovieContract.MovieEntry;

/**
 * Created by peet29 on 5/1/2559.
 */
public class MovieDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 3;

    static final String DATABASE_NAME = "movieData.db";

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE "
                + MovieEntry.TABLE_NAME + " ("
                + MovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + MovieEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL,"
                + MovieEntry.COLUMN_MOVIE_POSTER + " TEXT NOT NULL,"
                + MovieEntry.COLUMN_MOVIE_NAME + " TEXT NOT NULL,"
                + MovieEntry.COLUMN_MOVIE_OVERVIEW + " TEXT NOT NULL,"
                + MovieEntry.COLUMN_MOVIE_RELEASE_DATE + " TEXT NOT NULL,"
                + MovieEntry.COLUMN_MOVIE_POPULARITY + " REAL NOT NULL,"
                + MovieEntry.COLUMN_MOVIE_VOTE + " REAL NOT NULL,"
                + MovieEntry.COLUMN_MOVIE_TRAILER + " TEXT,"
                + MovieEntry.COLUMN_MOVIE_REVIEW + " TEXT,"
                + MovieEntry.COLUMN_MOVIE_FAVORITES + " INTEGER DEFAULT 0);";
        db.execSQL(SQL_CREATE_MOVIE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME);
        onCreate(db);
    }
}
