package cc.eoma.spring.boot.autoconfigure.shiro;

import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.web.filter.DelegatingFilterProxy;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;

import cc.eoma.spring.boot.autoconfigure.shiro.auth.filter.MyAuthcAuthorizationFilter;
import cc.eoma.spring.boot.autoconfigure.shiro.auth.filter.MyPermissionsAuthorizationFilter;
import cc.eoma.spring.boot.autoconfigure.shiro.cache.RedisCacheManager;
import cc.eoma.spring.boot.autoconfigure.shiro.session.MyDefaultWebSessionManager;
import cc.eoma.spring.boot.autoconfigure.shiro.session.RedisSessionDAO;

@Configuration
@ConditionalOnClass({ShiroFilterFactoryBean.class})
@EnableConfigurationProperties(ShiroProperties.class)
public class ShiroAutoConfiguration {


  //  /**
  //   * shiro auto configuration 中 采用下列方法是无法注入的（因为shiro 依赖
  //   * lifecycleBeanPostProcessor，而lifecycleBeanPostProcessor是保证开启Shiro注解的Spring配置方式的beans,
  //   * 在lifecycleBeanPostProcessor之后运行）
  //   */
  //  @Resource
  //  private ShiroProperties shiroProperties;

  /**
   * 手动注册ShiroFilter
   * 集成Shiro有2种方法：
   * 1. 按这个方法自己组装一个FilterRegistrationBean（这种方法更为灵活，可以自己定义UrlPattern，在项目使用中你可能会因为一些很但疼的问题最后采用它，
   * 想使用它你可能需要看官网或者已经很了解Shiro的处理原理了）
   * 2. 直接使用ShiroFilterFactoryBean（这种方法比较简单，其内部对ShiroFilter做了组装工作，无法自己定义UrlPattern，默认拦截 /*）
   */
  @Bean
  public FilterRegistrationBean filterRegistrationBean() {
    FilterRegistrationBean filterRegistration = new FilterRegistrationBean();

    filterRegistration.setFilter(new DelegatingFilterProxy("shiroFilter"));
    //  该值缺省为false,表示生命周期由SpringApplicationContext管理,设置为true则表示由ServletContainer管理
    filterRegistration.addInitParameter("targetFilterLifecycle", "true");
    filterRegistration.setEnabled(true);
    filterRegistration.addUrlPatterns("/*");// 可以自己灵活的定义很多，避免一些根本不需要被Shiro处理的请求被包含进来
    filterRegistration.setOrder(Integer.MIN_VALUE + 11000); // 设置过滤器加载顺序
    return filterRegistration;
  }

  @Bean(name = "shiroFilter")
  @DependsOn(value = {"lifecycleBeanPostProcessor", "securityManager"})
  public ShiroFilterFactoryBean getShiroFilterFactoryBean(
      DefaultWebSecurityManager securityManager, ShiroProperties shiroProperties) {

    // 通常ShiroFilter UrlPatterm会设置成"/*"，这样会导致拦截所有请求（包括css、js等），然而我们并不需要，这是需要重写shiroFilter
    //    ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
    ShiroFilterFactoryBean shiroFilterFactoryBean = new MyShiroFilterFactoryBean();
    shiroFilterFactoryBean.setSecurityManager(securityManager);
    shiroFilterFactoryBean.setLoginUrl(shiroProperties.getLoginUrl());
    shiroFilterFactoryBean.setSuccessUrl(shiroProperties.getSuccessUrl());
    shiroFilterFactoryBean.setUnauthorizedUrl(shiroProperties.getUnauthorizedUrl());

    Map<String, Filter> filters = new HashMap<>();
    /**
     * 重写 authc、perms filter，支持ajax请求返回JSON数据。
     * ==== shiro 默认 filters： http://shiro.apache.org/web.html#Web-defaultfilters
     */
    filters.put("authc", new MyAuthcAuthorizationFilter());
    filters.put("perms", new MyPermissionsAuthorizationFilter());
    shiroFilterFactoryBean.setFilters(filters);

    Map<String, String> filterChainDefinitionMap = new HashMap<>();
    /**
     * shiro filter definition map setting.
     *
     * 格式如下：
     *    filterChainDefinitionMap.put("/**", "anon");
     *    filterChainDefinitionMap.put("/**","authc");
     *    filterChainDefinitionMap.put("/static/**", "anno");
     *
     * 此处采用两种方法： 1.appliction配置文件中配置，2.配置filgerChainDefinitioner类重写getFilterChainDefinitionMap方法来获取。
     */

    if (shiroProperties.getFilterChainDefinitionMap() != null) {
      filterChainDefinitionMap.putAll(shiroProperties.getFilterChainDefinitionMap());
    }

    /**
     * 可以从数据库中获取菜单拦截信息
     * -
     * ==== 如果采用DubboConsumer时，在这里是无法注入的，因为此时DubboConsumer代理尚未生成，
     * ======== 解决方法见 ShiroFilterChainAutoConfiguration（参考：@jun.zhao）
     */
    shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);

    return shiroFilterFactoryBean;
  }

  @Bean(name = "securityManager")
  @DependsOn({"sessionManager", "redisCacheManager"})
  public DefaultWebSecurityManager getDefaultWebSecurityManager(
      SessionManager sessionManager, CacheManager cacheManager, ShiroProperties shiroProperties) {
    DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();

    if (shiroProperties.getRealm() != null) {
      AuthorizingRealm realm = BeanUtils.instantiate(shiroProperties.getRealm());
      securityManager.setRealm(realm);
    }
    securityManager.setSessionManager(sessionManager);
    securityManager.setCacheManager(cacheManager);
    return securityManager;
  }

  @Bean(name = "sessionManager")
  public DefaultWebSessionManager getDefaultWebSessionManager(ShiroProperties shiroProperties) {
    MyDefaultWebSessionManager sessionManager = new MyDefaultWebSessionManager();
    sessionManager.setSessionDAO(this.getRedisSessionDAO());
    sessionManager.setSessionIdCookieEnabled(true);
    sessionManager.setSessionIdCookie(sessionCookie(shiroProperties));
    return sessionManager;
  }

  @Bean
  public RedisSessionDAO getRedisSessionDAO() {
    return new RedisSessionDAO();
  }

  private SimpleCookie sessionCookie(ShiroProperties shiroProperties) {
    final SimpleCookie sessionCookie = new SimpleCookie();
    sessionCookie.setName(shiroProperties.getSessionName());
    sessionCookie.setPath(shiroProperties.getSessionPath());
    sessionCookie.setHttpOnly(shiroProperties.getSessionHttpOnly());
    sessionCookie.setMaxAge(shiroProperties.getSessionMaxAge());
    return sessionCookie;
  }

  @Bean("redisCacheManager")
  public CacheManager getRedisCacheManager() {
    return new RedisCacheManager();
  }

  /**
   * 开启Shiro注解的Spring配置方式的beans。在lifecycleBeanPostProcessor之后运行
   */
  @Bean(name = "lifecycleBeanPostProcessor")
  public LifecycleBeanPostProcessor getLifecycleBeanPostProcessor() {
    return new LifecycleBeanPostProcessor();
  }

  /**
   * 开启Shiro的注解(如@RequiresRoles,@RequiresPermissions),需借助SpringAOP扫描使用Shiro注解的类,并在必要时进行安全逻辑验证
   * 配置以下两个bean即可实现此功能
   * Enable Shiro Annotations for Spring-configured beans. Only run after the
   * lifecycleBeanProcessor has run
   * 由于本例中并未使用Shiro注解,故注释掉这两个bean(个人觉得将权限通过注解的方式硬编码在程序中,查看起来不是很方便,没必要使用)
   **/
  @Bean
  @ConditionalOnMissingBean
  @DependsOn("lifecycleBeanPostProcessor")
  public DefaultAdvisorAutoProxyCreator getDefaultAdvisorAutoProxyCreator() {
    DefaultAdvisorAutoProxyCreator daap = new DefaultAdvisorAutoProxyCreator();
    daap.setProxyTargetClass(true);
    return daap;
  }

  @Bean
  @ConditionalOnMissingBean
  @DependsOn("securityManager")
  public AuthorizationAttributeSourceAdvisor getAuthorizationAttributeSourceAdvisor(
      DefaultWebSecurityManager securityManager) {
    AuthorizationAttributeSourceAdvisor aasa = new AuthorizationAttributeSourceAdvisor();
    aasa.setSecurityManager(securityManager);
    return aasa;
  }
}
