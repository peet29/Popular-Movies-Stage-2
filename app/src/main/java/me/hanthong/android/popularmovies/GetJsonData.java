package me.hanthong.android.popularmovies;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by peet29 on 29/9/2558.
 */
public class GetJsonData {
    /**
     *Get poster path from json
     * @param position a position in JSON array
     * @return url of poster movie
     */
    public static String getPicURL(int position,JSONObject json) throws JSONException,NullPointerException {
        String baseURL = "http://image.tmdb.org/t/p/w185/";
        String getURL = json.getJSONArray("results").getJSONObject(position).getString("poster_path");
        return baseURL+getURL;
    }

    /**
     * This method is for get a number data from json
     * @param position a position in JSON array
     * @param json a json object
     * @param tag a tag that data is store
     * @return a number from tag
     * @throws JSONException
     * @throws NullPointerException
     */

    public static double getNumber(int position,JSONObject json,String tag)throws JSONException,NullPointerException {
        return  json.getJSONArray("results").getJSONObject(position).getDouble(tag);
    }

    /**
     * This method is for get a string from json
     * @param position a position in JSON array
     * @param json a json object
     * @param tag a tag that store data
     * @return a string from tag
     * @throws JSONException
     * @throws NullPointerException
     */
    public static String getString(int position,JSONObject json,String tag)throws JSONException,NullPointerException {
        return  json.getJSONArray("results").getJSONObject(position).getString(tag);
    }
}
