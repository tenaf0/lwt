package hu.garaba.audio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;

import static hu.garaba.audio.Language.ENGLISH;
import static hu.garaba.audio.Language.GERMAN;
import static hu.garaba.audio.Language.HUNGARIAN;
import static hu.garaba.audio.Language.SPANISH;


public class EdgeTtsClient implements WebSocket.Listener {

	public static final Logger logger = LoggerFactory.getLogger(EdgeTtsClient.class);

	private final static String URL = "wss://speech.platform.bing.com/consumer/speech/synthesize/readaloud/edge/v1?TrustedClientToken=6A5AA1D4EAFF4E9FB37E23D68491D6F4";

	public static final Map<Language, String> LANGUAGE_TO_VOICE = Map.of(
			ENGLISH, "en-US-JennyNeural",
			GERMAN, "de-DE-KatjaNeural",
			SPANISH, "es-ES-ElviraNeural",
			HUNGARIAN, "hu-HU-NoemiNeural"
	);

	private final WebSocket ws = HttpClient
			.newHttpClient()
			.newWebSocketBuilder()
			.header("Pragma", "no-cache")
			.header("Cache-Control", "no-cache")
			.header("Origin", "chrome-extension://jdiccldimpdaibmpdkjnbmckianbfold")
			.header("Accept-Encoding", "gzip, deflate, br")
			.header("Accept-Language", "en-US,en;q=0.9")
			.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.77 Safari/537.36 Edg/91.0.864.41")
			.buildAsync(URI.create(URL), this)
			.join();

	private final CountDownLatch latch = new CountDownLatch(1);

	private final ByteArrayOutputStream audio = new ByteArrayOutputStream();

	public byte[] synthesize(Language language, String text) {
		if (latch.getCount() != 1) {
			throw new IllegalStateException("Object is already used, create a new one!");
		}

		try {
			ws.sendText("Content-Type:application/json; charset=utf-8\r\nPath:speech.config\r\n\r\n" +
							"                    {\n" +
							"                        \"context\": {\n" +
							"                            \"synthesis\": {\n" +
							"                                \"audio\": {\n" +
							"                                    \"metadataoptions\": {\n" +
							"                                        \"sentenceBoundaryEnabled\": \"false\",\n" +
							"                                        \"wordBoundaryEnabled\": \"false\"\n" +
							"                                    },\n" +
							"                                    \"outputFormat\": \"audio-24khz-96kbitrate-mono-mp3\"\n" +
							"                                }\n" +
							"                            }\n" +
							"                        }\n" +
							"                    }", true)
					.join();

			sendRequest(language, text);
			latch.await();

			logger.info("Finished synthesizing");
			ws.abort();
			return audio.toByteArray();
		}
		catch (Exception e) {
			logger.error("Error while synthesizing", e);
			throw new RuntimeException(e);
		}
	}

	//TODO: break up long text to chunks, learn from the python lib
	private void sendRequest(Language language, String text) {
		String voice = LANGUAGE_TO_VOICE.get(language);
		String locale = voice.substring(0, 5);

		String request = "X-RequestId:f12ecbf11d4588e5e19dfaf1d2010d73\r\nContent-Type:application/ssml+xml\r\n" + getCurrentDate() +
				"\r\nPath:ssml\r\n\r\n<speak version=\"1.0\" xmlns=\"http://www.w3.org/2001/10/synthesis\" xml:lang=\"" + locale +
				"\"><voice name=\"" + voice + "\">" + sanitizeText(text) + "</voice></speak>";
		ws.sendText(request, true).join();
	}

	private String sanitizeText(String text) {
		return text.replaceAll("&", "");
	}

	@Override
	public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
		logger.debug("onText: " + data);
		if (data.toString().contains("Path:turn.end")) {
			latch.countDown();
		}
		webSocket.request(1);
		return null;
	}

	@Override
	public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
		logger.debug("onBinary: " + data);
		try {
			audio.write(data.array());
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		webSocket.request(1);
		return null;
	}

	@Override
	public void onError(WebSocket webSocket, Throwable error) {
		logger.error("onError: " + error);
		if (latch.getCount() > 1) {
			latch.countDown();
		}
	}

	@Override
	public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
		logger.debug("onClose");
		logger.debug("Reason: " + reason);
		logger.debug("StatusCode: " + statusCode);
		if (latch.getCount() > 1) {
			latch.countDown();
		}
		return null;
	}

	private String getCurrentDate() {
		SimpleDateFormat sdf = new SimpleDateFormat("E MMM dd yyyy HH:mm:ss");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		return sdf.format(new Date()) + " GMT+0000 (Coordinated Universal Time)Z";
	}
}
