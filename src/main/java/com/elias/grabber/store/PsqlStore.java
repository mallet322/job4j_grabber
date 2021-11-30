package com.elias.grabber.store;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import com.elias.grabber.model.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PsqlStore implements Store, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(PsqlStore.class.getName());

    private final Connection connection;

    public PsqlStore(Properties cfg) {
        connection = initConnection(cfg);
    }

    @Override
    public void save(Post post) {
        Objects.requireNonNull(post, "Post must not be null!");
        try (PreparedStatement statement = connection.prepareStatement(
                "insert into post(name, text, link, created) values(?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
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
        } catch (Exception e) {
            LOG.warn("Insert into post error", e);
        }
    }

    @Override
    public List<Post> getAll() {
        var posts = new ArrayList<Post>();
        try (PreparedStatement statement = connection.prepareStatement("select * from post")) {
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    posts.add(getPost(resultSet));
                }
            }
        } catch (Exception e) {
            LOG.warn("Find all from post error", e);
        }
        return posts;
    }

    @Override
    public Post findById(int id) {
        Objects.requireNonNull(id, "Post id must not be null!");
        Post post = null;
        try (PreparedStatement statement = connection.prepareStatement("select * from post where id = ?")) {
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                post = getPost(resultSet);
            }
        } catch (Exception e) {
            LOG.warn("Find by id from post error", e);
        }
        return post;
    }

    @Override
    public void close() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }

    private Post getPost(ResultSet resultSet) throws SQLException {
        return new Post(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getString("text"),
                resultSet.getString("link"),
                resultSet.getTimestamp("created").toLocalDateTime()
        );
    }

    private Connection initConnection(Properties config) {
        Connection connection = null;
        try {
            Class.forName(config.getProperty("jdbc.driver"));
            connection = DriverManager.getConnection(
                    config.getProperty("jdbc.url"),
                    config.getProperty("jdbc.username"),
                    config.getProperty("jdbc.password")
            );
        } catch (Exception e) {
            LOG.warn("Init database connection error", e);
        }
        return connection;
    }

}
