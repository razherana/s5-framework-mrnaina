package mg.razherana.framework.web.utils.http.readers;

import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class UrlEncodedDataToMap implements DataToMap<String> {
  private final Charset encoding;

  public UrlEncodedDataToMap(Charset encoding) {
    this.encoding = encoding;
  }

  private String decode(String value) {
    try {
      return URLDecoder.decode(value, encoding);
    } catch (Exception e) {
      throw new IllegalArgumentException("Error decoding value: " + value, e);
    }
  }

  @Override
  public Map<String, ?> toMap(String data) {
    Map<String, Object> result = new LinkedHashMap<>();

    if (data == null || data.isEmpty())
      return result;

    String[] pairs = data.split("&");
    for (String pair : pairs) {
      String[] keyValue = pair.split("=", 2);
      String key = decode(keyValue[0]);
      String value = keyValue.length > 1 ? decode(keyValue[1]) : "";

      // Use the insertIntoMap method instead of direct put
      insertIntoMap(result, key, value);
    }

    return result;
  }

  @SuppressWarnings("unchecked")
  static void insertIntoMap(Map<String, Object> map, String key, Object value) {
    // First tokenize the key by dots
    String[] parts = key.split("\\.");
    Map<String, Object> currentMap = map;

    for (int i = 0; i < parts.length; i++) {
      String part = parts[i];

      // Check for array notation
      if (part.contains("[")) {
        // Get the base key name (everything before the first bracket)
        String baseKey = part.substring(0, part.indexOf('['));

        // Parse all array indices
        List<Integer> indices = parseArrayIndices(part);

        // Create the initial list if needed
        if (!currentMap.containsKey(baseKey)) {
          currentMap.put(baseKey, new ArrayList<>());
        }

        ArrayList<Object> list = (ArrayList<Object>) currentMap.get(baseKey);

        // Navigate through nested arrays using the indices
        Object target = list;
        for (int j = 0; j < indices.size(); j++) {
          int currentIndex = indices.get(j);

          // Ensure the list is large enough
          ArrayList<Object> currentList = (ArrayList<Object>) target;

          // Ensure list size
          while (currentList.size() <= currentIndex)
            currentList.add(null);

          // If this is the last array index and we're at the last part of the key
          if (j == indices.size() - 1 && i == parts.length - 1) {
            currentList.set(currentIndex, value);
            return;
          }

          // If this is the last array index but not the last part of the key
          else if (j == indices.size() - 1 && i < parts.length - 1) {
            if (currentList.get(currentIndex) == null)
              currentList.set(currentIndex, new LinkedHashMap<String, Object>());

            target = currentList.get(currentIndex);
          }
          // If there are more array indices to process
          else {
            if (currentList.get(currentIndex) == null)
              currentList.set(currentIndex, new ArrayList<>());

            target = currentList.get(currentIndex);
          }
        }

        // If we processed all indices, continue with the next part
        if (i < parts.length - 1) {
          currentMap = (Map<String, Object>) target;
        }
      } else {
        // Regular (non-array) key
        if (i == parts.length - 1) {
          currentMap.put(part, value);
        } else {
          if (!currentMap.containsKey(part) || !(currentMap.get(part) instanceof Map))
            currentMap.put(part, new LinkedHashMap<String, Object>());

          currentMap = (Map<String, Object>) currentMap.get(part);
        }
      }
    }
  }

  private static List<Integer> parseArrayIndices(String keyPart) {
    List<Integer> indices = new ArrayList<>();
    int start = keyPart.indexOf('[');
    while (start != -1) {
      int end = keyPart.indexOf(']', start);
      if (end == -1)
        throw new IllegalArgumentException("Malformed key: missing closing bracket");

      String indexStr = keyPart.substring(start + 1, end);
      try {
        indices.add(Integer.parseInt(indexStr));
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException("Malformed key: invalid array index");
      }

      start = keyPart.indexOf('[', end);
    }
    return indices;
  }
}