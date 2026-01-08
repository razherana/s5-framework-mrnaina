package mg.razherana.framework.web.utils.http.readers;

import java.nio.charset.Charset;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import mg.razherana.framework.web.exceptions.WebExecutionException;
import mg.razherana.framework.web.utils.http.ContentType;

public interface DataToMap<T> {
  public static Map<String, ?> convertToMap(String contentType, String data, HttpServletRequest request, Charset encoding) {
    DataToMap<String> reader;

    try {
      request.getParts();

      // If no exception, it's multipart
      var partReader = new MultPartToMap();

      return partReader.toMap(request);
    } catch (ServletException e) {
      // Ignore
    } catch (Exception e) {
      throw new WebExecutionException(e);
    }

    switch (ContentType.fromString(contentType)) {
      case APPLICATION_JSON:
        reader = new JsonDataToMap();
        break;

      case APPLICATION_X_WWW_FORM_URLENCODED:
        reader = new UrlEncodedDataToMap(encoding);
        break;

      default:
        throw new IllegalArgumentException("Unsupported content type: " + contentType);
    }

    return reader.toMap(data);
  }

  Map<String, ?> toMap(T data);
}
