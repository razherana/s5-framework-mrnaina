package mg.razherana.framework.web.utils.json.types;

import java.util.List;

public class JsonArray extends JsonElement {

  private final List<JsonElement> elements = new java.util.ArrayList<>();

  public JsonArray(List<Object> values) {
    for (Object value : values) {
      elements.add(JsonElement.of(value));
    }
  }

  public void add(JsonElement element) {
    elements.add(element);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    for (int i = 0; i < elements.size(); i++) {
      sb.append(elements.get(i).toString());
      if (i < elements.size() - 1) {
        sb.append(",");
      }
    }
    sb.append("]");
    return sb.toString();
  }

  @Override
  public JsonType getType() {
    return JsonType.ARRAY;
  }

}
