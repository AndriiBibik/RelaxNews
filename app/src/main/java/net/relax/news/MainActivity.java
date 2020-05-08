package net.relax.news;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import net.relax.news.custom.Article;
import net.relax.news.custom.ArticleAdapter;
import net.relax.news.utils.Utils;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    public static final String LOG_TAG = MainActivity.class.getSimpleName();

    public static final String SHARED_PREFERENCES_NAME = "sharedPrefs";
    public static final String SECTION_NAME_KEY = "section";

    @BindView(R.id.spinner_section) Spinner spinnerSection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        //starting spinner functionality to choose section
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter
                .createFromResource(
                        this,
                        R.array.section_items,
                        android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // setting spinner to position stored in shared preferences
        spinnerSection.setOnItemSelectedListener(this);
        spinnerSection.setAdapter(adapter1);
        spinnerSection.setSelection(adapter1.getPosition(getStoredSection()));

        // starting AsyncTask
        new ArticlesAssyncTask().execute(Utils.getUriConsideringPrefs(MainActivity.this));
    }

    @OnClick (R.id.refresh_results_button)
    void refreshResults() {
        //enable progress indicator
        findViewById(R.id.progress_indicator).setVisibility(View.VISIBLE);
        //disable list view
        findViewById(R.id.list_articles).setVisibility(View.GONE);
        // starting AsyncTask
        new ArticlesAssyncTask().execute(Utils.getUriConsideringPrefs(MainActivity.this));
    }

    // Getting section from shared preferences
    private String getStoredSection() {
        SharedPreferences sp = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        //'All' is the default section here
        String defaultSection = getResources().getStringArray(R.array.section_items)[2];
        return sp.getString(SECTION_NAME_KEY, defaultSection);
    }

    // Update UI, means update list of articles
    private void updateUi(List<Article> articles) {

        ArrayAdapter<Article> adapter = new ArticleAdapter(this, articles);
        ListView listArticles = findViewById(R.id.list_articles);
        listArticles.setAdapter(adapter);
        listArticles.setEmptyView(findViewById(R.id.empty_view));
        listArticles.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(((Article)parent.getItemAtPosition(position)).getUrlString()));
            startActivity(intent);
        });

        // enable list view
        listArticles.setVisibility(View.VISIBLE);
        // disable progress indicator
        findViewById(R.id.progress_indicator).setVisibility(View.GONE);

    }

    // AsyncTask to run Http request
    private class ArticlesAssyncTask extends AsyncTask<String, Void, List<Article>> {

        @Override
        protected List<Article> doInBackground(String... urls) {

            if (urls != null && urls.length > 0 ) {
                if (urls[0] != null && !urls[0].isEmpty()) {

                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // getting list of articles from url
                    InputStream jsonInputStream = Utils.getInputStream(urls[0]);
                    String json = Utils.getStringFromStream(jsonInputStream);
                    List<Article> articles = Utils.getListOfArticles(json);

                    return articles;
                }

            }

            return new ArrayList<>();
        }

        @Override
        protected void onPostExecute(List<Article> articles) {
            updateUi(articles);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //store selected item in shared preferences
        SharedPreferences sp = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        sp
            .edit()
            .putString(SECTION_NAME_KEY, (String) parent.getItemAtPosition(position))
            .apply();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
