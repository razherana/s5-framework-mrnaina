package mg.razherana.framework.web.utils.http;

import java.util.LinkedHashMap;
import java.util.Map;

import mg.razherana.framework.web.utils.json.types.JsonElement;

public class ResponseBody extends RequestBody {

  public ResponseBody() {
    super(new LinkedHashMap<>(), null);
  }

  public ResponseBody put(String key, Object value) {
    data.put(key, value);
    return this;
  }

  public ResponseBody json(JsonElement element) {
    this.jsonElement = element;
    return this;
  }

  public JsonElement toJsonElement() {
    if (jsonElement != null) {
      return jsonElement;
    }

    Map<String, Object> snapshot = new LinkedHashMap<>(data);
    return JsonElement.of(snapshot);
  }
}
