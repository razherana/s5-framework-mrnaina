package mg.razherana.framework.web.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import mg.razherana.framework.web.exceptions.WebExecutionException;
import mg.razherana.framework.web.utils.json.types.JsonArray;
import mg.razherana.framework.web.utils.json.types.JsonElement;
import mg.razherana.framework.web.utils.json.types.JsonObject;
import mg.razherana.framework.web.utils.json.types.JsonType;

public class RequestBody {
  private static final String REQUEST_BODY_ATTRIBUTE = RequestBody.class.getName() + ".INSTANCE";
  private static final String ROOT_VALUE_KEY = "value";

  protected final Map<String, Object> data;
  protected JsonElement jsonElement;

  protected RequestBody(Map<String, Object> data, JsonElement jsonElement) {
    this.data = data;
    this.jsonElement = jsonElement;
  }

  public static RequestBody empty() {
    return new RequestBody(Collections.emptyMap(), null);
  }

  public static RequestBody from(HttpServletRequest request) {
    if (request == null) {
      return empty();
    }

    Object cached = request.getAttribute(REQUEST_BODY_ATTRIBUTE);
    if (cached instanceof RequestBody cachedBody) {
      return cachedBody;
    }

    Charset encoding = resolveEncoding(request);
    Map<String, Object> aggregated = new LinkedHashMap<>();

    mergeUrlEncoded(aggregated, request.getQueryString(), encoding, false);

    String rawBody = readBody(request);
    String contentType = normalizeContentType(request.getContentType());
    JsonElement parsedJson = null;

    boolean isMultipart = false;

    try {
      request.getParts();
      isMultipart = true;
    } catch (ServletException e) {
      isMultipart = false;
    } catch (IOException e) {
      throw new WebExecutionException("I/O error when reading parts", e);
    }

    if (rawBody != null && !rawBody.isBlank()) {
      if ("application/json".equals(contentType)) {
        parsedJson = parseJsonElement(rawBody);
        aggregated.putAll(jsonElementToBody(parsedJson));
      } else if (isMultipart) {
        try {
          var parts = new ArrayList<>(request.getParts());
          parts.forEach(e -> System.out.println(e.getContentType()));
        } catch (IOException | ServletException e) {
          throw new WebExecutionException("Error when reading parts", e);
        }
      } else if (contentType == null || contentType.isEmpty()
          || "application/x-www-form-urlencoded".equals(contentType)) {
        mergeUrlEncoded(aggregated, rawBody, encoding, true);
      }
    }

    RequestBody requestBody = new RequestBody(Collections.unmodifiableMap(aggregated), parsedJson);
    request.setAttribute(REQUEST_BODY_ATTRIBUTE, requestBody);
    return requestBody;
  }

  public Object get(String key) {
    return data.get(key);
  }

  public boolean containsKey(String key) {
    return data.containsKey(key);
  }

  public Map<String, Object> asMap() {
    return data;
  }

  public Optional<JsonElement> getJsonElement() {
    return Optional.ofNullable(jsonElement);
  }

  public boolean isEmpty() {
    return data.isEmpty();
  }

  private static Charset resolveEncoding(HttpServletRequest request) {
    String encoding = request.getCharacterEncoding();

    if (encoding == null || encoding.isBlank()) {
      return StandardCharsets.UTF_8;
    }

    try {
      return Charset.forName(encoding);
    } catch (Exception ex) {
      return StandardCharsets.UTF_8;
    }
  }

  private static String normalizeContentType(String contentType) {
    if (contentType == null || contentType.isBlank()) {
      return null;
    }

    int separatorIndex = contentType.indexOf(';');
    String baseContentType = separatorIndex >= 0 ? contentType.substring(0, separatorIndex) : contentType;

    return baseContentType.trim().toLowerCase(Locale.ROOT);
  }

  private static String readBody(HttpServletRequest request) {
    try (BufferedReader reader = request.getReader()) {
      StringBuilder builder = new StringBuilder();

      String line;
      while ((line = reader.readLine()) != null) {
        builder.append(line);
      }

      return builder.toString();
    } catch (IOException e) {
      throw new WebExecutionException("Error reading request body", e);
    }
  }

  private static void mergeUrlEncoded(Map<String, Object> target, String source, Charset encoding,
      boolean overrideExisting) {
    if (source == null || source.isBlank()) {
      return;
    }

    String[] pairs = source.split("&");

    for (String pair : pairs) {
      if (pair.isEmpty()) {
        continue;
      }

      String[] kv = pair.split("=", 2);

      if (kv[0].isEmpty()) {
        continue;
      }

      String decodedKey = decode(kv[0], encoding);
      String decodedValue = kv.length > 1 ? decode(kv[1], encoding) : "";

      boolean treatAsArray = decodedKey.endsWith("[]");

      if (treatAsArray) {
        decodedKey = decodedKey.substring(0, decodedKey.length() - 2);
      }

      Object existing = overrideExisting ? null : target.get(decodedKey);

      if (overrideExisting) {
        target.remove(decodedKey);
      }

      if (existing == null) {
        if (treatAsArray) {
          target.put(decodedKey, new String[] { decodedValue });
        } else {
          target.put(decodedKey, decodedValue);
        }
        continue;
      }

      if (existing instanceof String existingString) {
        target.put(decodedKey, new String[] { existingString, decodedValue });
        continue;
      }

      if (existing instanceof String[] existingArray) {
        String[] newValues = Arrays.copyOf(existingArray, existingArray.length + 1);
        newValues[newValues.length - 1] = decodedValue;
        target.put(decodedKey, newValues);
        continue;
      }

      target.put(decodedKey, decodedValue);
    }
  }

  private static JsonElement parseJsonElement(String rawBody) {
    try {
      return JsonElement.from(rawBody);
    } catch (Exception e) {
      throw new WebExecutionException("Invalid JSON request body", e);
    }
  }

  private static Map<String, Object> jsonElementToBody(JsonElement element) {
    if (element == null || element.getType() == null) {
      return Collections.emptyMap();
    }

    if (element.getType() == JsonType.OBJECT) {
      return convertJsonObject((JsonObject) element);
    }

    Map<String, Object> wrapper = new LinkedHashMap<>();
    wrapper.put(ROOT_VALUE_KEY, convertJsonElement(element));
    return wrapper;
  }

  private static Map<String, Object> convertJsonObject(JsonObject jsonObject) {
    Map<String, Object> result = new LinkedHashMap<>();

    jsonObject.getMembers().forEach((key, value) -> result.put(key, convertJsonElement(value)));

    return result;
  }

  private static Object convertJsonElement(JsonElement element) {
    if (element == null || element.getType() == null) {
      return null;
    }

    JsonType type = element.getType();

    if (type == JsonType.OBJECT) {
      return convertJsonObject((JsonObject) element);
    }

    if (type == JsonType.ARRAY) {
      return convertJsonArray((JsonArray) element);
    }

    if (type.isPrimitive()) {
      return element.getAsPrimitive();
    }

    if (type == JsonType.NULL) {
      return null;
    }

    throw new WebExecutionException("Unsupported JSON element type: " + type);
  }

  private static Object convertJsonArray(JsonArray array) {
    if (array == null || array.getElements() == null || array.getElements().isEmpty()) {
      return new String[0];
    }

    boolean allPrimitives = true;

    for (JsonElement element : array.getElements()) {
      if (element == null || !element.getType().isPrimitive()) {
        allPrimitives = false;
        break;
      }
    }

    if (allPrimitives) {
      String[] values = new String[array.getElements().size()];

      for (int i = 0; i < array.getElements().size(); i++) {
        Object primitiveValue = array.getElements().get(i).getAsPrimitive();
        values[i] = primitiveValue == null ? null : String.valueOf(primitiveValue);
      }

      return values;
    }

    Map<String, Object> nested = new LinkedHashMap<>();

    for (int i = 0; i < array.getElements().size(); i++) {
      nested.put(String.valueOf(i), convertJsonElement(array.getElements().get(i)));
    }

    return nested;
  }

  private static String decode(String value, Charset encoding) {
    try {
      return URLDecoder.decode(value, encoding.name());
    } catch (IllegalArgumentException e) {
      throw new WebExecutionException("Failed to decode URL-encoded value", e);
    } catch (UnsupportedEncodingException e) {
      // Should never happen as we use Charset
      throw new WebExecutionException("Unsupported encoding: " + encoding.name(), e);
    }
  }
}
