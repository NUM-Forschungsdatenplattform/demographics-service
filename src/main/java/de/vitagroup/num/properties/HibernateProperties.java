package de.vitagroup.num.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Data
@Configuration
@ConfigurationProperties(prefix = "spring.jpa.properties.hibernate")
public class HibernateProperties {

  private Properties properties = new Properties();

  private String searchModelMapping;
  private String hbm2ddlAuto;

  public Properties getProperties() {
    if (properties.isEmpty()) {
      properties.put("hibernate.hbm2ddl.auto", hbm2ddlAuto);
      properties.put("hibernate.search.model_mapping", searchModelMapping);
    }
    return properties;
  }
}
