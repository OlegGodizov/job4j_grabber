package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.model.Post;
import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse implements Parse {

    private static final String SOURCE_LINK = "https://career.habr.com";
    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer?page=", SOURCE_LINK);
    public static final int PAGES_COUNT = 5;
    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    private String retrieveDescription(String link) {
        String description;
        try {
            Connection connection = Jsoup.connect(link);
            Document document = connection.get();
            description = document.selectFirst(".collapsible-description").text();
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to load post description");
        }
        return description;
    }

    private Post parsePost(Element element) {
        Element titleElement = element.select(".vacancy-card__title").first();
        Element linkElement = titleElement.child(0);
        Element dateElement = element.select(".vacancy-card__date").first().child(0);
        String dateAttr = dateElement.attr("datetime");
        String vacancyName = titleElement.text();
        String postLink = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
        String description = this.retrieveDescription(postLink);
        return new Post(vacancyName, postLink, description, dateTimeParser.parse(dateAttr));
    }

    @Override
    public List<Post> list(String link) {
        List<Post> posts = new ArrayList<>();
        for (int i = 1; i <= PAGES_COUNT; i++) {
            Connection connection = Jsoup.connect(String.format("%s%d", link, i));
            Document document;
            try {
                document = connection.get();
            } catch (IOException e) {
                throw new IllegalArgumentException("Failed to load site page");
            }
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> posts.add(parsePost(row)));
        }
        return posts;
    }

    public static void main(String[] args) {
        HabrCareerDateTimeParser dateParser = new HabrCareerDateTimeParser();
        HabrCareerParse parser = new HabrCareerParse(dateParser);
        parser.list(PAGE_LINK).forEach(System.out::println);
    }
}