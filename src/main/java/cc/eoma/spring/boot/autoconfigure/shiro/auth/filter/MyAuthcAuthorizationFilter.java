package cc.eoma.spring.boot.autoconfigure.shiro.auth.filter;

import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

public class MyAuthcAuthorizationFilter extends FormAuthenticationFilter {
  private Logger log = LoggerFactory.getLogger(MyAuthcAuthorizationFilter.class);

  @Override
  protected boolean onAccessDenied(ServletRequest request, ServletResponse response)
      throws Exception {
    if (this.isLoginRequest(request, response)) {
      if (this.isLoginSubmission(request, response)) {
        if (log.isTraceEnabled()) {
          log.trace("Login submission detected.  Attempting to execute login.");
        }
        return this.executeLogin(request, response);
      } else {
        if (log.isTraceEnabled()) {
          log.trace("Login page view.");
        }
        return true;
      }
    } else {
      if (log.isTraceEnabled()) {
        log.trace(
            "Attempting to access a path which requires authentication.  Forwarding to the "
                + "Authentication url ["
                + this.getLoginUrl() + "]");
      }
      HttpServletRequest req = (HttpServletRequest) request;
      if ((req.getHeader("accept").contains("application/json") || (
          req.getHeader("X-Requested-With") != null && req.getHeader("X-Requested-With")
              .contains("XMLHttpRequest")))) {
        response.setContentType("application/json");
        PrintWriter writer = response.getWriter();
        writer.write("{\"success\":false,\"errorMessages\":[\"login\"]}");
        writer.close();
        return false;
      }

      this.saveRequestAndRedirectToLogin(request, response);
      return false;
    }
  }

}
