package com.nexus.sion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.nexus.sion.config.RestTemplateConfig;

@SpringBootApplication
@EnableScheduling
@Import({RestTemplateConfig.class})
public class SionApplication {

  public static void main(String[] args) {
    SpringApplication.run(SionApplication.class, args);
  }
}
