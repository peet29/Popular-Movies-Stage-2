package me.hanthong.android.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

/**
 * Created by peet29 on 29/9/2558.
 */
public class ImageAdapter extends CursorAdapter {
    private static final String LOG_TAG = ImageAdapter.class.getSimpleName();


    private static class ViewHolder {
        public final ImageView moviePoster;

        public ViewHolder(View view){
            moviePoster = (ImageView) view.findViewById(R.id.list_image);
        }

    }

    public ImageAdapter(Context context, Cursor c, int flags){
        super(context,c,flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_image, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        String posterPath = cursor.getString(MainActivityFragment.COL_MOVIE_POSTER);
        //Log.d("Poster", posterPath);
        Glide.with(context)
                .load(posterPath)
                .placeholder(R.drawable.hold)
                .into(viewHolder.moviePoster);
    }
}
