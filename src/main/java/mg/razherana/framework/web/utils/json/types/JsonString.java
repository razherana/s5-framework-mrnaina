package mg.razherana.framework.web.utils.json.types;

public class JsonString extends JsonElement {
  private final String value;

  public JsonString(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return "\"" + value.replace("\"", "\\\"") + "\"";
  }

  @Override
  public JsonType getType() {
    return JsonType.STRING;
  }

}
