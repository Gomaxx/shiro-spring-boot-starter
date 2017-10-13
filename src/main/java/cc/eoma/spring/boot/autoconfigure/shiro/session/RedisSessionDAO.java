package cc.eoma.spring.boot.autoconfigure.shiro.session;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.eis.AbstractSessionDAO;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;


/**
 * 参考： http://blog.ionio.cn/post/springmvc-shiro-session-redis.html
 */
public class RedisSessionDAO extends AbstractSessionDAO {

  @Resource(name = "redisTemplate")
  private RedisTemplate<String, Session> redisTemplate;
  private static final String SHIRO_CACHE_SESSION_KEY = "shiro-cache-session:";

  protected Serializable doCreate(Session session) {
    final Serializable sessionId = generateSessionId(session);
    assignSessionId(session, sessionId);
    redisTemplate.opsForValue().set(SHIRO_CACHE_SESSION_KEY + sessionId, session, 30, TimeUnit.MINUTES);
    return sessionId;
  }

  protected Session doReadSession(Serializable serializable) {
    return redisTemplate.opsForValue().get(SHIRO_CACHE_SESSION_KEY + serializable);
  }

  public void update(Session session) throws UnknownSessionException {
    redisTemplate.opsForValue()
        .set(SHIRO_CACHE_SESSION_KEY + session.getId(), session, 30, TimeUnit.MINUTES);
  }

  public void delete(Session session) {
    final Serializable sessionId = session.getId();
    redisTemplate.delete(SHIRO_CACHE_SESSION_KEY + sessionId);
  }

  public Collection<Session> getActiveSessions() {
    Set<String> keys = redisTemplate.keys(SHIRO_CACHE_SESSION_KEY.concat("*"));
    return redisTemplate.opsForValue().multiGet(keys);
  }
}
