package tanin.wait;

import com.eclipsesource.json.Json;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

public class FormTest extends Base {
  @Test
  void join() throws InterruptedException, IOException {
    go("/");

    fill(tid("email"), "test@test.com");
    click(tid("submitButton"));

    waitUntil(() -> assertTrue(hasElem(tid("success"))));
    assertFalse(hasElem(tid("error")));
  }

  @Test
  void validate() throws InterruptedException, IOException {
    go("/");

    fill(tid("email"), "test");
    click(tid("submitButton"));

    waitUntil(() -> assertTrue(hasElem(tid("error"))));
    assertFalse(hasElem(tid("success")));

    assertEquals("The email is invalid.", elem(tid("error")).getText().trim());
  }
}
