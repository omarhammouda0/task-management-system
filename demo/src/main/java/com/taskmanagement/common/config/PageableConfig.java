package com.taskmanagement.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Configuration for Spring Data Pageable defaults.
 * Sets the default sorting to "createdAt,desc" for all paginated endpoints.
 */
@Configuration
@EnableSpringDataWebSupport
public class PageableConfig implements WebMvcConfigurer {

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        PageableHandlerMethodArgumentResolver resolver = new PageableHandlerMethodArgumentResolver();


        resolver.setFallbackPageable(
                PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"))
        );


        resolver.setMaxPageSize(100);

        resolvers.add(resolver);
    }
}

