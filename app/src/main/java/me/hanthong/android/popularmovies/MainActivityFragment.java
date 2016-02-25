package me.hanthong.android.popularmovies;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Vector;

import me.hanthong.android.popularmovies.data.MovieContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();

    private GridView mGridview;
    private int mPosition = GridView.INVALID_POSITION;
    private static final String SELECTED_KEY = "selected_position";
    private ImageAdapter mImageAdapter;

    private static final int MOVIE_LOADER = 0;

    private static final String[] MOVIE_COLUMNS = {

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

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(MOVIE_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri movieUri);
    }

    public MainActivityFragment() {

    }

    private String createPosterURI() {
        String apiKey = getResources().getString(R.string.api_key);
        return "http://api.themoviedb.org/3/discover/movie?sort_by=popularity.desc&api_key=" + apiKey;
    }

    @Override
    public void onStart() {
        super.onStart();
        DownloadJson(createPosterURI());
    }

    @Override
    public void onResume() {
        super.onResume();
        mGridview.smoothScrollToPosition(mPosition);
        onSettingChange();
        Log.d("Setting", "Sort");
        Log.d("Grid", "redraw Grid");

    }

    void onSettingChange() {
        DownloadJson(createPosterURI());
        getLoaderManager().restartLoader(MOVIE_LOADER, null, this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);


        mImageAdapter = new ImageAdapter(getActivity(), null, 0);
        mGridview = (GridView) rootView.findViewById(R.id.gridview);
        mGridview.setAdapter(mImageAdapter);

        //activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mGridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);

                /*Intent intent = new Intent(getActivity(), DetailActivity.class)
                        .putExtra(DetailFragment.DETAIL_URI, MovieContract.MovieEntry.buildMovieUri(cursor.getLong(COL_MOVIE_ID)));
                startActivity(intent);*/

                if (cursor != null) {
                    ((Callback) getActivity())
                            .onItemSelected(MovieContract.MovieEntry.buildMovieUri(cursor.getLong(COL_MOVIE_ID)));
                }
                mPosition = position;
            }
        });

        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            // The listview probably hasn't even been populated yet.  Actually perform the
            // swapout in onLoadFinished.
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }

        return rootView;
    }

    private int getSortSetting() {
        // 0 for popularity
        // 1 for vote_average
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortMode = prefs.getString(getString(R.string.pref_sorting_key), getString(R.string.pref_sorting_default));
        return Integer.valueOf(sortMode);
    }

    private int getFavoriteSetting() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String favorite = prefs.getString(getString(R.string.pref_favorite_key), getString(R.string.pref_favprite_default));
        return Integer.valueOf(favorite);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if(mPosition != GridView.INVALID_POSITION){
            outState.putInt(SELECTED_KEY,mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    /**
     * Download Json from internet use Volley
     */
    private void DownloadJson(String posterURL) {
        final RequestQueue mRequestQueue;

        // Instantiate the RequestQueue with the cache and network.
        mRequestQueue = Volley.newRequestQueue(getContext());


        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, posterURL, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("JSON", response.toString());
                        Log.d("JSON", "Download complete!");
                        mRequestQueue.stop();

                        try {
                            FillJsonData(response);
                        } catch (JSONException | NullPointerException e) {
                            Log.d("JSON", "Fail to load JSON");
                        } finally {
                            Log.d("JSON", "Done fill");
                        }
                        //mImageAdapter.notifyDataSetChanged();
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        mRequestQueue.stop();


                    }
                });

        mRequestQueue.add(jsObjRequest);
    }


    /**
     * Fill json data to content provider so it don't need json object anymore
     *
     * @throws JSONException
     * @throws NullPointerException
     */
    private void FillJsonData(JSONObject jsonData) throws JSONException, NullPointerException {
        int jsonSize = jsonData.getJSONArray("results").length();
        Vector<ContentValues> cVVector = new Vector<ContentValues>(jsonSize);
        for (int i = 0; i < jsonSize; i++) {
            double id = GetJsonData.getNumber(i, jsonData, "id");
            double popularity = GetJsonData.getNumber(i, jsonData, "popularity");
            double vote = GetJsonData.getNumber(i, jsonData, "vote_average");
            String name = GetJsonData.getString(i, jsonData, "original_title");
            String posterPath = GetJsonData.getPicURL(i, jsonData);
            String overView = GetJsonData.getString(i, jsonData, "overview");
            String releaseDate = GetJsonData.getString(i, jsonData, "release_date");

            //add data to in Vector
            ContentValues movieValues = new ContentValues();

            movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, id);
            movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_POPULARITY, popularity);
            movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_VOTE, vote);
            movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_NAME, name);
            movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_POSTER, posterPath);
            movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_OVERVIEW, overView);
            movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_RELEASE_DATE, releaseDate);
            cVVector.add(movieValues);
        }

        // add to database
        if (cVVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            getContext().getContentResolver().bulkInsert(MovieContract.MovieEntry.CONTENT_URI, cvArray);
        }

        //Log.d(LOG_TAG, "Sync Complete. " + cVVector.size() + " Inserted");
    }

    int getPosition()
    {
        return mPosition;
    }
    void setPosition(int position)
    {
        mPosition = position;
    }

    GridView getGridview()
    {
        return mGridview;
    }

    //start loader

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String sortOrder;
        //Sort order by popularity
        if (getSortSetting() == 0) {
            sortOrder = MovieContract.MovieEntry.COLUMN_MOVIE_POPULARITY + " DESC";
        } else {
            //Sort order by Vote
            sortOrder = MovieContract.MovieEntry.COLUMN_MOVIE_VOTE + " DESC";
        }

        Uri MovieUri;

        if (getFavoriteSetting() == 0) {
            MovieUri = MovieContract.MovieEntry.CONTENT_URI;
        } else {
            MovieUri = MovieContract.MovieEntry.buildMovieFavoirteUri();
        }

        return new CursorLoader(getActivity(),
                MovieUri,
                MOVIE_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        Log.d("cursor data", Arrays.toString(data.getColumnNames()));
        mImageAdapter.swapCursor(data);

        if (mPosition != GridView.INVALID_POSITION) {
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            mGridview.smoothScrollToPosition(mPosition);
            mGridview.requestFocusFromTouch();
            mGridview.setSelection(mPosition);
        }else if(getActivity().findViewById(R.id.detail_container) != null && mImageAdapter.getCount() > 0){
            mGridview.requestFocusFromTouch();
            mGridview.setSelection(0);
            mPosition = 0;
            mGridview.post(new Runnable() {
                @Override
                public void run() {
                    mGridview.performItemClick(
                            mImageAdapter.getView(0, null, null), 0, mImageAdapter.getItemId(0)
                    );
                }
            });
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mImageAdapter.swapCursor(null);
    }
}
