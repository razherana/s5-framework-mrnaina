package mg.razherana.framework.web.utils.json.types;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class JsonString extends JsonElement {
  private final String value;

  final static Class<?>[] STRING_LIKE_TYPES = new Class<?>[] {
      String.class,
      LocalDate.class,
      LocalDateTime.class,
      LocalTime.class,
      java.util.Date.class,
      java.sql.Date.class,
      java.sql.Timestamp.class
  };

  public JsonString(String value) {
    this.value = value;
  }

  public static boolean shouldBeString(Object value) {
    if (value == null)
      return false;

    for (Class<?> type : STRING_LIKE_TYPES) {
      if (type.isInstance(value))
        return true;
    }

    return false;
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
