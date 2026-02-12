package mg.razherana.framework.web.utils.sessionflash;

import java.util.Map;

import mg.razherana.framework.web.middlewares.Middleware;
import mg.razherana.framework.web.utils.ModelView;
import mg.razherana.framework.web.utils.proxies.annotations.Impl;

public abstract class SessionFlashMiddleware extends Middleware {
  public static final String FLASH_SESSION_KEY = "session_flash";

  @Impl
  public Object before(ModelView mv) {
    // Set to the modelview the session flash messages
    Map<String, ?> flashMessages = mv.session(FLASH_SESSION_KEY);

    if (flashMessages == null)
      return null;

    flashMessages.forEach((key, value) -> {
      mv.attribute(key, value);
    });

    mv.session(FLASH_SESSION_KEY, null);
    
    return null;
  }

  @Impl
  public Object after(ModelView mv) {
    return null;
  }
}
