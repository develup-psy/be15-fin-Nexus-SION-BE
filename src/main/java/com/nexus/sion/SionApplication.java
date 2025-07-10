package com.nexus.sion;

import com.nexus.sion.config.RestTemplateConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({RestTemplateConfig.class})
public class SionApplication {

  public static void main(String[] args) {
    SpringApplication.run(SionApplication.class, args);
  }
}
