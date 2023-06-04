package org.example.model;

import java.lang.ref.Cleaner;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class KnownWordDb {
    private static final Cleaner cleaner = Cleaner.create();

    private final Connection connection;
    private final Cleaner.Cleanable cleanable;

    public KnownWordDb(String path) throws SQLException {
        boolean needInit = false;
        if (!Files.exists(Path.of(path))) {
            needInit = true;
        }
        connection = DriverManager.getConnection("jdbc:sqlite:" + path);

        if (needInit) {
            initDB();
        }

        cleanable = cleaner.register(this, () -> {
            try {
                System.out.println("Closing db");
                connection.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public Model.WordState isKnown(String lemma) {
        try (var st = connection.prepareStatement("SELECT status FROM word WHERE lemma = ?")) {
            st.setString(1, lemma);
            ResultSet resultSet = st.executeQuery();

            if (resultSet.next()) {
                return Model.WordState.valueOf(resultSet.getString(1));
            } else {
                return Model.WordState.UNKNOWN;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void addWord(String lemma, Model.WordState state) {
        try (var st = connection.prepareStatement("INSERT INTO word (lemma, status, datetime) VALUES (?, ?, CURRENT_TIMESTAMP)")) {
            if (isKnown(lemma) != Model.WordState.UNKNOWN) {
                return;
            }

            st.setString(1, lemma);
            st.setString(2, state.toString());
            st.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void initDB() {
        try (var st = connection.createStatement()) {
            st.execute("""
                create table main.word
                (
                    id       integer not null
                        constraint word_pk
                            primary key autoincrement,
                    lemma    TEXT    not null,
                    status   TEXT    not null,
                    datetime int     not null
                );
                """);
            st.execute("""
                create table example_sentences
                (
                    id       integer not null
                        constraint example_sentences_pk
                            primary key autoincrement,
                    word_id  integer not null
                        constraint example_sentences_word_id_fk
                            references word (id),
                    sentence TEXT    not null
                );
                """);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
