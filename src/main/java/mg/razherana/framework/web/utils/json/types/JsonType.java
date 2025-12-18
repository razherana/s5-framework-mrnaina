package mg.razherana.framework.web.utils.json.types;

public enum JsonType {
  OBJECT,
  ARRAY,
  STRING,
  NUMBER,
  BOOLEAN,
  NULL;

  public boolean isPrimitive() {
    return this == STRING || this == NUMBER || this == BOOLEAN || this == NULL;
  }
}
