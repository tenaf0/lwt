package hu.garaba.dictionary;

import com.alibaba.fastjson2.JSON;
import org.jetbrains.annotations.Nullable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CollinsDictionaryLookup implements DictionaryLookup2 {
    public static final System.Logger LOGGER = System.getLogger(CollinsDictionaryLookup.class.getCanonicalName());

    private final ExecutorService httpExecutorService = Executors.newVirtualThreadPerTaskExecutor();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .executor(httpExecutorService)
            .build();

    private record CollinsSearchResult(List<CollinsResultEntry> results) {}
    private record CollinsResultEntry(String entryLabel, String entryUrl, String entryId) {}
    public List<SearchResult> search(String word) throws IOException {
        LOGGER.log(System.Logger.Level.DEBUG, "Searching for \"" + word + "\"");

        try {
            HttpRequest request = buildRequest("/dictionaries/german-english/search?q=" + URLEncoder.encode(word, StandardCharsets.UTF_8));
            LOGGER.log(System.Logger.Level.DEBUG, request);
            HttpResponse<String> resp = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            CollinsSearchResult searchResult = JSON.parseObject(resp.body(), CollinsSearchResult.class);

            return searchResult.results.stream().map(r -> new SearchResult(r.entryLabel, r.entryId)).toList();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private final Pattern genderPattern = Pattern.compile("(masculine|feminine|neuter) noun");
    @Nullable
    private String determinePrefix(String grammarBlock) {
        if (grammarBlock == null) {
            return null;
        }

        String prefix = null;
        Matcher matcher = genderPattern.matcher(grammarBlock);
        if (matcher.matches()) {
            prefix = switch (matcher.group(1)) {
                case "masculine" -> "der";
                case "feminine" -> "die";
                case "neuter" -> "das";
                default -> throw new IllegalArgumentException();
            };
        }
        return prefix;
    }

    @Override
    public DictionaryEntry lookup(String word) throws IOException {
        LOGGER.log(System.Logger.Level.DEBUG, "Looking up \"" + word + "\"");

        try {
            HttpRequest request = buildRequest("/dictionaries/german-english/search/first/?q=" + URLEncoder.encode(word, StandardCharsets.UTF_8) + "&format=XML");
            HttpResponse<String> resp = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            return parseEntryContent(resp);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            throw new RuntimeException(e);
        }
    }

    @Override
    public DictionaryEntry selectById(String entryId) throws IOException {
        LOGGER.log(System.Logger.Level.DEBUG, "Selecting dictionary entry by id \"" + entryId + "\"");

        try {
            HttpRequest request = buildRequest("/dictionaries/german-english/entries/"
                    + URLEncoder.encode(entryId, StandardCharsets.UTF_8) + "/?format=XML");
            HttpResponse<String> resp = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            return parseEntryContent(resp);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            throw new RuntimeException(e);
        }
    }

    @Nullable
    private DictionaryEntry parseEntryContent(HttpResponse<String> resp) {
        var result = JSON.parseObject(resp.body());

        if (result.getString("errorCode") != null) {
            return null;
        }

        Document doc = Jsoup.parse(result.getString("entryContent"));

        Element gramGrp = doc.selectFirst(".gramGrp");
        String grammarBlock = gramGrp != null ? gramGrp.text() : null;

        Element sense = doc.selectFirst(".sense");
        String text = sense != null ? sense.text() : null;

        return new DictionaryEntry(new LemmaDisplay(determinePrefix(grammarBlock), cutPostfixNumber(result.getString("entryLabel")), null),
                createBrowserLinkToWord(result.getString("entryId")),
                grammarBlock,
                text);
    }

    private HttpRequest buildRequest(String urlPostfix) {
        return HttpRequest.newBuilder()
                .uri(URI.create("http://garaba.org:5000/" + urlPostfix))
                .GET()
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(30))
                .build();
    }

    private URI createBrowserLinkToWord(String entryId) {
        return URI.create("https://www.collinsdictionary.com/dictionary/german-english/" + entryId);
    }

    private static final Pattern postfixNumber = Pattern.compile("^(\\D+)\\d+$");

    private String cutPostfixNumber(String word) {
        Matcher matcher = postfixNumber.matcher(word);
        if (!matcher.matches()) {
            return word;
        } else {
            return matcher.group(1);
        }
    }
}
