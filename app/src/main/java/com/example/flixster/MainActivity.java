package com.example.flixster;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flixster.models.Movie;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    // declaring constants for strings we want to reference multiple times
    // base URL for API
    public final static String API_BASE_URL = "https://api.themoviedb.org/3";

    // the parameter name of the API Key;
    public final static String API_KEY_PARAM = "api_key";

    // instance fields that are corresponding to specific movies
    AsyncHttpClient client;


    //tag for logging activity
    public final static String TAG = "MovieListActivity";

    //the base url for loading images
    String imageBaseURL;

    //the poser size to use when fetching images, part of the url
    String posterSize;

    //the list of currently playing movies
    ArrayList<Movie> movies;

    //the recycler view
    RecyclerView rvMovies;

    //the adapater wired to the recycler view
    MovieAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        client = new AsyncHttpClient();

        //initializing the list of movies
        movies = new ArrayList<>();

        //initialize the adapter -- movies array cannot be reinitialized after this point
        adapter = new MovieAdapter(movies);

        //resolve the recycler view and connect a layout manager and the adapter
        rvMovies = findViewById(R.id.rvMovies);
        rvMovies.setLayoutManager(new LinearLayoutManager(this));
        rvMovies.setAdapter(adapter);

        //get the configuration on app creation
        getConfiguration();
    }

    //get the list of currently playing movies from the API
    private void getNowPlaying() {
        //create URL
        String url = API_BASE_URL + "/movie/now_playing";

        // set the requested parameters
        RequestParams params = new RequestParams();
        params.put(API_KEY_PARAM, getString(R.string.api_key));

        //execute a GET request expecting a JSON object response
        client.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // load the results into movies list
                try {
                    JSONArray results = response.getJSONArray("results");
                    //iterate through result sets and create Movie objects
                    for (int i = 0; i < results.length(); i++) {
                        Movie movie = new Movie(results.getJSONObject(i));
                        movies.add(movie);
                        //notify adapter that a a row was added
                        adapter.notifyItemInserted(movies.size() - 1);
                    }
                    Log.i(TAG, String.format("Loaded %s movies", results.length()));
                }
                catch (JSONException E) {
                    logError("Failed to parse now_playing movies", E, true);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
               logError("Failed to get data from now_playing endpoint", throwable, true);
            }
        });
    }

    private void getConfiguration() {
        //create URL
        String url = API_BASE_URL + "/configuration";

        // set the requested parameters
        RequestParams params = new RequestParams();
        params.put(API_KEY_PARAM, getString(R.string.api_key)); // API key is always required

        //execute a GET request expecting a JSON object response
        client.get(url, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONObject images = response.getJSONObject("images");

                    //get the image base url
                    imageBaseURL = images.getString("secure_base_url");
                    //get the poster size
                    JSONArray posterSizeOptions = images.getJSONArray("poster_sizes");
                    //use the option at index 3 (which is where I expect the value to be) or w342 as a fallback
                    posterSize = posterSizeOptions.optString(3, "w342");
                    Log.i(TAG, String.format("Loaded configuration with imageBaseUrl %s and posterSize %s", imageBaseURL, posterSize));
                }
                catch (JSONException E) {
                    logError("Failed parsing configuration", E, true);
                }
                //get the now playing movie list
                getNowPlaying();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
               logError("Failed getting configuration", throwable, true);
            }

        });

    }

    //handler errors, log and alert user
    private void logError(String message, Throwable error, boolean alertUser) {
        // always log the error
        Log.e(TAG, message, error);
        //alert the user to avoid silent errors
        if (alertUser) {
            //shows a long toast with the error message
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
        }
    }
}
