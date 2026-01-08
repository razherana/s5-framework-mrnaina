package mg.razherana.framework.web.utils.http;

public enum ContentType {
  APPLICATION_JSON("application/json"),
  APPLICATION_X_WWW_FORM_URLENCODED("application/x-www-form-urlencoded"),
  MULTIPART_FORM_DATA("multipart/form-data"),
  TEXT_PLAIN("text/plain"),
  OTHER("other");

  private final String type;

  ContentType(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }

  public static ContentType fromString(String contentType) {
    if (contentType == null) {
      return OTHER;
    }
    String lowerCased = contentType.toLowerCase();
    for (ContentType ct : values()) {
      if (lowerCased.equals(ct.type)) {
        return ct;
      }
    }
    return OTHER;
  }
}
