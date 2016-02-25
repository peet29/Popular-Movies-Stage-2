package me.hanthong.android.popularmovies.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * Created by peet29 on 1/2/2559.
 */
public class MovieProvider extends ContentProvider {
    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MovieDbHelper mOpenHelper;

    static final int MOVIE = 100;
    static final int MOVIE_FAVORITE = 102;
    static final int MOVIE_ID = 103;
    static final int MOVIE_ID_REVIEW = 104;
    static final int MOVIE_ID_TRAILER = 105;
    static final int MOVIE_ID_FAVORITE = 106;

    static UriMatcher buildUriMatcher() {
        // I know what you're thinking.  Why create a UriMatcher when you can use regular
        // expressions instead?  Because you're not crazy, that's why.

        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MovieContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, MovieContract.PATH_MOVIE, MOVIE);
        matcher.addURI(authority, MovieContract.PATH_MOVIE + "/"   + MovieContract.PATH_FAVORITE,MOVIE_FAVORITE);
        matcher.addURI(authority, MovieContract.PATH_MOVIE + "/#", MOVIE_ID);
        matcher.addURI(authority, MovieContract.PATH_MOVIE + "/#/" + MovieContract.PATH_REIEW, MOVIE_ID_REVIEW);
        matcher.addURI(authority, MovieContract.PATH_MOVIE + "/#/" + MovieContract.PATH_TRAILERS, MOVIE_ID_TRAILER);
        matcher.addURI(authority, MovieContract.PATH_MOVIE + "/#/" + MovieContract.PATH_FAVORITE, MOVIE_ID_FAVORITE);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new MovieDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case MOVIE: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MovieContract.MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case MOVIE_FAVORITE:{
                retCursor = getCursorByFavorites(mOpenHelper.getReadableDatabase(),projection);
                break;
            }
            case MOVIE_ID: {
                retCursor = getCursorByMovieId(mOpenHelper.getReadableDatabase(), MovieContract.MovieEntry.getMovieIdFromUri(uri));
                break;
            }
            case MOVIE_ID_REVIEW: {
                retCursor = getReviewCursorByMovieId(mOpenHelper.getReadableDatabase(), MovieContract.MovieEntry.getMovieIdFromUri(uri));
                break;
            }
            case MOVIE_ID_TRAILER: {
                retCursor = getTrailersCursorByMovieId(mOpenHelper.getReadableDatabase(), MovieContract.MovieEntry.getMovieIdFromUri(uri));
                break;
            }
            case MOVIE_ID_FAVORITE:{
                retCursor = getFavoritesCursorByMovieId(mOpenHelper.getReadableDatabase(), MovieContract.MovieEntry.getMovieIdFromUri(uri));
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {

        final int match = sUriMatcher.match(uri);

        switch (match) {
            case MOVIE:
                return MovieContract.MovieEntry.CONTENT_TYPE;
            case MOVIE_ID:
                return MovieContract.MovieEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }


    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;
        switch (match) {
            case MOVIE: {
                long _id = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = MovieContract.MovieEntry.buildMovieUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        if (null == selection) selection = "1";
        switch (match) {
            case MOVIE:
                rowsDeleted = db.delete(
                        MovieContract.MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;
        switch (match) {
            case MOVIE:
                rowsUpdated = db.update(MovieContract.MovieEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case MOVIE_ID_REVIEW:
                rowsUpdated = db.update(MovieContract.MovieEntry.TABLE_NAME,values,selection,selectionArgs);
                break;
            case MOVIE_ID_TRAILER:
                rowsUpdated = db.update(MovieContract.MovieEntry.TABLE_NAME,values,selection,selectionArgs);
                break;
            case MOVIE_ID_FAVORITE:
                rowsUpdated = db.update(MovieContract.MovieEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIE:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        //check data before insert if already have update it.
                        Long movieID = value.getAsLong(MovieContract.MovieEntry.COLUMN_MOVIE_ID);
                        Cursor cursor = getCursorByMovieId(db, movieID);
                        if (cursor != null && cursor.getCount() > 0) {
                            int columnIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_ID);
                            Long cursorMovieID = cursor.getLong(columnIndex);
                            if (cursorMovieID == movieID) {
                                db.update(MovieContract.MovieEntry.TABLE_NAME, value, null, null);
                            }
                        } else {
                            long _id = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, value);
                            if (_id != -1) {
                                returnCount++;
                            }
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    private Cursor getCursorByMovieId(SQLiteDatabase database, Long movieID) {

        Cursor cursor = database.query(MovieContract.MovieEntry.TABLE_NAME,
                null,
                MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ?",
                new String[]{String.valueOf(movieID)},
                null,
                null,
                null);
        cursor.moveToFirst();
        return cursor;

    }

    private Cursor getReviewCursorByMovieId(SQLiteDatabase database, Long movieID) {
        Cursor cursor = database.query(MovieContract.MovieEntry.TABLE_NAME,
                new String[]{MovieContract.MovieEntry.COLUMN_MOVIE_REVIEW},
                MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ?",
                new String[]{String.valueOf(movieID)},
                null,
                null,
                null);
        cursor.moveToFirst();
        return cursor;
    }

    private Cursor getTrailersCursorByMovieId(SQLiteDatabase database, Long movieID) {
        Cursor cursor = database.query(MovieContract.MovieEntry.TABLE_NAME,
                new String[]{MovieContract.MovieEntry.COLUMN_MOVIE_TRAILER},
                MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ?",
                new String[]{String.valueOf(movieID)},
                null,
                null,
                null);
        cursor.moveToFirst();
        return cursor;
    }

    private Cursor getFavoritesCursorByMovieId(SQLiteDatabase database, Long movieID) {
        Cursor cursor = database.query(MovieContract.MovieEntry.TABLE_NAME,
                new String[]{MovieContract.MovieEntry.COLUMN_MOVIE_FAVORITES},
                MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ?",
                new String[]{String.valueOf(movieID)},
                null,
                null,
                null);
        cursor.moveToFirst();
        return cursor;
    }

    private Cursor getCursorByFavorites(SQLiteDatabase database,String[] projection) {
        Cursor cursor = database.query(MovieContract.MovieEntry.TABLE_NAME,
                projection,
                MovieContract.MovieEntry.COLUMN_MOVIE_FAVORITES + " = ?",
                new String[]{String.valueOf(1)},
                null,
                null,
                null);
        cursor.moveToFirst();
        return cursor;
    }

    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }

}
