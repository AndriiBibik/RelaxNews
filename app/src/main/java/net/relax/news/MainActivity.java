package net.relax.news;

import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import net.relax.news.custom.Article;
import net.relax.news.custom.ArticleAdapter;
import net.relax.news.utils.Utils;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements
        AdapterView.OnItemSelectedListener,
        LoaderManager.LoaderCallbacks<List<Article>> {

    public static final String LOG_TAG = MainActivity.class.getSimpleName();

    public static final String SHARED_PREFERENCES_NAME = "sharedPrefs";
    public static final String SECTION_NAME_KEY = "section";

    // list of articles that could be loaded by loader
    private static List<Article> articlesRelaxation;
    private static List<Article> articlesExercises;
    private static List<Article> articlesAll;


    @BindView(R.id.spinner_section)
    Spinner spinnerSection;

    // empty view
    @BindView(R.id.empty_view)
    TextView emptyView;

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

        if (Utils.isNetworkAvailable(this)) {
            // initializing loader
            int loaderId = Utils.getLoaderIdConsideringPrefs(this);
            getSupportLoaderManager().initLoader(loaderId, null, this);

        } else {
            emptyView.setText(R.string.internet_down_text);
            updateUi(new ArrayList<>());
        }

    }

    @OnClick(R.id.refresh_results_button)
    void refreshResults() {
        // make loading indicator visible again
        findViewById(R.id.progress_indicator).setVisibility(View.VISIBLE);
        // check for network connection
        if (Utils.isNetworkAvailable(this)) {
            // empty view to initial state (indicating there is no results)
            emptyView.setText(R.string.empty_view_text);
            // initializing loader
            int loaderId = Utils.getLoaderIdConsideringPrefs(this);
            getSupportLoaderManager().initLoader(loaderId, null, this);
        } else {
            // no internet text into empty view
            emptyView.setText(R.string.internet_down_text);
            // then update ui with empty data
            updateUi(new ArrayList<>());
        }
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
            intent.setData(Uri.parse(((Article) parent.getItemAtPosition(position)).getUrlString()));
            startActivity(intent);
        });

        // disable progress indicator
        findViewById(R.id.progress_indicator).setVisibility(View.GONE);
    }

    // Loader
    @Override
    public Loader<List<Article>> onCreateLoader(int id, Bundle args) {
        return new ArticlesLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<List<Article>> loader, List<Article> articles) {
        updateUi(articles);
    }

    @Override
    public void onLoaderReset(Loader<List<Article>> loader) {
        updateUi(new ArrayList<>());
    }

    public static class ArticlesLoader extends AsyncTaskLoader<List<Article>> {

        public static final int LOADER_ID_RELAXATION = 100;
        public static final int LOADER_ID_EXERCISES = 101;
        public static final int LOADER_ID_ALL = 102;

        public ArticlesLoader(Context context) {
            super(context);
        }

        @Override
        public List<Article> loadInBackground() {

            InputStream jsonInputStream = Utils.getInputStream(Utils.getUriConsideringPrefs(getContext()));
            String json = Utils.getStringFromStream(jsonInputStream);
            List<Article> articles = Utils.getListOfArticles(json);

            switch (getId()) {
                case LOADER_ID_RELAXATION:
                    articlesRelaxation = articles;
                    break;
                case LOADER_ID_EXERCISES:
                    articlesExercises = articles;
                    break;
                default:
                    articlesAll = articles;
                    break;
            }

            return articles;
        }

        @Override
        protected void onStartLoading() {
            switch (getId()) {
                case LOADER_ID_RELAXATION:
                    if (articlesRelaxation == null) {
                        forceLoad();
                    }
                    break;
                case LOADER_ID_EXERCISES:
                    if (articlesExercises == null) {
                        forceLoad();
                    }
                    break;
                case LOADER_ID_ALL:
                    if (articlesAll == null) {
                        forceLoad();
                    }
                    break;
            }
        }
    }

    // Spinner
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
