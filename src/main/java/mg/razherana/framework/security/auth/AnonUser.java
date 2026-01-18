package mg.razherana.framework.security.auth;

import java.util.Set;

final public class AnonUser implements AuthUser {
  @Override
  public Set<String> getRoles() {
    return Set.of();
  }

  @Override
  public String getUsername() {
    return "ANONYMOUS";
  }

  @Override
  public String getPassword() {
    return "";
  }
}
