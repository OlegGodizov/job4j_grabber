package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class HabrCareerParse {

    public static final DateTimeFormatter OUTPUT_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final String SOURCE_LINK = "https://career.habr.com";
    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer", SOURCE_LINK);

    private String retrieveDescription(String link) {
        String description;
        try {
            Connection connection = Jsoup.connect(link);
            Document document = connection.get();
            description = document.selectFirst(".collapsible-description").text();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return description;
    }

    public static void main(String[] args) throws IOException {
        HabrCareerDateTimeParser dateTimeParser = new HabrCareerDateTimeParser();
        HabrCareerParse parser = new HabrCareerParse();
        for (int i = 1; i <= 5; i++) {
            Connection connection = Jsoup.connect(String.format("%s%s%d", PAGE_LINK, "?page=", i));
            Document document = connection.get();
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> {
                Element titleElement = row.select(".vacancy-card__title").first();
                Element linkElement = titleElement.child(0);
                Element dateElement = row.select(".vacancy-card__date").first().child(0);
                String dateAttr = dateElement.attr("datetime");
                String vacancyName = titleElement.text();
                String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
                String date = dateTimeParser.parse(dateAttr).format(OUTPUT_FORMATTER);
                String description = parser.retrieveDescription(link);
                System.out.printf("%s %s %s%n%s%n", vacancyName, link, date, description);

            });
        }
    }
}