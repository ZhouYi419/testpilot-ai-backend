package com.zy.testpilotai.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    /**
     * 注册全局 CORS 过滤器。
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        /*
         * 允许的前端来源。
         */
        config.addAllowedOriginPattern("http://localhost:*");
        config.addAllowedOriginPattern("http://127.0.0.1:*");

        /*
         * 是否允许携带 Cookie / Authorization 等认证信息。
         */
        config.setAllowCredentials(true);

        /*
         * 允许所有请求头。
         */
        config.addAllowedHeader("*");

        /*
         * 允许所有请求方法。
         * 包括 GET / POST / PUT / DELETE / OPTIONS 等。
         */
        config.addAllowedMethod("*");

        /*
         * 暴露给前端可读取的响应头。
         */
        config.addExposedHeader("Content-Disposition");
        config.addExposedHeader("Authorization");

        /*
         * 预检请求缓存时间，单位秒。
         */
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        /*
         * 对所有接口生效。
         */
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}