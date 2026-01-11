package tanin.wait;

import com.eclipsesource.json.Json;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WriteTest extends Base {
  @Test
  void writeEmail() throws InterruptedException, IOException {
    var client = HttpClient.newHttpClient();
    var request = HttpRequest.newBuilder()
      .uri(URI.create("http://localhost:" + PORT + "/write"))
      .header("Content-Type", "application/json")
      .POST(HttpRequest.BodyPublishers.ofString(
        Json.object()
          .add("email", "test-email@backdooradmin.com")
          .toString()
      ))
      .build();

    var response = client.send(request, HttpResponse.BodyHandlers.ofString());
    assertEquals(200, response.statusCode());
  }

  @Test
  void validateInvalidEmail() throws InterruptedException, IOException {
    var client = HttpClient.newHttpClient();
    var request = HttpRequest.newBuilder()
      .uri(URI.create("http://localhost:" + PORT + "/write"))
      .header("Content-Type", "application/json")
      .POST(HttpRequest.BodyPublishers.ofString(
        Json.object()
          .add("email", "aaabbb")
          .toString()
      ))
      .build();

    var response = client.send(request, HttpResponse.BodyHandlers.ofString());
    assertEquals(400, response.statusCode());
    var json = Json.parse(response.body());
    assertEquals("The email is invalid.", json.asObject().get("error").asString());
  }

  @Test
  void validateEmptyEmail() throws InterruptedException, IOException {
    var client = HttpClient.newHttpClient();
    var request = HttpRequest.newBuilder()
      .uri(URI.create("http://localhost:" + PORT + "/write"))
      .header("Content-Type", "application/json")
      .POST(HttpRequest.BodyPublishers.ofString(
        Json.object()
          .add("email", "")
          .toString()
      ))
      .build();

    var response = client.send(request, HttpResponse.BodyHandlers.ofString());
    assertEquals(400, response.statusCode());
    var json = Json.parse(response.body());
    assertEquals("The email is invalid.", json.asObject().get("error").asString());
  }
}
