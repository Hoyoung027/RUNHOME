package com.l2kb.runhome.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("RunHome API")
                        .description("KBO 팬을 위한 개인화 webOS 홈화면 서비스 API")
                        .version("v1.0.0"));
    }
}
