package cc.eoma.spring.boot.autoconfigure.shiro.cache;

import org.apache.shiro.cache.Cache;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class RedisCache<K, V> implements Cache<K, V> {

  private RedisTemplate<K, V> redisTemplate;

  private static final String SHIRO_CACHE_KEY = "shiro-cache:";
  private String cacheKey;

  @SuppressWarnings({"rawtypes", "unchecked"})
  RedisCache(String name, RedisTemplate redisTemplate) {
    this.cacheKey = SHIRO_CACHE_KEY.concat(name).concat(":");
    this.redisTemplate = redisTemplate;
  }

  public V get(K key) {
    long globExpire = (long) 60;
    redisTemplate.boundValueOps(getCacheKey(key)).expire(globExpire, TimeUnit.MINUTES);
    return redisTemplate.boundValueOps(getCacheKey(key)).get();
  }

  public V put(K key, V value) {
    V old = get(key);
    redisTemplate.boundValueOps(getCacheKey(key)).set(value);
    return old;
  }

  public V remove(K key) {
    V old = get(key);
    redisTemplate.delete(getCacheKey(key));
    return old;
  }

  public void clear() {
    redisTemplate.delete(keys());
  }

  public int size() {
    return keys().size();
  }

  @SuppressWarnings("unchecked")
  private K getCacheKey(Object k) {
    return (K) (this.cacheKey + k);
  }

  public Set<K> keys() {
    return redisTemplate.keys(getCacheKey("*"));
  }

  @SuppressWarnings({"unchecked"})
  public Collection<V> values() {
    return (Collection<V>) this.redisTemplate.opsForHash().values((K) SHIRO_CACHE_KEY);
  }

}
