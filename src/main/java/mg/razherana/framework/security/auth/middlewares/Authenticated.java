package mg.razherana.framework.security.auth.middlewares;

import mg.razherana.framework.security.auth.AbstractAuthGiver;
import mg.razherana.framework.security.auth.AnonUser;
import mg.razherana.framework.security.auth.AuthUser;
import mg.razherana.framework.security.auth.annotations.HasRole;
import mg.razherana.framework.security.auth.annotations.Roles;
import mg.razherana.framework.web.middlewares.Middleware;
import mg.razherana.framework.web.utils.proxies.annotations.Impl;

/**
 * Middleware to check if the user is authenticated.
 * Also checks the roles if @HasRole or @Roles annotations are present.
 */
abstract public class Authenticated extends Middleware {
  @Impl
  public Object before(AbstractAuthGiver authGiver, Roles rolesAnnotation) {
    AuthUser user = authGiver.extractUser();

    if (user != null && !(user instanceof AnonUser)) {
      // User is authenticated
      if (rolesAnnotation != null) {
        HasRole[] requiredRoles = rolesAnnotation.value();

        for (HasRole roleAnnotation : requiredRoles) {
          String requiredRole = roleAnnotation.value();

          if (!authGiver.hasRole(requiredRole)) {
            // User does not have the required role
            return authGiver.onRoleNotFoundError(requiredRole);
          }
        }
      }

      return null;
    }

    return authGiver.onError();
  }

  @Impl
  public Object after() {
    return null;
  }
}
