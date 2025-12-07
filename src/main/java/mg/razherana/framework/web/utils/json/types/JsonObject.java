package mg.razherana.framework.web.utils.json.types;

import java.util.HashMap;
import java.util.Map;

public class JsonObject extends JsonElement {
  private final Map<String, JsonElement> members = new HashMap<>();

  public void add(String key, JsonElement value) {
    members.put(key, value);
  }

  public JsonObject(Map<String, Object> map) {
    for (Map.Entry<String, Object> entry : map.entrySet()) {
      members.put(entry.getKey(), JsonElement.of(entry.getValue()));
    }
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("{");
    boolean first = true;

    for (Map.Entry<String, JsonElement> entry : members.entrySet()) {
      if (!first) {
        sb.append(",");
      }
      sb.append("\"").append(entry.getKey()).append("\":").append(entry.getValue().toString());
      first = false;
    }
    sb.append("}");
    return sb.toString();
  }

  @Override
  public JsonType getType() {
    return JsonType.OBJECT;
  }
}
