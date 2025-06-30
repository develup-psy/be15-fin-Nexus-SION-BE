package com.nexus.sion.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.nexus.sion.feature.squad.query.util.SqlLogInterceptor;

@Configuration
public class SpringConfig {
  @Bean
  public SqlLogInterceptor sqlLogInterceptor() {
    return new SqlLogInterceptor();
  }
}
