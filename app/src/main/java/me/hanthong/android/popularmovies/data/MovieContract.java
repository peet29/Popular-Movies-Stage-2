package me.hanthong.android.popularmovies.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by peet29 on 5/1/2559.
 */
public class MovieContract {

    public static final String CONTENT_AUTHORITY = "me.hanthong.android.popularmovies";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_MOVIE = "movie";
    public static final String PATH_REIEW = "review";
    public static final String PATH_TRAILERS = "trailers";
    public static final String PATH_FAVORITE = "favorite";

    public static final class MovieEntry implements BaseColumns{
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;

        public static final String TABLE_NAME = "moviedata";

        public static final String COLUMN_MOVIE_ID = "id";
        public static final String COLUMN_MOVIE_POSTER = "poster";
        public static final String COLUMN_MOVIE_NAME = "name";
        public static final String COLUMN_MOVIE_OVERVIEW = "overview";
        public static final String COLUMN_MOVIE_RELEASE_DATE= "release_date";
        public static final String COLUMN_MOVIE_POPULARITY= "popularity";
        public static final String COLUMN_MOVIE_VOTE = "vote";
        public static final String COLUMN_MOVIE_TRAILER = "trailer";
        public static final String COLUMN_MOVIE_REVIEW = "review";
        public static final String COLUMN_MOVIE_FAVORITES = "favorites";

        public static Uri buildMovieUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildMovieReviewUri(long id)
        {
            return CONTENT_URI.buildUpon().appendPath(Long.toString(id)).appendPath(PATH_REIEW).build();
        }
        public static Uri buildMovieFavoirteUri()
        {
            return CONTENT_URI.buildUpon().appendPath(PATH_FAVORITE).build();
        }

        public static Uri buildMovieFavoriteWithIDUri(long id)
        {
            return CONTENT_URI.buildUpon().appendPath(Long.toString(id)).appendPath((PATH_FAVORITE)).build();
        }

        public static Uri buildMovieTrailerswUri(long id)
        {
            return CONTENT_URI.buildUpon().appendPath(Long.toString(id)).appendPath(PATH_TRAILERS).build();
        }

        public static Long getMovieIdFromUri(Uri uri){
            return Long.parseLong(uri.getPathSegments().get(1));
        }






    }
}
