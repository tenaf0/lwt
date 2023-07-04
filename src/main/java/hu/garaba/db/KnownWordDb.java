package hu.garaba.db;

import hu.garaba.model.CardEntry;

import java.io.Closeable;
import java.io.IOException;
import java.lang.ref.Cleaner;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class KnownWordDb implements Closeable {
    private static final Cleaner cleaner = Cleaner.create();

    private final Connection connection;
    private final Cleaner.Cleanable cleanable;

    public KnownWordDb(String path) throws SQLException {
        boolean needInit = !Files.exists(Path.of(path));
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

    public WordState isKnown(String lemma) {
        if (ignoredWords(lemma)) {
            return WordState.IGNORED;
        }

        try (var st = connection.prepareStatement("SELECT status FROM word WHERE LOWER(REPLACE(word, '|', '')) = LOWER(?)")) {
            Pattern startPattern = Pattern.compile("^\\.{2,}");
            lemma = startPattern.matcher(lemma).replaceAll("");

            Pattern endPattern = Pattern.compile("(\\.{2,}|:)$");
            lemma = endPattern.matcher(lemma).replaceAll("");

            st.setString(1, lemma);
            ResultSet resultSet = st.executeQuery();

            if (resultSet.next()) {
                return WordState.valueOf(resultSet.getString(1));
            } else {
                return WordState.UNKNOWN;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean ignoredWords(String lemma) {
        List<String> ignoredSymbols = List.of(",", ".", "?", "!", ";", ":");
        Pattern number = Pattern.compile("\\d+");

        return ignoredSymbols.contains(lemma) || number.matcher(lemma).matches();
    }

    public void addWord(CardEntry row, WordState state) {
        try (var st = connection.prepareStatement("INSERT INTO word (prefix, word, postfix, meaning, note, example_sentence, status, datetime) VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)")) {
            st.setString(1, row.prefix());
            st.setString(2, row.word());
            st.setString(3, row.postfix());
            st.setString(4, row.meaning());
            st.setString(5, row.note());
            st.setString(6, row.exampleSentence());
            st.setString(7, state.toString());
            st.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<CardEntry> fetchLearningWords() {
        try (var st = connection.prepareStatement("SELECT id, prefix, word, postfix, meaning, note, example_sentence, datetime FROM word WHERE status = 'LEARNING' ORDER BY id")) {
            ResultSet rs = st.executeQuery();

            List<CardEntry> result = new ArrayList<>();
            while (rs.next()) {
                result.add(new CardEntry(
                        rs.getLong(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4),
                        rs.getString(5),
                        rs.getString(6),
                        rs.getString(7)
                ));
            }

            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void initDB() {
        try (var st = connection.createStatement()) {
            st.execute("""
                    create table word (
                                  id               integer not null
                                      constraint word_pk
                                          primary key autoincrement,
                                  prefix           TEXT,
                                  word             TEXT    not null,
                                  postfix          TEXT,
                                  meaning          TEXT,
                                  note             TEXT,
                                  example_sentence TEXT,
                                  status           TEXT   not null,
                                  datetime         int    not null,
                                  UNIQUE(prefix,word,postfix)
                              );
                """);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws IOException {
        cleanable.clean();
    }
}