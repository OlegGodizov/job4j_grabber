package ru.job4j.grabber;

import ru.job4j.grabber.model.Post;
import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store, AutoCloseable {

    private Connection cnn;

    public PsqlStore(Properties cfg) {
        try {
            Class.forName(cfg.getProperty("jdbc.driver"));
            cnn = DriverManager.getConnection(
                    cfg.getProperty("jdbc.url"),
                    cfg.getProperty("jdbc.username"),
                    cfg.getProperty("jdbc.password")
            );
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private Post getPostByResultSet(ResultSet resultSet) throws SQLException {
        return new Post(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getString("text"),
                resultSet.getString("link"),
                resultSet.getTimestamp("created").toLocalDateTime()
        );
    }

    @Override
    public void save(Post post) {
        try (PreparedStatement statement =
                cnn.prepareStatement("INSERT INTO posts(name, text, link, created)"
                        + " values(?,?,?,?) ON CONFLICT (link) DO NOTHING", Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, post.getTitle());
            statement.setString(2, post.getDescription());
            statement.setString(3, post.getLink());
            statement.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
            statement.execute();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    post.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Post> getAll() {
        List<Post> posts = new ArrayList<>();
        try (Statement statement = cnn.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM posts")) {
            while (resultSet.next()) {
                posts.add(getPostByResultSet(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return posts;
    }

    @Override
    public Post findById(int id) {
        Post post = null;
        try (PreparedStatement statement =
                     cnn.prepareStatement("SELECT * FROM posts WHERE id=?")) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                post = getPostByResultSet(resultSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return post;
    }

    @Override
    public void close() throws Exception {
        if (cnn != null) {
            cnn.close();
        }
    }

    public static void main(String[] args) throws IOException {
        Properties properties = new Properties();
        try (InputStream resource = ClassLoader.getSystemResourceAsStream("rabbit.properties")) {
            properties.load(resource);
        }
        try (PsqlStore store = new PsqlStore(properties)) {
            DateTimeParser dateParser = new HabrCareerDateTimeParser();
            HabrCareerParse parser = new HabrCareerParse(dateParser);
            String sourceLink = "https://career.habr.com";
            String pageLink = String.format("%s/vacancies/java_developer?page=", sourceLink);
            parser.list(pageLink).forEach(store::save);
            store.getAll().forEach(System.out::println);
            System.out.println(store.findById(1));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}