package me.hanthong.android.popularmovies;

import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.Bind;
import butterknife.ButterKnife;
import me.hanthong.android.popularmovies.data.MovieContract;

/**
 * Created by peet29 on 24/12/2558.
 */

public class DetailFragment extends Fragment implements View.OnClickListener, android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    static final String DETAIL_URI = "MovieData";
    private MovieData mMovie;
    private Uri mUri;
    private Cursor mMovieData;
    private CardView mCardViewTrailer;
    private CardView mCardViewReivew;
    private String mReview, mTrailer;
    private String[] mYoutubeKey = new String[3];
    RequestQueue mRequestQueue;

    private static final int DETAIL_LOADER = 0;

    private static final String[] DETAIL_COLUMNS = {

            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_MOVIE_ID,
            MovieContract.MovieEntry.COLUMN_MOVIE_POSTER,
            MovieContract.MovieEntry.COLUMN_MOVIE_NAME,
            MovieContract.MovieEntry.COLUMN_MOVIE_OVERVIEW,
            MovieContract.MovieEntry.COLUMN_MOVIE_RELEASE_DATE,
            MovieContract.MovieEntry.COLUMN_MOVIE_POPULARITY,
            MovieContract.MovieEntry.COLUMN_MOVIE_VOTE,
            MovieContract.MovieEntry.COLUMN_MOVIE_TRAILER,
            MovieContract.MovieEntry.COLUMN_MOVIE_REVIEW,
            MovieContract.MovieEntry.COLUMN_MOVIE_FAVORITES
    };

    static final int COL_ID = 0;
    static final int COL_MOVIE_ID = 1;
    static final int COL_MOVIE_POSTER = 2;
    static final int COL_MOVIE_NAME = 3;
    static final int COL_MOVIE_OVERVIEW = 4;
    static final int COL_MOVIE_RELEASE_DATE = 5;
    static final int COL_MOVIE_POPULARITY = 6;
    static final int COL_MOVIE_VOTE = 7;
    static final int COL_MOVIE_TRAILER = 8;
    static final int COL_MOVIE_REVIEW = 9;
    static final int COL_MOVIE_FAVORITES = 10;

    @Bind(R.id.text_movie_name)
    TextView movieName;
    @Bind(R.id.movie_release_date)
    TextView movieReleaseDate;
    @Bind(R.id.movie_rate)
    TextView movieVote;
    @Bind(R.id.movie_plot)
    TextView movieOverView;
    @Bind(R.id.image_movie_poster)
    ImageView poster;
    @Bind(R.id.movie_review)
    TextView movieReview;
    @Bind(R.id.movie_trailer_1)
    TextView trailerText_1;
    @Bind(R.id.movie_trailer_2)
    TextView trailerText_2;
    @Bind(R.id.movie_trailer_3)
    TextView trailerText_3;
    @Bind(R.id.movie_button_trailer_1)
    Button trailerButton_1;
    @Bind(R.id.movie_button_trailer_2)
    Button trailerButton_2;
    @Bind(R.id.movie_button_trailer_3)
    Button trailerButton_3;
    @Bind(R.id.movie_favorite)
    ImageButton favoriteButton;


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.movie_button_trailer_1:
                watchYoutubeVideo(mYoutubeKey[0]);
                break;
            case R.id.movie_button_trailer_2:
                watchYoutubeVideo(mYoutubeKey[1]);
                break;
            case R.id.movie_button_trailer_3:
                watchYoutubeVideo(mYoutubeKey[2]);
                break;
            case R.id.movie_favorite:
                updateFavoriteData();
                break;
        }
    }

    private void showToast(String message) {
        Context context = getActivity().getBaseContext();
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, message, duration);
        toast.show();
    }

    private void updateFavoriteData() {
        Long movieID = getMovieId(mUri);
        Cursor cursor = getActivity().getContentResolver().query(MovieContract.MovieEntry.buildMovieFavoriteWithIDUri(movieID),
                DETAIL_COLUMNS,
                null,
                null,
                null);
        if (cursor != null) {
            cursor.moveToFirst();
            int favorite = cursor.getInt(0);
            ContentValues cv = new ContentValues();
            switch (favorite) {
                case 0:
                    //if not favorite change star to black and update data
                    showToast("Add to favorite");
                    favoriteButton.setImageResource(R.drawable.ic_star_black_24dp);
                    cv.put(MovieContract.MovieEntry.COLUMN_MOVIE_FAVORITES, 1);
                    getActivity().getContentResolver().update(
                            MovieContract.MovieEntry.buildMovieFavoriteWithIDUri(movieID),
                            cv,
                            MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ? ",
                            new String[]{Long.toString(movieID)}
                    );
                    break;
                case 1:
                    showToast("Remove from favorite");
                    favoriteButton.setImageResource(R.drawable.ic_star_border_black_24dp);
                    cv.put(MovieContract.MovieEntry.COLUMN_MOVIE_FAVORITES, 0);
                    getActivity().getContentResolver().update(
                            MovieContract.MovieEntry.buildMovieFavoriteWithIDUri(movieID),
                            cv,
                            MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ? ",
                            new String[]{Long.toString(movieID)}
                    );
                    break;

            }
        }


    }

    void onSettingChange(int movieID){
        mRequestQueue.cancelAll("review");
        mRequestQueue.cancelAll("trailers");
        mUri = MovieContract.MovieEntry.buildMovieUri(movieID);
        DownloadJson();
        getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
            Log.d(LOG_TAG, "can get data");
            DownloadJson();
        }

        mCardViewTrailer = (CardView) rootView.findViewById(R.id.card_view_trailer);
        mCardViewReivew = (CardView) rootView.findViewById(R.id.card_view_review);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (null != mUri) {
            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(
                    getActivity(),
                    mUri,
                    DETAIL_COLUMNS,
                    null,
                    null,
                    null
            );
        }
        return null;
    }




    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {

            movieName.setText(data.getString(COL_MOVIE_NAME));
            Log.d(LOG_TAG, data.getString(COL_MOVIE_NAME));
            movieReleaseDate.setText(data.getString(COL_MOVIE_RELEASE_DATE));
            movieVote.setText(data.getString(COL_MOVIE_VOTE));
            movieOverView.setText(data.getString(COL_MOVIE_OVERVIEW));
            Glide.with(this)
                    .load(data.getString(COL_MOVIE_POSTER))
                    .placeholder(R.drawable.hold)
                    .fitCenter()
                    .into(poster);

            String review = data.getString(COL_MOVIE_REVIEW);
            String trailer = data.getString(COL_MOVIE_TRAILER);

            if (data.getInt(COL_MOVIE_FAVORITES) == 1) {
                favoriteButton.setImageResource(R.drawable.ic_star_black_24dp);
            } else {
                favoriteButton.setImageResource(R.drawable.ic_star_border_black_24dp);
            }

            JSONObject reviewData, trailerData;
            try {
                if (review != null) {
                    reviewData = new JSONObject(review);
                    movieReview.setText(formatReviewData(reviewData));
                    int reviewSize = reviewData.getJSONArray("results").length();
                    if (reviewSize == 0) {
                        mCardViewReivew.setVisibility(View.GONE);
                    }
                }

                if (trailer != null) {
                    trailerData = new JSONObject(trailer);
                    int trailerSize = trailerData.getJSONArray("results").length();
                    getYoutubeUri(trailerData);
                    switch (trailerSize) {
                        case 1:
                            trailerText_1.setText(GetJsonData.getString(0, trailerData, "name"));
                            trailerText_2.setVisibility(View.GONE);
                            trailerText_3.setVisibility(View.GONE);
                            trailerButton_2.setVisibility(View.GONE);
                            trailerButton_3.setVisibility(View.GONE);
                            break;
                        case 2:
                            trailerText_1.setText(GetJsonData.getString(0, trailerData, "name"));
                            trailerText_2.setText(GetJsonData.getString(1, trailerData, "name"));

                            trailerText_3.setVisibility(View.GONE);
                            trailerButton_3.setVisibility(View.GONE);
                            break;
                        case 0:
                            mCardViewTrailer.setVisibility(View.GONE);
                            break;
                        default:
                            trailerText_1.setText(GetJsonData.getString(0, trailerData, "name"));
                            trailerText_2.setText(GetJsonData.getString(1, trailerData, "name"));
                            trailerText_3.setText(GetJsonData.getString(2, trailerData, "name"));
                            break;
                    }
                }
            } catch (NullPointerException | JSONException e) {
                Log.d("JSON", "Can't get data");
            }
            mTrailer = data.getString(COL_MOVIE_TRAILER);
            mReview = data.getString(COL_MOVIE_REVIEW);
            trailerButton_1.setOnClickListener(this);
            trailerButton_2.setOnClickListener(this);
            trailerButton_3.setOnClickListener(this);
            favoriteButton.setOnClickListener(this);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {


    }

    private void getYoutubeUri(JSONObject data) throws JSONException, NullPointerException {
        int trailerSize = data.getJSONArray("results").length();
        for(int i = 0;i<(trailerSize%3);i++)
        {
            mYoutubeKey[i] = GetJsonData.getString(i, data, "key");
        }
    }

    private String formatReviewData(JSONObject data) throws JSONException, NullPointerException {
        int jsonSize = data.getJSONArray("results").length();
        String reviewFormated = "";
        for (int i = 0; i < jsonSize; i++) {
            String reviewText = GetJsonData.getString(i, data, "content");
            String author = GetJsonData.getString(i, data, "author");
            if (i < jsonSize - 1) {
                reviewFormated += reviewText + "\n\n" + author + "\n\n" + "-------\n\n";
            } else {
                reviewFormated += reviewText + "\n\n" + author + "\n\n";
            }
        }

        return reviewFormated;
    }


    private void watchYoutubeVideo(String id) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id));
            startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://www.youtube.com/watch?v=" + id));
            startActivity(intent);
        }
    }

    private Long getMovieId(Uri uri) {
        return MovieContract.MovieEntry.getMovieIdFromUri(uri);
    }

    /**
     * Download Json from web for review and Trailers
     */
    private void DownloadJson() {
        final String REVIEW_TAG = "review";
        final Long movieID = getMovieId(mUri);
        final String TRAILERS_TAG = "trailers";

        String apiKey = getResources().getString(R.string.api_key);
        String reviewUri = "http://api.themoviedb.org/3/movie/" + movieID + "/reviews?api_key=" + apiKey;
        String trailersUri = "http://api.themoviedb.org/3/movie/" + movieID + "/videos?api_key=" + apiKey;


        // Instantiate the RequestQueue with the cache and network.
        mRequestQueue = Volley.newRequestQueue(getContext());


        JsonObjectRequest reviewRequest = new JsonObjectRequest
                (Request.Method.GET, reviewUri, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("JSON", response.toString());
                        Log.d("JSON", "Download complete!");
                        mRequestQueue.cancelAll(REVIEW_TAG);

                        ContentValues cv = new ContentValues();
                        cv.put(MovieContract.MovieEntry.COLUMN_MOVIE_REVIEW, response.toString());

                        if (mReview == null) {
                            try {
                                getActivity().getContentResolver().update(
                                        MovieContract.MovieEntry.buildMovieReviewUri(movieID),
                                        cv,
                                        MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ? ",
                                        new String[]{Long.toString(movieID)}
                                );
                            }catch (NullPointerException e){
                                Log.d("Movie Provider","can't get data");
                            }
                        } else {
                            Log.d("JSON", "review not null");
                            Log.d("JSON", mReview);
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        mRequestQueue.cancelAll(REVIEW_TAG);


                    }
                });

        JsonObjectRequest trailerRequest = new JsonObjectRequest
                (Request.Method.GET, trailersUri, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("JSON", response.toString());
                        Log.d("JSON", "Download complete!");
                        mRequestQueue.cancelAll(TRAILERS_TAG);

                        ContentValues cv = new ContentValues();
                        cv.put(MovieContract.MovieEntry.COLUMN_MOVIE_TRAILER, response.toString());

                        if (mTrailer == null) {
                            try {
                                getActivity().getContentResolver().update(
                                        MovieContract.MovieEntry.buildMovieTrailerswUri(movieID),
                                        cv,
                                        MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ? ",
                                        new String[]{Long.toString(movieID)}
                                );
                            }catch (NullPointerException e){
                                Log.d("Movie Provider","can't get data");
                            }
                        } else {
                            Log.d("JSON", "trailer not null");
                            Log.d("JSON", mTrailer);
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        mRequestQueue.cancelAll(TRAILERS_TAG);


                    }
                });

        reviewRequest.setTag(REVIEW_TAG);
        trailerRequest.setTag(TRAILERS_TAG);

        mRequestQueue.add(reviewRequest);
        mRequestQueue.add(trailerRequest);
    }
}
