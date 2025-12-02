package mg.razherana.framework.web.utils.validation;

public record ValidationResult(
    boolean valid,
    String message) {
  public static final ValidationResult SUCCESS(String message) {
    return new ValidationResult(true, message);
  }
}
