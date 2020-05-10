package net.relax.news.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import net.relax.news.MainActivity;
import net.relax.news.R;
import net.relax.news.custom.Article;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import android.util.Base64;
import android.util.Log;

public class Utils {

    //keys to extract values from json
    public static final String RESPONSE_JSON_OBJECT = "response";
    public static final String RESULTS_JSON_ARRAY = "results";
    public static final String SECTION_NAME_JSON_PRIMITIVE = "sectionName";
    public static final String WEB_TITLE_JSON_PRIMITIVE = "webTitle";
    public static final String DATE_JSON_PRIMITIVE = "webPublicationDate";
    public static final String WEB_URL_JSON_PRIMITIVE = "webUrl";
    public static final String TAGS_JSON_ARRAY = "tags";
    public static final String AUTHOR_WEB_TITLE_PRIMITIVE = "webTitle";

    //parameters for Uri
    public static final String PARAMETER_SEARCH = "q";
    public static final String PARAMETER_SHOW_TAGS = "show-tags";
    public static final String PARAMETER_SHOW_TAGS_CONTRIBUTOR = "contributor";
    public static final String PARAMETER_API_KEY = "api-key";

    //base uri
    public static final String BASE_URI = "https://content.guardianapis.com/search";

    //response code for successful connection
    public static final int RESPONSE_CODE_SUCCESS = 200;

    public static String getUriConsideringPrefs(Context context) {

        // variables to work with in this method
        String relaxation = context.getResources().getStringArray(R.array.section_items)[0].toLowerCase();
        String exercises = context.getResources().getStringArray(R.array.section_items)[1].toLowerCase();
        String all = context.getResources().getStringArray(R.array.section_items)[2].toLowerCase();

        SharedPreferences sp
            = context.getSharedPreferences(MainActivity.SHARED_PREFERENCES_NAME, context.MODE_PRIVATE);

        StringBuilder searchValueBuilder = new StringBuilder(
                sp.getString(MainActivity.SECTION_NAME_KEY, relaxation)
        ); // relaxation by default

        if (searchValueBuilder.toString().equalsIgnoreCase(relaxation)) {
            searchValueBuilder
                    .append(context.getString(R.string.and_in_uri))
                    .append(context.getString(R.string.health_word));
        } // relaxation AND health
        else if (searchValueBuilder.toString().equalsIgnoreCase(all)) { // all
            searchValueBuilder // relaxation
                    .append(context.getString(R.string.and_in_uri)) // AND
                    .append(context.getString(R.string.health_word))
                    .append(context.getString(R.string.or_in_uri)) // OR
                    .append(exercises); // exercises
        } // = relaxation%20AND%20health%20OR%exercises

        String searchValue = searchValueBuilder.toString();

        Uri uri = Uri.parse(BASE_URI);
        Uri.Builder uriBuilder = uri.buildUpon();

        uriBuilder.appendQueryParameter(PARAMETER_SEARCH, searchValue);
        uriBuilder.appendQueryParameter(PARAMETER_SHOW_TAGS, PARAMETER_SHOW_TAGS_CONTRIBUTOR);
        String apiKey = new String(Base64.decode(context.getString(R.string.api_key_encoded), android.util.Base64.DEFAULT));
        uriBuilder.appendQueryParameter(PARAMETER_API_KEY, apiKey);

        return uriBuilder.toString();
    }

    public static String getStringFromStream(InputStream inputStream) {

        if (inputStream == null)
            return "";

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        try {
            String line = bufferedReader.readLine();
            while(line != null) {
                stringBuilder.append(line);
                line = bufferedReader.readLine();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //in worse case return empty string if exception occurs
        return stringBuilder.toString();
    }

    public static InputStream getInputStream(String urlString) {

        try {
            URL url = new URL(urlString);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setConnectTimeout(10000);
            urlConnection.setReadTimeout(10000);
            urlConnection.connect();

            if (urlConnection.getResponseCode() == RESPONSE_CODE_SUCCESS)
                return urlConnection.getInputStream();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Article> getListOfArticles(String json) {
        try {
            JSONObject root = new JSONObject(json);

            if (root.has(RESPONSE_JSON_OBJECT)) {
                JSONObject response = root.getJSONObject(RESPONSE_JSON_OBJECT);

                if (response.has(RESULTS_JSON_ARRAY)) {

                    JSONArray jsonArray = response.getJSONArray(RESULTS_JSON_ARRAY);
                    List<Article> articles = new ArrayList<>();

                    for (int i = 0; i < jsonArray.length(); i++) {

                        JSONObject item = (JSONObject) jsonArray.get(i);

                        String sectionName
                                = item.has(SECTION_NAME_JSON_PRIMITIVE)
                                ? item.getString(SECTION_NAME_JSON_PRIMITIVE)
                                : "";
                        String title
                                = item.has(WEB_TITLE_JSON_PRIMITIVE)
                                ? item.getString(WEB_TITLE_JSON_PRIMITIVE)
                                : "";
                        // retrieving authors from json
                        String[] authors = new String[]{};
                        if (item.has(TAGS_JSON_ARRAY)) {
                            JSONArray tagsArray = item.getJSONArray(TAGS_JSON_ARRAY);
                            authors = new String[tagsArray.length()];
                            for (int j = 0; j < tagsArray.length(); j++) {
                                JSONObject tag = (JSONObject) tagsArray.get(j);
                                if (tag.has(AUTHOR_WEB_TITLE_PRIMITIVE))
                                    authors[j] = tag.getString(AUTHOR_WEB_TITLE_PRIMITIVE);
                            }
                        }
                        String date
                                = item.has(DATE_JSON_PRIMITIVE)
                                ? item.getString(DATE_JSON_PRIMITIVE).substring(0, 10)
                                : "";
                        String url
                                = item.has(WEB_URL_JSON_PRIMITIVE)
                                ? item.getString(WEB_URL_JSON_PRIMITIVE)
                                : "";
                        articles.add(new Article(sectionName, title, authors, date, url));
                    }
                    return articles;
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    // getting loader id considering shared prefs to load and reuse appropriate info
    public static int getLoaderIdConsideringPrefs(Context context) {

        SharedPreferences sp = context.getSharedPreferences(MainActivity.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);

        String[] array = context.getResources().getStringArray(R.array.section_items);
        String relaxation = array[0];
        String exercises = array[1];
        String all = array[2];

        String section = sp.getString(MainActivity.SECTION_NAME_KEY, all);

        if (section.equalsIgnoreCase(relaxation))
            return MainActivity.ArticlesLoader.LOADER_ID_RELAXATION;
        else if (section.equalsIgnoreCase(exercises))
            return MainActivity.ArticlesLoader.LOADER_ID_EXERCISES;
        else
            return MainActivity.ArticlesLoader.LOADER_ID_ALL;
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
