package tanin.wait;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;

import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class Base {
  static int PORT = 9091;
  static boolean IS_MAC = System.getProperty("os.name").toLowerCase().contains("mac");

  WaitServer server;

  @BeforeAll
  void setUpAll() throws SQLException, URISyntaxException, InterruptedException {
  }

  @BeforeEach
  void setUp() throws SQLException, URISyntaxException {
    server = new WaitServer(
      PORT,
      Main.getServiceAccountKeyJson(),
      Main.getSheetId(),
      Main.getSheetName()
    );
    server.start();
  }

  @AfterEach
  void tearDown() {
    if (server != null) {
      server.stop();
    }
  }

  int WAIT_UNTIL_TIMEOUT_MILLIS = 5000;

  @FunctionalInterface
  interface InterruptibleSupplier {
    boolean get() throws InterruptedException;
  }

  @FunctionalInterface
  interface VoidFn {
    void invoke() throws InterruptedException;
  }

  void waitUntil(VoidFn fn) throws InterruptedException {
    InterruptibleSupplier newFn = () -> {
      try {
        fn.invoke();
        return true;
      } catch (AssertionError | StaleElementReferenceException | java.util.NoSuchElementException |
               NoSuchElementException e) {
        return false;
      }
    };

    var startTime = System.currentTimeMillis();
    while ((System.currentTimeMillis() - startTime) < WAIT_UNTIL_TIMEOUT_MILLIS) {
      if (newFn.get()) return;
      Thread.sleep(500);
    }

    fn.invoke();
  }
}
