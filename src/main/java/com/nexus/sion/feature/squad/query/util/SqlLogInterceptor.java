package com.nexus.sion.feature.squad.query.util;

import java.sql.Connection;
import java.util.Map;
import java.util.Properties;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.*;

import lombok.extern.slf4j.Slf4j;

@Intercepts({
  @Signature(
      type = StatementHandler.class,
      method = "prepare",
      args = {Connection.class, Integer.class})
})
@Slf4j
public class SqlLogInterceptor implements Interceptor {

  @Override
  public Object intercept(Invocation invocation) throws Throwable {
    StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
    String sql = statementHandler.getBoundSql().getSql();
    Object param = statementHandler.getBoundSql().getParameterObject();

    log.debug("[MyBatis SQL] ▶ {}", sql);
    // 파라미터가 Map이면 키-값 출력
    if (param instanceof Map<?, ?> map) {
      log.debug("[MyBatis Param] ▶ ");
      map.forEach((k, v) -> log.debug("  - {} = {}", k, v));
    } else {
      log.debug("[MyBatis Param] ▶ {}", param);
    }

    return invocation.proceed();
  }

  @Override
  public Object plugin(Object target) {
    return Plugin.wrap(target, this);
  }

  @Override
  public void setProperties(Properties properties) {}
}
