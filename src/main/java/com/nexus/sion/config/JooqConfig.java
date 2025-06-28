package com.nexus.sion.config;

import org.jooq.conf.RenderNameStyle;
import org.jooq.conf.Settings;
import org.springframework.boot.autoconfigure.jooq.DefaultConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JooqConfig {

    @Bean
    public Settings jooqSettings() {
        return new Settings()
                .withRenderNameStyle(RenderNameStyle.AS_IS)
                .withRenderGroupConcatMaxLenSessionVariable(false)
                .withRenderFormatted(true);
    }

    @Bean
    public DefaultConfigurationCustomizer configurationCustomizer() {
        return configuration -> configuration.setSettings(jooqSettings());
    }
}
