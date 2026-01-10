package tanin.wait;

import com.eclipsesource.json.Json;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

public class GoogleSheetService {

  private final String serviceAccountEmail;
  private final String serviceAccountPrivateKey;

  public GoogleSheetService(
    String serviceAccountEmail,
    String serviceAccountPrivateKey
  ) {
    this.serviceAccountEmail = serviceAccountEmail;
    this.serviceAccountPrivateKey = serviceAccountPrivateKey;
  }

  private PrivateKey loadEcPrivateKey(String pem) throws NoSuchAlgorithmException, InvalidKeySpecException {
    var privateKeyContent = pem.replace("-----BEGIN PRIVATE KEY-----", "").replace("-----END PRIVATE KEY-----", "").replaceAll("\\s", "");
    var decodedKey = Base64.getDecoder().decode(privateKeyContent);
    var keySpec = new PKCS8EncodedKeySpec(decodedKey);
    var keyFactory = KeyFactory.getInstance("RSA");
    return keyFactory.generatePrivate(keySpec);
  }

  String getAccessToken() throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
    var header = Json.object()
      .add("alg", "RS256")
      .add("typ", "JWT");
    var payload = Json.object()
      .add("iss", serviceAccountEmail)
      .add("scope", "https://www.googleapis.com/auth/spreadsheets")
      .add("iat", Instant.now().getEpochSecond())
      .add("exp", Instant.now().plus(5, ChronoUnit.MINUTES).getEpochSecond())
      .add("aud", "https://oauth2.googleapis.com/token");

    var encoder = Base64.getUrlEncoder().withoutPadding();
    var dataToSign = encoder.encodeToString(header.toString().getBytes(StandardCharsets.UTF_8)) + "." +
      encoder.encodeToString(payload.toString().getBytes(StandardCharsets.UTF_8));
    var signature = Signature.getInstance("SHA256withRSA");
    signature.initSign(loadEcPrivateKey(serviceAccountPrivateKey));
    signature.update(dataToSign.getBytes(StandardCharsets.UTF_8));

    var jwtToken = dataToSign + "." + encoder.encodeToString(signature.sign());
    var grantType = "urn:ietf:params:oauth:grant-type:jwt-bearer";
    var body = "grant_type=" + URLEncoder.encode(grantType, StandardCharsets.UTF_8) + "&assertion=" + URLEncoder.encode(jwtToken, StandardCharsets.UTF_8);

    try {
      HttpResponse<String> response;
      try (var client = HttpClient.newHttpClient()) {
        var request = HttpRequest.newBuilder()
          .uri(URI.create("https://oauth2.googleapis.com/token"))
          .header("Content-Type", "application/x-www-form-urlencoded")
          .POST(HttpRequest.BodyPublishers.ofString(body))
          .build();

        response = client.send(request, HttpResponse.BodyHandlers.ofString());
      }

      if (response.statusCode() != 200) {
        throw new RuntimeException("Failed to write to Google Sheet: " + response.body());
      }

      var json = Json.parse(response.body());

      return json.asObject().get("access_token").asString();
    } catch (Exception e) {
      throw new RuntimeException("Error writing to Google Sheet", e);
    }
  }

  public void write(
    String sheetId,
    String sheetName,
    String email,
    String group
  ) throws NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {
    var accessToken = getAccessToken();

    try {
      var url = String.format(
        "https://sheets.googleapis.com/v4/spreadsheets/%s/values/%s:append?valueInputOption=RAW&insertDataOption=INSERT_ROWS",
        sheetId, sheetName);

      var body = Json.object()
        .add(
          "values",
          Json.array()
            .add(
              Json.array()
                .add(email)
                .add(Instant.now().toString())
                .add(group == null ? "" : group)
            )
        );

      HttpResponse<String> response;
      try (var client = HttpClient.newHttpClient()) {
        var request = HttpRequest.newBuilder()
          .uri(URI.create(url))
          .header("Content-Type", "application/json")
          .header("Authorization", "Bearer " + accessToken)
          .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
          .build();

        response = client.send(request, HttpResponse.BodyHandlers.ofString());
      }

      if (response.statusCode() != 200) {
        throw new RuntimeException("Failed to write to Google Sheet: " + response.body());
      }
    } catch (Exception e) {
      throw new RuntimeException("Error writing to Google Sheet", e);
    }
  }
}
