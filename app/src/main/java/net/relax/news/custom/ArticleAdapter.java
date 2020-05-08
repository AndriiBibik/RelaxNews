package net.relax.news.custom;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import net.relax.news.R;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;

public class ArticleAdapter extends ArrayAdapter<Article> {

    private Context context;
    private List<Article> articles;

    public ArticleAdapter(Context context, List<Article> articles) {
        super(context, R.layout.list_item_article_summary, articles);

        this.context = context;
        this.articles = articles;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {

            convertView = LayoutInflater
                    .from(context)
                    .inflate(R.layout.list_item_article_summary, parent, false);

            convertView.setTag(new ViewHolder(convertView));
        }
        ViewHolder holder = (ViewHolder) convertView.getTag();

        Article article = articles.get(position);

        holder.section.setText(article.getSection());
        holder.title.setText(article.getTitle());
        holder.date.setText(article.getDate());

        return convertView;
    }

    class ViewHolder {
        @BindView(R.id.article_section_text) TextView section;
        @BindView(R.id.article_title) TextView title;
        @BindView(R.id.article_date) TextView date;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
