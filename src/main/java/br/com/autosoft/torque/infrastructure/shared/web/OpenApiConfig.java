package br.com.autosoft.torque.infrastructure.shared.web;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@Configuration
@OpenAPIDefinition(info = @Info(title = "Torque", description = "API da oficina mecânica"),
		security = @SecurityRequirement(name = OpenApiConfig.TOKEN))
@SecurityScheme(name = OpenApiConfig.TOKEN, type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
class OpenApiConfig {

	static final String TOKEN = "token";

}
