package mg.razherana.framework.web.utils.json;

import mg.razherana.framework.web.utils.json.types.JsonElement;

public class JsonParser {

  public static JsonElement parse(String jsonString) {
    if (jsonString == null || jsonString.isBlank()) {
      throw new IllegalArgumentException("Input JSON string is null or empty");
    }
    
    jsonString = jsonString.trim();

    // Check that json is not an object or an array
    char firstChar = jsonString.charAt(0);
    if (firstChar != '{' && firstChar != '[') {
      // Check if string
      if (firstChar == '"' && jsonString.charAt(jsonString.length() - 1) == '"') {
        return JsonElement.of(jsonString.substring(1, jsonString.length() - 1));
      }

      // Check if boolean
      if (jsonString.equals("true") || jsonString.equals("false")) {
        return JsonElement.of(Boolean.parseBoolean(jsonString));
      }

      // Check if null
      if (jsonString.equals("null"))
        return JsonElement.of(null);

      // Check if number
      try {
        if (jsonString.contains(".") || jsonString.contains("e") || jsonString.contains("E")) {
          return JsonElement.of(Double.parseDouble(jsonString));
        } else {
          return JsonElement.of(Long.parseLong(jsonString));
        }
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException("Invalid JSON value: " + jsonString);
      }
    }

    // Delegate to JsonObjectParser or JsonArrayParser based on the first character
    if (firstChar == '{') {
      return JsonObjectParser.parse(jsonString);
    } else if (firstChar == '[') {
      return JsonArrayParser.parse(jsonString);
    }

    throw new IllegalArgumentException("Invalid JSON string: " + jsonString);
  }

}
