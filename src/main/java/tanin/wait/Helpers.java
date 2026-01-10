package tanin.wait;

import com.eclipsesource.json.JsonObject;

public class Helpers {
  public static String getStringOrNull(JsonObject obj, String key) {
    var value = obj.get(key);

    if (value == null) {
      return null;
    }

    if (value.isNull()) {
      return null;
    }

    return value.asString();
  }
}
