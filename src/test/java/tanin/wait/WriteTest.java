package tanin.wait;

import com.eclipsesource.json.Json;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

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
    System.out.println(response.body());
  }
}
