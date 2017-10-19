# shiro-spring-boot-starter

 spring boot shiro redis 支持
 
 JDK支持1.8或1.8+

 在修改源码前，请导入googlestyle-java.xml以保证一致的代码格式

## 如何使用shiro-spring-boot-starter:
* 添加POM依赖:
````xml
    <dependency>
      <groupId>cc.eoma</groupId>
      <artifactId>shiro-spring-boot-starter</artifactId>
      <version>1.0-SNAPSHOT</version>
    </dependency>
````
* 在 application.yml 添加相关配置信息(redisTemplate,shiro),样例配置如下:
````yaml
    spring:
      redis:
         database: 0
         host: localhost
         pool:
           max-active: 9
           max-idle: 9
           max-wait: -1
           min-idle: 0
         port: 6379
         timeout: 30000000
    
    shiro:
      loginUrl: "login"
      unauthorized-url: "unauthorized"
      success-url: "success"
      realm: com.eoma.unknown.MyRealm
      filter-chain-definition-map:
       "/**": "authc"
       "/aaa": "anon"
       "/user/login": "anon"
      filter-chain-definitioner: com.eoma.unknown.MyDefaultShiroFilterChainDefinitioner
````

* 编写 realm 类，集成 AuthorizingRealm，如下：
````java
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import java.util.ArrayList;
import java.util.List;

public class MyRealm extends AuthorizingRealm {
  @Override
  protected AuthorizationInfo doGetAuthorizationInfo(
      PrincipalCollection principalCollection) {
    List<String> roles = new ArrayList<>();
    List<String> resources = new ArrayList<>();
    SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
    info.addRoles(roles);
    info.addStringPermissions(resources);
    return info;
  }

  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(
      AuthenticationToken authenticationToken) throws AuthenticationException {
    UsernamePasswordToken usernamePasswordToken = (UsernamePasswordToken) authenticationToken;
    String username = usernamePasswordToken.getUsername();
    return new SimpleAuthenticationInfo(username, usernamePasswordToken.getPassword(), getName());
  }
}

````
* 编写 filter-chain-definitioner 继承 DefaultFilterChainDefinitioner ，如下：
````java
import com.alibaba.boot.dubbo.annotation.DubboConsumer;
import com.alibaba.fastjson.JSON;
import com.camelotchina.unknown.common.ExecuteResult;
import com.camelotchina.unknown.user.domain.User;
import com.camelotchina.unknown.user.service.UserService;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Resource;
import cc.eoma.spring.boot.autoconfigure.shiro.DefaultFilterChainDefinitioner;

@Component
public class MyDefaultShiroFilterChainDefinitioner extends
    DefaultFilterChainDefinitioner {
  @DubboConsumer
  private UserService userService;

  @Override
  public Map<String, String> getFilterChainDefinitionMap() {
    Map<String, String> map = new HashMap<>();
    map.put("/goma", "perms['xxxx']");
    ExecuteResult<User> er = this.userService.findByUserName("admin");
    System.out.println(JSON.toJSONString(er));
    return map;
  }
}

````