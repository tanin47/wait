package tanin.wait;

import com.eclipsesource.json.Json;
import com.renomad.minum.web.FullSystem;
import com.renomad.minum.web.Response;
import com.renomad.minum.web.StatusLine;

import java.io.IOException;
import java.util.Map;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static com.renomad.minum.web.RequestLine.Method.*;

public class WaitServer {
  private static final Logger logger = Logger.getLogger(WaitServer.class.getName());

  static {
    try (var configFile = Main.class.getResourceAsStream("/wait_logging.properties")) {
      LogManager.getLogManager().readConfiguration(configFile);
      logger.info("The log config (wait_logging.properties) has been loaded.");
    } catch (IOException e) {
      logger.warning("Could not load the log config file (wait_logging.properties): " + e.getMessage());
    }
  }

  int port;
  public FullSystem minum;

  GoogleSheetService googleSheetService;
  String sheetId;
  String sheetName;


  public WaitServer(
    int port,
    String serviceAccountKeyJson,
    String sheetId,
    String sheetName
  ) {
    this.port = port;
    var keyJson = Json.parse(serviceAccountKeyJson).asObject();
    googleSheetService = new GoogleSheetService(
      keyJson.get("client_email").asString(),
      keyJson.get("private_key").asString()
    );
    this.sheetId = sheetId;
    this.sheetName = sheetName;
  }

  public void start() {
    minum = MinumBuilder.build(port);
    var wf = minum.getWebFramework();

    wf.registerPath(
      GET,
      "",
      r -> {
        String content = new String(Main.class.getResourceAsStream("/html/index.html").readAllBytes());
        return Response.htmlOk(content);
      }
    );

    wf.registerPath(
      OPTIONS,
      "write",
      req -> {
        return Response.buildResponse(
          StatusLine.StatusCode.CODE_200_OK,
          Map.of(
            "Access-Control-Allow-Origin", req.getHeaders().valueByKey("Origin").getFirst(),
            "Access-Control-Allow-Methods", "POST",
            "Access-Control-Allow-Headers", "*",
            "Vary", "Origin"
          ),
          ""
        );
      }
    );

    wf.registerPath(
      POST,
      "write",
      req -> {
        var json = Json.parse(req.getBody().asString()).asObject();
        var email = Helpers.getStringOrNull(json, "email");
        var group = Helpers.getStringOrNull(json, "group");

        if (email != null) { email = email.trim(); }
        if (group != null) { group = group.trim(); }

        if (email == null || email.isBlank() || !email.contains("@")) {
          return Response.buildResponse(
            StatusLine.StatusCode.CODE_400_BAD_REQUEST,
            Map.of(
              "Content-Type", "application/json",
              "Access-Control-Allow-Origin", req.getHeaders().valueByKey("Origin").getFirst(),
              "Access-Control-Allow-Methods", "POST",
              "Access-Control-Allow-Headers", "*",
              "Vary", "Origin"
            ),
            Json.object()
              .add("error", "The email is invalid.")
              .toString()
          );
        }

        googleSheetService.write(sheetId, sheetName, email, group);

        return Response.buildResponse(
          StatusLine.StatusCode.CODE_200_OK,
          Map.of(
            "Content-Type", "application/json",
            "Access-Control-Allow-Origin", req.getHeaders().valueByKey("Origin").getFirst(),
            "Access-Control-Allow-Methods", "POST",
            "Access-Control-Allow-Headers", "*",
            "Vary", "Origin"
          ),
          Json.object().toString()
        );
      }
    );

    wf.registerPath(
      GET,
      "healthcheck",
      req -> {
        return Response.buildResponse(StatusLine.StatusCode.CODE_200_OK, Map.of("Content-Type", "text/plain"), "OK EWJF");
      }
    );
  }

  public void stop() {
    if (minum != null) {
      minum.shutdown();
    }
  }
}
