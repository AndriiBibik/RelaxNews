package net.relax.news.custom;

public class Article {

    private String section;
    private String title;
    private String[] authors;
    private String date;
    private String urlString;

    public Article(String section, String title, String[] authors, String date, String urlString) {
        this.section = section;
        this.title = title;
        this.authors = authors;
        this.date = date;
        this.urlString = urlString;
    }

    public String getSection() { return section; }
    public String getTitle() { return title; }
    public String[] getAuthors() { return authors; }
    public String getDate() { return date; }
    public String getUrlString() { return urlString; }
}
