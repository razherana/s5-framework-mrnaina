package mg.razherana.framework.web.utils.json.types;

public class JsonNull extends JsonElement {
  @Override
  public String toString() {
    return "null";
  }

  @Override
  public JsonType getType() {
    return JsonType.NULL;
  }
}
