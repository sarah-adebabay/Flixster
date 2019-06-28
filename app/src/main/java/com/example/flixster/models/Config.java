package com.example.flixster.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Config {

    //the base url for loading images
    String imageBaseURL;

    //the poser size to use when fetching images, part of the url
    String posterSize;

    //the backdrop size to use when fetching images
    String backdropSize;


    public Config(JSONObject object) throws JSONException {
        JSONObject images = object.getJSONObject("images");

        //get the image base url
        imageBaseURL = images.getString("secure_base_url");
        //get the poster size
        JSONArray posterSizeOptions = images.getJSONArray("poster_sizes");
        //use the option at index 3 (which is where I expect the value to be) or w342 as a fallback
        posterSize = posterSizeOptions.optString(3, "w342");
        JSONArray backdropSizeOptions = images.getJSONArray("backdrop_sizes");
        backdropSize = backdropSizeOptions.optString(1,"w780");
    }

    //helper for creating URLs
    public String getImageURL(String size, String path) {
        return String.format("%s%s%s", imageBaseURL, size, path); // constructs the URL, and it is not refering to specific sizes to support multiple sizes/flexibility
    }

    public String getBackdropSize() {
        return backdropSize;
    }

    public String getImageBaseURL() {
        return imageBaseURL;
    }

    public String getPosterSize() {
        return posterSize;
    }
}
