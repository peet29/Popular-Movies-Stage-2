package me.hanthong.android.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.PopupMenu;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements MainActivityFragment.Callback {
    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    private boolean mTwoPane;
    private MenuItem mFavoriteMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (findViewById(R.id.detail_container) != null) {
            mTwoPane = true;

            if (savedInstanceState == null) {

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.detail_container, new DetailFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
            //getSupportActionBar().setElevation(0f);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);

        setSupportActionBar(toolbar);
        //setHasOptionsMenu(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mFavoriteMenu = menu.findItem(R.id.action_favorite);
        switch (getFavoriteSetting()) {
            case 0:
                mFavoriteMenu.setIcon(R.drawable.ic_star_border_black_24dp);
                break;
            case 1:
                mFavoriteMenu.setIcon(R.drawable.ic_star_black_24dp);
                break;
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        MainActivityFragment mainActivityFragment = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_main);
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_favorite) {
            switch (getFavoriteSetting()) {
                case 0:
                    upDataFavoriteSetting(1);
                    mFavoriteMenu.setIcon(R.drawable.ic_star_black_24dp);
                    mainActivityFragment.onSettingChange();
                    showToast("Show only favorite.");

                    break;
                case 1:
                    upDataFavoriteSetting(0);
                    mFavoriteMenu.setIcon(R.drawable.ic_star_border_black_24dp);
                    mainActivityFragment.onSettingChange();
                    showToast("Show all movie.");
                    break;
            }
            mainActivityFragment.setPosition(GridView.INVALID_POSITION);
            return true;
        } else if (id == R.id.action_sort) {
            showPopUpMenu();
            mainActivityFragment.setPosition(GridView.INVALID_POSITION);
        }

        return super.onOptionsItemSelected(item);
    }

    private void showToast(String message) {
        Context context = getBaseContext();
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, message, duration);
        toast.show();
    }


    private void showPopUpMenu() {
        View menuItemView = findViewById(R.id.action_sort);
        PopupMenu popup = new PopupMenu(this, menuItemView);
        MenuInflater inflate = popup.getMenuInflater();
        inflate.inflate(R.menu.popup_menu, popup.getMenu());


        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                MainActivityFragment mainActivityFragment = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_main);
                int id = item.getItemId();
                if (id == R.id.popup_popular) {
                    upDateSortSetting(0);
                    mainActivityFragment.onSettingChange();
                    showToast("Sort by popular");
                } else if (id == R.id.popup_rate) {
                    upDateSortSetting(1);
                    mainActivityFragment.onSettingChange();
                    showToast("Sort by rate");
                }
                return true;
            }
        });
        popup.show();

    }

    private void upDateSortSetting(int sortMode) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit().putString(getString(R.string.pref_sorting_key), String.valueOf(sortMode)).apply();

    }

    private void upDataFavoriteSetting(int favorite) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit().putString(getString(R.string.pref_favorite_key), String.valueOf(favorite)).apply();
    }

    private int getFavoriteSetting() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String favorite = prefs.getString(getString(R.string.pref_favorite_key), getString(R.string.pref_favprite_default));
        return Integer.valueOf(favorite);
    }

    @Override
    public void onItemSelected(Uri contentUri) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle args = new Bundle();
            args.putParcelable(DetailFragment.DETAIL_URI, contentUri);

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detail_container, fragment, DETAILFRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, DetailActivity.class)
                    .putExtra(DetailFragment.DETAIL_URI, contentUri);
            startActivity(intent);
        }
    }

}
