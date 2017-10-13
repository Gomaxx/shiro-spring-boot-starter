package cc.eoma.spring.boot.autoconfigure.shiro.auth.filter;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authz.PermissionsAuthorizationFilter;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

public class MyPermissionsAuthorizationFilter extends PermissionsAuthorizationFilter {
  @Override
  public boolean isAccessAllowed(
      ServletRequest request, ServletResponse response, Object mappedValue) throws
      IOException {
    Subject subject = this.getSubject(request, response);
    String[] perms = (String[]) ((String[]) mappedValue);
    boolean isPermitted = true;
    if (perms != null && perms.length > 0) {
      if (perms.length == 1) {
        if (!subject.isPermitted(perms[0])) {
          isPermitted = false;
        }
      } else if (!subject.isPermittedAll(perms)) {
        isPermitted = false;
      }
    }

    HttpServletRequest req = (HttpServletRequest) request;
    if (!isPermitted && (req.getHeader("accept").contains("application/json") || (
        req.getHeader("X-Requested-With") != null && req.getHeader("X-Requested-With")
            .contains("XMLHttpRequest")))) {
      response.setContentType("application/json");
      PrintWriter writer = response.getWriter();
      writer.write("{\"success\":false,\"errorMessages\":[\"Subject does not have permission\"]}");
      writer.close();
    }


    return isPermitted;
  }
}
