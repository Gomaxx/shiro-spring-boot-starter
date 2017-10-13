package cc.eoma.spring.boot.autoconfigure.shiro.cache;

import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;

/**
 * 参考： http://blog.ionio.cn/post/springmvc-shiro-session-redis.html
 */
public class RedisCacheManager implements CacheManager {

  @Resource(name = "redisTemplate")
  private RedisTemplate<String, ?> redisTemplate;


  public <K, V> Cache<K, V> getCache(String name) throws CacheException {
    return new RedisCache<K, V>(name, redisTemplate);
  }
}
  