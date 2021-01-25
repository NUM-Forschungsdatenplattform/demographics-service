package de.vitagroup.num.configuration;

import java.util.function.Predicate;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.RequestHandler;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger.web.SecurityConfigurationBuilder;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
@AllArgsConstructor
public class SwaggerConfig {

  private static final String SEC_CONFIG_NAME = "oauth_setting";

  private final SwaggerProperties swaggerProperties;

  @Bean
  public Docket api() {
    return getDocket("Api", "/*.*");
  }

  @Bean
  public SecurityConfiguration security(SwaggerProperties properties) {
    return SecurityConfigurationBuilder.builder()
      .clientId(properties.getClientName())
      .clientSecret(properties.getClientSecret())
      .scopeSeparator(" ")
      .useBasicAuthenticationWithAccessCodeGrant(true)
      .build();
  }

  private Docket getDocket(String groupName, String pathRegexp, String... excludedBasePackage) {
    return new Docket(DocumentationType.SWAGGER_2)
      .groupName(groupName)
      .select()
      .apis(getRequestHandlerSelector(excludedBasePackage))
      .paths(PathSelectors.regex(pathRegexp))
      .build();
  }

  private Predicate<RequestHandler> getRequestHandlerSelector(String... excludedBasePackage) {
    if (excludedBasePackage.length > 0) {
      return Predicate.not(RequestHandlerSelectors.basePackage(excludedBasePackage[0]));
    }
    return RequestHandlerSelectors.any();
  }

}
