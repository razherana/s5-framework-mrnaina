package mg.razherana.framework.web.utils.http.readers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import mg.razherana.framework.web.exceptions.WebExecutionException;
import mg.razherana.framework.web.utils.http.ContentType;
import mg.razherana.framework.web.utils.json.types.JsonElement;

public class MultPartToMap implements DataToMap<HttpServletRequest> {
  @Override
  public java.util.Map<String, ?> toMap(HttpServletRequest request) {
    Map<String, Object> result = new LinkedHashMap<>();

    // Loop parts
    try {
      for (Part part : request.getParts()) {
        Object value = part;
        String contentType = part.getContentType();

        if (contentType == null || contentType.isBlank() || contentType.equals(ContentType.TEXT_PLAIN.getType())) {
          // It's a form field
          byte[] bytes = part.getInputStream().readAllBytes();
          value = new String(bytes, "UTF-8");
        } else if (contentType.equals(ContentType.APPLICATION_JSON.getType())) {
          byte[] bytes = part.getInputStream().readAllBytes();
          value = new String(bytes, "UTF-8");

          value = JsonElement.from((String) value);
        } else if (contentType.equals(ContentType.APPLICATION_X_WWW_FORM_URLENCODED.getType())) {
          byte[] bytes = part.getInputStream().readAllBytes();
          value = new String(bytes, "UTF-8");

          value = new UrlEncodedDataToMap(StandardCharsets.UTF_8).toMap((String) value);
        }

        UrlEncodedDataToMap.insertIntoMap(result, part.getName(), value);
      }
    } catch (IOException | ServletException e) {
      throw new WebExecutionException("Error processing multipart data", e);
    }

    return result;
  }
}
