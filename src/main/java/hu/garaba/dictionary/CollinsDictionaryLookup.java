package hu.garaba.dictionary;

import com.alibaba.fastjson2.JSON;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CollinsDictionaryLookup implements DictionaryLookup2 {
    public static final System.Logger LOGGER = System.getLogger(CollinsDictionaryLookup.class.getCanonicalName());
    private static final String COLLINS_API_KEY = System.getenv("COLLINS_API_KEY");

    private record CollinsSearchResult(List<CollinsResultEntry> results) {}
    private record CollinsResultEntry(String entryLabel, String entryUrl, String entryId) {}
    public List<SearchResult> search(String word) throws IOException {
        LOGGER.log(System.Logger.Level.DEBUG, "Searching for \"" + word + "\"");

        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.collinsdictionary.com/api/v1" + "/dictionaries/german-english/search"
                + "?q=" + word))
                .GET()
                .header("Accept", "application/json")
                .header("accessKey", COLLINS_API_KEY)
                .build();

        try {
            HttpResponse<String> resp = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            CollinsSearchResult searchResult = JSON.parseObject(resp.body(), CollinsSearchResult.class);

            return searchResult.results.stream().map(r -> new SearchResult(r.entryLabel, r.entryId)).toList();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public DictionaryEntry lookup(String word) throws IOException {
        LOGGER.log(System.Logger.Level.DEBUG, "Looking up \"" + word + "\"");

        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.collinsdictionary.com/api/v1" + "/dictionaries/german-english/search/first/"
                        + "?q=" + word + "&format=XML"))
                .GET()
                .header("Accept", "application/json")
                .header("accessKey", COLLINS_API_KEY)
                .build();

        try {
            HttpResponse<String> resp = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            var result = JSON.parseObject(resp.body());
            Document doc = Jsoup.parse(result.getString("entryContent"));
            String grammarBlock = doc.selectFirst(".gramGrp").text();
            String text = doc.selectFirst(".sense").text();


            return new DictionaryEntry(cutPostfixNumber(result.getString("entryLabel")),
                    createBrowserLinkToWord(result.getString("entryId")),
                    grammarBlock,
                    text);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public DictionaryEntry selectById(String entryId) throws IOException {
        LOGGER.log(System.Logger.Level.DEBUG, "Selecting dictionary entry by id \"" + entryId + "\"");

        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.collinsdictionary.com/api/v1/dictionaries/german-english/entries/"
                        + entryId + "/?format=XML"))
                .GET()
                .header("Accept", "application/json")
                .header("accessKey", COLLINS_API_KEY)
                .build();

        try {
            HttpResponse<String> resp = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            var result = JSON.parseObject(resp.body());
            Document doc = Jsoup.parse(result.getString("entryContent"));
            String grammarBlock = doc.selectFirst(".gramGrp").text();
            String text = doc.selectFirst(".sense").text();


            return new DictionaryEntry(cutPostfixNumber(result.getString("entryLabel")),
                    createBrowserLinkToWord(result.getString("entryId")),
                    grammarBlock,
                    text);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
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
