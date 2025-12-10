package mg.razherana.framework.web.utils.json.types;

import java.lang.reflect.Array;

import mg.razherana.framework.web.utils.json.JsonObjectBuilder;
import mg.razherana.framework.web.utils.json.JsonParser;

public abstract class JsonElement {
  public abstract String toString();

  public abstract JsonType getType();

  public static final JsonNull NULL = new JsonNull();

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
      return new JsonObject((java.util.Map<?, ?>) value);

    if (value instanceof java.util.List)
      return new JsonArray((java.util.List<?>) value);

    if (value.getClass().isArray()) {
      int len = Array.getLength(value);
      Object[] array = new Object[len];

      for (int i = 0; i < len; i++)
        array[i] = Array.get(value, i);

      return new JsonArray(java.util.Arrays.asList(array));
    }

    if (value instanceof Object)
      return JsonObjectBuilder.buildFromClassAttributes(value);

    // Should not reach here
    throw new IllegalArgumentException("Unsupported type: " + value.getClass().getName());
  }

  public static JsonElement from(String jsonString) {
    return JsonParser.parse(jsonString);
  }

  public Object getAsPrimitive() {
    if (!getType().isPrimitive()) {
      throw new IllegalStateException("JsonElement is not a primitive type");
    }
    if (getType() == JsonType.STRING)
      return ((JsonString) this).getValue();

    if (getType() == JsonType.NUMBER)
      return ((JsonNumber) this).getValue();

    if (getType() == JsonType.BOOLEAN)
      return ((JsonBoolean) this).getValue();

    if (getType() == JsonType.NULL)
      return null;

    throw new IllegalStateException("Unknown primitive type: " + getType());

  }
}
