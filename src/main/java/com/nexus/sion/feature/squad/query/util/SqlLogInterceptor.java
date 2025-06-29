package com.nexus.sion.feature.squad.query.util;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.*;

import java.sql.Connection;
import java.util.Map;
import java.util.Properties;

@Intercepts({@Signature(
        type = StatementHandler.class,
        method = "prepare",
        args = {Connection.class, Integer.class}
)})
public class SqlLogInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        String sql = statementHandler.getBoundSql().getSql();
        Object param = statementHandler.getBoundSql().getParameterObject();

        System.out.println("[MyBatis SQL] ▶ " + sql);

        // 파라미터가 Map이면 키-값 출력
        if (param instanceof Map<?, ?> map) {
            System.out.println("[MyBatis Param] ▶ ");
            map.forEach((k, v) -> System.out.println("  - " + k + " = " + v));
        } else {
            System.out.println("[MyBatis Param] ▶ " + param);
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
