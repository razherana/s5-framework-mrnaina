package mg.razherana.framework.web.utils.json.types;

import mg.razherana.framework.web.utils.json.JsonObjectBuilder;

public abstract class JsonElement {
  public abstract String toString();

  public abstract JsonType getType();

  public static final JsonNull NULL = new JsonNull();

  @SuppressWarnings("unchecked")
  public static JsonElement of(Object value) {
    if (value == null)
      return NULL;

    if (value instanceof JsonElement)
      return (JsonElement) value;

    if (value instanceof String)
      return new JsonString((String) value);

    if (value instanceof Boolean)
      return new JsonBoolean((Boolean) value);

    if (value instanceof Number)
      return new JsonNumber((Number) value);

    if (value instanceof java.util.Map)
      return new JsonObject((java.util.Map<String, Object>) value);

    if (value instanceof java.util.List)
      return new JsonArray((java.util.List<Object>) value);

    if (value instanceof Object[])
      return new JsonArray(java.util.Arrays.asList((Object[]) value));

    if (value instanceof Object)
      return JsonObjectBuilder.buildFromClassAttributes(value);

    throw new IllegalArgumentException("Unsupported type: " + value.getClass().getName());
  }
}
