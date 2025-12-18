package mg.razherana.framework.web.utils.json.types;

public class JsonNumber extends JsonElement {
  private final Number value;

  public JsonNumber(Number value) {
    this.value = value;
  }

  public Number getValue() {
    return value;
  }

  @Override
  public String toString() {
    return value.toString();
  }

  @Override
  public JsonType getType() {
    return JsonType.NUMBER;
  }
}
