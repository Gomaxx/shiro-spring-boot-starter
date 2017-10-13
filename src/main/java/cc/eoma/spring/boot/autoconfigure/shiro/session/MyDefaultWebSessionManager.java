package cc.eoma.spring.boot.autoconfigure.shiro.session;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.SessionKey;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.apache.shiro.web.session.mgt.WebSessionKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

import javax.servlet.ServletRequest;

public class MyDefaultWebSessionManager extends DefaultWebSessionManager {
  private Logger log = LoggerFactory.getLogger(MyDefaultWebSessionManager.class);

  @Override
  protected Session retrieveSession(SessionKey sessionKey) {
    Serializable sessionId = this.getSessionId(sessionKey);
    if (sessionId == null) {
      log.debug(
          "Unable to resolve session ID from SessionKey [{}].  Returning null to indicate a "
              + "session could not be found.",
          sessionKey);
      return null;
    }
    // ***************Add By Goma****************
    ServletRequest request = null;
    if (sessionKey instanceof WebSessionKey) {
      request = ((WebSessionKey) sessionKey).getServletRequest();
    }
    if (request != null) {
      Object s = request.getAttribute(sessionId.toString());
      if (s != null) {
        return (Session) s;
      }
    }
    // ***************Add By Goma****************
    Session s = this.retrieveSessionFromDataSource(sessionId);
    if (s == null) {
      String msg = "Could not find session with ID [" + sessionId + "]";
      throw new UnknownSessionException(msg);
    }
    // ***************Add By Goma****************
    if (request != null) {
      request.setAttribute(sessionId.toString(), s);
    }
    // ***************Add By Goma****************
    return s;
  }
}
