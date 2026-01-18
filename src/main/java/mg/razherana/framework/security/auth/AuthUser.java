package mg.razherana.framework.security.auth;

import java.util.Set;

/**
 * Represents an user as an entity in the system.
 */
public interface AuthUser {
  /**
   * Get the username of this user.
   * 
   * @return
   */
  public String getUsername();

  /**
   * Get the password of this user.
   * 
   * @return
   */
  public String getPassword();

  /**
   * Get the roles associated with this user.
   */
  public Set<String> getRoles();
}
