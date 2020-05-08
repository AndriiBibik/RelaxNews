package net.relax.news.utils;

import android.content.Context;
import android.content.SharedPreferences;
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

public class Utils {

    //keys to extract values from json
    public static final String RESPONSE_JSON_OBJECT = "response";
    public static final String RESULTS_JSON_ARRAY = "results";
    public static final String SECTION_NAME_JSON_PRIMITIVE = "sectionName";
    public static final String WEB_TITLE_JSON_PRIMITIVE = "webTitle";
    public static final String DATE_JSON_PRIMITIVE = "webPublicationDate";
    public static final String WEB_URL_JSON_PRIMITIVE = "webUrl";

    //base uri
    public static final String BASE_URI = "https://content.guardianapis.com/search";

    public static String getUriConsideringPrefs(Context context) {

        // variables to work with in this method
        String relaxation = context.getResources().getStringArray(R.array.section_items)[0].toLowerCase();
        String exercises = context.getResources().getStringArray(R.array.section_items)[1].toLowerCase();
        String all = context.getResources().getStringArray(R.array.section_items)[2].toLowerCase();

        SharedPreferences sp
            = context.getSharedPreferences(MainActivity.SHARED_PREFERENCES_NAME, context.MODE_PRIVATE);

        String searchValue = sp.getString(
                MainActivity.SECTION_NAME_KEY, relaxation); // relaxation by default

        if (searchValue.equalsIgnoreCase(relaxation)) {
            searchValue = relaxation
                    + context.getString(R.string.and_in_uri)
                    + context.getString(R.string.health_word);
        } // relaxation AND health
        else if (searchValue.equalsIgnoreCase(all)) { // all
            searchValue = relaxation // relaxation
                    + context.getString(R.string.and_in_uri) // AND
                    + context.getString(R.string.health_word)
                    + context.getString(R.string.or_in_uri) // OR
                    + exercises; // exercises

        } // = relaxation%20AND%20health%20OR%exercises

        Uri uri = Uri.parse(BASE_URI);
        Uri.Builder uriBuilder = uri.buildUpon();

        uriBuilder.appendQueryParameter("q", searchValue);
        String apiKey = new String(Base64.decode(context.getString(R.string.api_key_encoded), android.util.Base64.DEFAULT));
        uriBuilder.appendQueryParameter("api-key", apiKey);

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

            if (urlConnection.getResponseCode() == 200)
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
                        String date
                                = item.has(DATE_JSON_PRIMITIVE)
                                ? item.getString(DATE_JSON_PRIMITIVE)
                                : "";
                        String url
                                = item.has(WEB_URL_JSON_PRIMITIVE)
                                ? item.getString(WEB_URL_JSON_PRIMITIVE)
                                : "";
                        articles.add(new Article(sectionName, title, date, url));
                    }
                    return articles;
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
