package mg.razherana.framework.web.utils.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;
import mg.razherana.framework.web.exceptions.WebExecutionException;
import mg.razherana.framework.web.utils.http.readers.DataToMap;
import mg.razherana.framework.web.utils.http.readers.UrlEncodedDataToMap;
import mg.razherana.framework.web.utils.json.types.JsonElement;

public class RequestBody {
  private static final String REQUEST_BODY_ATTRIBUTE = RequestBody.class.getName() + ".INSTANCE";
  private static final String ROOT_VALUE_KEY = "__root_value__";

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

    String normalizedContentType = normalizeContentType(request.getContentType());
    ContentType detectedContentType = ContentType.fromString(normalizedContentType);
    JsonElement parsedJson = null;

    Map<String, ?> bodyData = Collections.emptyMap();

    if (detectedContentType == ContentType.MULTIPART_FORM_DATA) {
      bodyData = DataToMap.convertToMap(ContentType.MULTIPART_FORM_DATA.getType(), null, request, encoding);
    } else {
      String rawBody = readBody(request);

      if (rawBody != null && !rawBody.isBlank()) {
        if (detectedContentType == ContentType.OTHER
            && (normalizedContentType == null || normalizedContentType.isBlank())) {
          detectedContentType = ContentType.APPLICATION_X_WWW_FORM_URLENCODED;
          normalizedContentType = detectedContentType.getType();
        }

        if (detectedContentType == ContentType.APPLICATION_JSON) {
          parsedJson = JsonElement.from(rawBody);
          bodyData = DataToMap.convertToMap(ContentType.APPLICATION_JSON.getType(), rawBody, request, encoding);
        } else if (detectedContentType == ContentType.APPLICATION_X_WWW_FORM_URLENCODED) {
          bodyData = DataToMap.convertToMap(ContentType.APPLICATION_X_WWW_FORM_URLENCODED.getType(), rawBody, request, encoding);
        } else if (detectedContentType == ContentType.TEXT_PLAIN) {
          aggregated.put(ROOT_VALUE_KEY, rawBody);
        }
      }
    }

    bodyData.forEach((key, value) -> {
      if (key != null) {
        aggregated.put(key, value);
      }
    });

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

  public Map<String, ?> asMap() {
    return data;
  }

  public Optional<JsonElement> getJsonElement() {
    return Optional.ofNullable(jsonElement);
  }

  public Optional<Object> getRootValue() {
    return Optional.ofNullable(data.get(ROOT_VALUE_KEY));
  }

  public JsonElement asJsonElement() {
    return JsonElement.of(asMap());
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

    Map<String, ?> data = new UrlEncodedDataToMap(encoding).toMap(source);

    data.forEach((key, value) -> {
      if (key != null) {
        if (overrideExisting || !target.containsKey(key)) {
          target.put(key, value);
        }
      }
    });
  }
}
