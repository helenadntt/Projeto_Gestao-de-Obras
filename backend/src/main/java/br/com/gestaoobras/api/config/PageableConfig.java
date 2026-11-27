package br.com.gestaoobras.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.PageableHandlerMethodArgumentResolverCustomizer;

@Configuration
public class PageableConfig {

    @Bean
    public PageableHandlerMethodArgumentResolverCustomizer pageableCustomizer(
            @Value("${spring.data.web.pageable.max-page-size:100}") int maxPageSize
    ) {
        return resolver -> resolver.setMaxPageSize(maxPageSize);
    }
}
