package cc.eoma.spring.boot.autoconfigure.shiro;

import java.util.Map;

public interface FilterChainDefinitioner {
  Map<String, String> getFilterChainDefinitionMap();
}
