package mg.razherana.framework.web.utils.json.types;

public class JsonBoolean extends JsonElement {
  private final boolean value;

  public JsonBoolean(boolean value) {
    this.value = value;
  }

  public boolean getValue() {
    return value;
  }

  @Override
  public String toString() {
    return Boolean.toString(value);
  }

  @Override
  public JsonType getType() {
    return JsonType.BOOLEAN;
  }
  
}
