package cc.eoma.spring.boot.autoconfigure.shiro;

import org.apache.shiro.realm.AuthorizingRealm;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "shiro")
public class ShiroProperties {
  private String loginUrl = "login";
  private String successUrl = "success";
  private String unauthorizedUrl = "unauthorized";
  private String sessionName = "shiro-session-id";
  private String sessionPath = "/";
  private Boolean sessionHttpOnly = true;
  private Integer sessionMaxAge = -1;
  private Class<? extends AuthorizingRealm> realm;
  private Map<String, String> filterChainDefinitionMap;
  private Class<? extends DefaultFilterChainDefinitioner> filterChainDefinitioner;

  public String getLoginUrl() {
    return loginUrl;
  }

  public void setLoginUrl(String loginUrl) {
    this.loginUrl = loginUrl;
  }

  public String getSuccessUrl() {
    return successUrl;
  }

  public void setSuccessUrl(String successUrl) {
    this.successUrl = successUrl;
  }

  public String getUnauthorizedUrl() {
    return unauthorizedUrl;
  }

  public void setUnauthorizedUrl(String unauthorizedUrl) {
    this.unauthorizedUrl = unauthorizedUrl;
  }

  public Class<? extends AuthorizingRealm> getRealm() {
    return realm;
  }

  public void setRealm(Class<? extends AuthorizingRealm> realm) {
    this.realm = realm;
  }

  public String getSessionName() {
    return sessionName;
  }

  public void setSessionName(String sessionName) {
    this.sessionName = sessionName;
  }

  public String getSessionPath() {
    return sessionPath;
  }

  public void setSessionPath(String sessionPath) {
    this.sessionPath = sessionPath;
  }

  public Boolean getSessionHttpOnly() {
    return sessionHttpOnly;
  }

  public void setSessionHttpOnly(Boolean sessionHttpOnly) {
    this.sessionHttpOnly = sessionHttpOnly;
  }

  public Integer getSessionMaxAge() {
    return sessionMaxAge;
  }

  public void setSessionMaxAge(Integer sessionMaxAge) {
    this.sessionMaxAge = sessionMaxAge;
  }

  public Map<String, String> getFilterChainDefinitionMap() {
    return filterChainDefinitionMap;
  }

  public void setFilterChainDefinitionMap(Map<String, String> filterChainDefinitionMap) {
    this.filterChainDefinitionMap = filterChainDefinitionMap;
  }

  public Class<? extends DefaultFilterChainDefinitioner> getFilterChainDefinitioner() {
    return filterChainDefinitioner;
  }

  public void setFilterChainDefinitioner(
      Class<? extends DefaultFilterChainDefinitioner> filterChainDefinitioner) {
    this.filterChainDefinitioner = filterChainDefinitioner;
  }
}
