package org.nikolait.assigment.userdeposit.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Configuration
@SecurityScheme(
        name = AUTHORIZATION,
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenAPIConfig {

    @Value("${spring.application.name}")
    private String appName;

    @Value("${app.version}")
    private String appVersion;

    @Value("${app.license.name}")
    private String licenseName;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info().title("%s API".formatted(appName))
                        .description("REST API for " + appName)
                        .version(appVersion)
                        .license(new License().name(licenseName)));
    }
}
