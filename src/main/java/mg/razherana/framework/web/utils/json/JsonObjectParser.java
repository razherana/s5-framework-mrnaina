package mg.razherana.framework.web.utils.json;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import mg.razherana.framework.web.utils.json.types.JsonElement;

public class JsonObjectParser {
  private JsonObjectParser() {
    // Private constructor to prevent instantiation
  }

  public static JsonElement parse(String jsonString) {
    jsonString = jsonString.trim();

    // Basic validation
    if (jsonString.charAt(0) != '{' || jsonString.charAt(jsonString.length() - 1) != '}') {
      throw new IllegalArgumentException("Invalid JSON object string: " + jsonString);
    }

    // Remove trailing { and }
    jsonString = jsonString.substring(1, jsonString.length() - 1);

    jsonString = jsonString.trim();
    if (jsonString.isEmpty()) {
      return JsonElement.of(new HashMap<String, Object>());
    }

    Map<String, Object> map = new HashMap<>();

    for (String rawPair : splitTopLevelPairs(jsonString)) {
      String pair = rawPair.trim();
      if (pair.isEmpty()) {
        continue;
      }

      int colonIndex = indexOfTopLevelColon(pair);
      if (colonIndex == -1) {
        throw new IllegalArgumentException("Invalid JSON key-value pair: " + pair);
      }

      String rawKey = pair.substring(0, colonIndex).trim();
      String rawValue = pair.substring(colonIndex + 1).trim();
      if (rawKey.isEmpty()) {
        throw new IllegalArgumentException("Missing key in JSON pair: " + pair);
      }

      String key = rawKey;
      if (key.charAt(0) == '"' && key.charAt(key.length() - 1) == '"') {
        key = key.substring(1, key.length() - 1);
      }

      JsonElement jsonValue = JsonParser.parse(rawValue);
      map.put(key, jsonValue);
    }

    return JsonElement.of(map);
  }

  static Iterable<String> splitTopLevelPairs(String input) {
    java.util.List<String> pairs = new java.util.ArrayList<>();
    if (input == null || input.isEmpty()) {
      return pairs;
    }

    StringBuilder current = new StringBuilder();
    Deque<Character> stack = new ArrayDeque<>();
    boolean inString = false;
    boolean escaping = false;

    for (int i = 0; i < input.length(); i++) {
      char c = input.charAt(i);

      if (escaping) {
        current.append(c);
        escaping = false;
        continue;
      }

      if (c == '\\' && inString) {
        current.append(c);
        escaping = true;
        continue;
      }

      if (c == '"') {
        inString = !inString;
        current.append(c);
        continue;
      }

      if (!inString) {
        if (c == '{' || c == '[') {
          stack.push(c);
        } else if (c == '}' || c == ']') {
          if (stack.isEmpty() || !isMatchingPair(stack.peek(), c)) {
            throw new IllegalArgumentException("Unbalanced brackets in JSON object: " + input);
          }
          stack.pop();
        } else if (c == ',') {
          if (stack.isEmpty()) {
            pairs.add(current.toString().trim());
            current.setLength(0);
            continue;
          }
        }
      }

      current.append(c);
    }

    if (inString || !stack.isEmpty()) {
      throw new IllegalArgumentException("Unbalanced JSON object string: " + input);
    }

    String last = current.toString().trim();
    if (!last.isEmpty()) {
      pairs.add(last);
    }

    return pairs;
  }

  private static int indexOfTopLevelColon(String input) {
    String[] parts = input.split(":", 2);
    
    if (parts.length < 2) 
      return -1;
    
    return parts[0].length();
  }

  private static boolean isMatchingPair(char opening, char closing) {
    return (opening == '{' && closing == '}') || (opening == '[' && closing == ']');
  }
}
