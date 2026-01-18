package mg.razherana.framework.security.auth;

import mg.razherana.framework.web.givers.Giver;
import mg.razherana.framework.web.utils.proxies.annotations.Resolve;

abstract public class AbstractAuthGiver implements Giver {

  protected static final String EXTRACT_USER = "auth_extractUser";
  protected static final String LOGIN = "auth_login";
  protected static final String ON_ERROR = "auth_onerror";
  protected static final String ON_ROLE_NOT_FOUND_ERROR = "auth_onRoleNotFoundError";

  /**
   * Login method to authenticate a user.
   * This method uses an @Impl method to perform the login.
   * 
   * @return
   */
  @Resolve(LOGIN)
  public abstract void login();

  /**
   * Extract the user from the request.
   * This method uses an @Impl method to extract the user.
   * 
   * @return
   */
  @Resolve(EXTRACT_USER)
  public abstract AuthUser extractUser();

  /**
   * Handle error during authentication process.
   * It is used in Auth middlewares.
   * This method uses an @Impl method to handle the error.
   * 
   * @return
   */
  @Resolve(ON_ERROR)
  public abstract Object onError();

  /**
   * Handle role not found error during authentication process.
   * It is used in Auth middlewares.
   * 
   * @return
   */
  public abstract Object onRoleNotFoundError(String missingRole);

  /**
   * Check if the current user has a specific role.
   * 
   * @param role
   * @return
   */
  public boolean hasRole(String role) {
    AuthUser user = extractUser();
    if (user != null) {
      return user.getRoles().contains(role);
    }
    return false;
  }
}
