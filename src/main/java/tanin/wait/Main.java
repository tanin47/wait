package tanin.wait;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Main {

  private static final Logger logger = Logger.getLogger(Main.class.getName());

  static {
    try (var configFile = Main.class.getResourceAsStream("/wait_logging.properties")) {
      LogManager.getLogManager().readConfiguration(configFile);
      logger.info("The log config (wait_logging.properties) has been loaded.");
    } catch (IOException e) {
      logger.warning("Could not load the log config file (wait_logging.properties): " + e.getMessage());
    }
  }

  static String readFromEnvOrFile(String envName, String filePath) {
    var value = System.getenv(envName);
    if (value == null || value.isEmpty()) {
      try {
        value = new String(Files.readAllBytes(Path.of(filePath)));
      } catch (IOException e) {
        throw new RuntimeException(envName + " environment variable or " + filePath + " file must be set");
      }
    }

    return value.trim();
  }

  static String getServiceAccountKeyJson() {
    return Main.readFromEnvOrFile("GOOGLE_SHEET_SERVICE_ACCOUNT_KEY_JSON", "./secrets/GOOGLE_SHEET_SERVICE_ACCOUNT_KEY_JSON");
  }
  static String getSheetId() {
    return Main.readFromEnvOrFile("GOOGLE_SHEET_ID", "./secrets/GOOGLE_SHEET_ID");
  }
  static String getSheetName() {
    return Main.readFromEnvOrFile("GOOGLE_SHEET_NAME", "./secrets/GOOGLE_SHEET_NAME");
  }

  public static void main(String[] args) {
    var main = new WaitServer(
      9090,
      getServiceAccountKeyJson(),
      getSheetId(),
      getSheetName()
    );
    main.start();
    main.minum.block();
  }
}
