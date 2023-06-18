package hu.garaba.audio;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EdgeTtsClientTest {

	@Test
	void shouldSynthesizeAudio() throws IOException {
		// Arrange
		var client = new EdgeTtsClient();

		// Act
		byte[] audio = client.synthesize(Language.GERMAN, "Hallo Welt");
		var path = Paths.get("/tmp/" + UUID.randomUUID() + ".mp3");
		Files.write(path, audio);

		// Assert
		assertTrue(audio.length > 0);
		assertTrue(Files.exists(path));
	}

	@Test
	void shouldFailWhenReused() {
		var client = new EdgeTtsClient();

		// run 1
		client.synthesize(Language.ENGLISH, "Hello World");

		// run 2
		assertThrows(
				IllegalStateException.class,
				() -> client.synthesize(Language.ENGLISH, "Hello World")
		);
	}
}
