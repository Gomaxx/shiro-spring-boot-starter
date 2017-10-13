package cc.eoma.spring.boot.autoconfigure.shiro;

import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.filter.mgt.FilterChainManager;
import org.apache.shiro.web.filter.mgt.FilterChainResolver;
import org.apache.shiro.web.filter.mgt.PathMatchingFilterChainResolver;
import org.apache.shiro.web.mgt.WebSecurityManager;
import org.apache.shiro.web.servlet.AbstractShiroFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanInitializationException;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

public class MyShiroFilterFactoryBean extends ShiroFilterFactoryBean {
  private static final transient Logger log = LoggerFactory.getLogger(ShiroFilterFactoryBean.class);
  // 对ShiroFilter来说，需要直接忽略的请求
  private Set<String> ignoreExt;

  public MyShiroFilterFactoryBean() {
    super();
    ignoreExt = new HashSet<String>();
    ignoreExt.add(".jpg");
    ignoreExt.add(".png");
    ignoreExt.add(".gif");
    ignoreExt.add(".bmp");
    ignoreExt.add(".js");
    ignoreExt.add(".css");
  }

  // 重写createInstance方法获取自定义ShiroFilter
  @Override
  protected AbstractShiroFilter createInstance() throws Exception {
    log.debug("Creating Shiro Filter instance.");
    SecurityManager securityManager = getSecurityManager();
    String msg;
    if (securityManager == null) {
      msg = "SecurityManager property must be set.";
      throw new BeanInitializationException(msg);
    } else if (!(securityManager instanceof WebSecurityManager)) {
      msg = "The security manager does not implement the WebSecurityManager interface.";
      throw new BeanInitializationException(msg);
    } else {
      FilterChainManager manager = this.createFilterChainManager();
      PathMatchingFilterChainResolver chainResolver = new PathMatchingFilterChainResolver();
      chainResolver.setFilterChainManager(manager);
      // modify zhis line.
      //      return new ShiroFilterFactoryBean.SpringShiroFilter((WebSecurityManager)
      // securityManager, chainResolver);
      return new MySpringShiroFilter((WebSecurityManager) securityManager, chainResolver);
    }
  }


  private final class MySpringShiroFilter extends AbstractShiroFilter {

    protected MySpringShiroFilter(
        WebSecurityManager webSecurityManager, FilterChainResolver resolver) {
      super();
      if (webSecurityManager == null) {
        throw new IllegalArgumentException("WebSecurityManager property cannot be null.");
      }
      setSecurityManager(webSecurityManager);
      if (resolver != null) {
        setFilterChainResolver(resolver);
      }
    }

    @Override
    protected void doFilterInternal(
        ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
        throws ServletException, IOException {
      HttpServletRequest request = (HttpServletRequest) servletRequest;
      String uri = request.getRequestURI().toLowerCase();
      boolean flag = true;
      int idx = uri.indexOf(".");
      if (idx > 0) {
        uri = uri.substring(idx);
        if (ignoreExt.contains(uri.toLowerCase())) {
          flag = false;
        }
      }
      if (flag) {
        super.doFilterInternal(servletRequest, servletResponse, chain);
      } else {
        chain.doFilter(servletRequest, servletResponse);
      }
    }
  }
}
