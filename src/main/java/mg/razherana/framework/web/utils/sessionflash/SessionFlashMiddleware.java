package mg.razherana.framework.web.utils.sessionflash;

import mg.razherana.framework.web.middlewares.Middleware;
import mg.razherana.framework.web.utils.proxies.annotations.Impl;

public abstract class SessionFlashMiddleware extends Middleware {
  @Impl
  public Object before() {
    return null;
  }
}
