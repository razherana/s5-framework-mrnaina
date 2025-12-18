package mg.razherana.framework.web.utils.json;

import java.util.ArrayList;
import java.util.List;

import mg.razherana.framework.web.utils.json.types.JsonElement;

public class JsonArrayParser {

  public static JsonElement parse(String jsonString) {
    jsonString = jsonString.trim();

    // Basic validation
    if (jsonString.charAt(0) != '[' || jsonString.charAt(jsonString.length() - 1) != ']') {
      throw new IllegalArgumentException("Invalid JSON array string: " + jsonString);
    }

    // Remove trailing [ and ]
    jsonString = jsonString.substring(1, jsonString.length() - 1);

    jsonString = jsonString.trim();
    if (jsonString.isEmpty()) {
      return JsonElement.of(new ArrayList<Object>());
    }
    List<Object> list = new ArrayList<>();

    for (String element : JsonObjectParser.splitTopLevelPairs(jsonString)) {
      String value = element.trim();
      if (value.isEmpty()) {
        continue;
      }

      // Parse value
      JsonElement jsonValue = JsonParser.parse(value);
      list.add(jsonValue);
    }

    return JsonElement.of(list);
  }
}
