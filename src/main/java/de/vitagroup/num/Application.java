package de.vitagroup.num;

import de.vitagroup.num.properties.HibernateProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;

@EnableConfigurationProperties({HibernateProperties.class})
@SpringBootApplication(exclude = {ElasticsearchRestClientAutoConfiguration.class})
public class Application extends SpringBootServletInitializer {

  @Autowired
  AutowireCapableBeanFactory beanFactory;

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  @Override
  protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
    return builder.sources(Application.class);
  }

  @Bean
  public ServletRegistrationBean hapiServletRegistration() {
    ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean();
    BaseJpaRestfulServer jpaRestfulServer = new BaseJpaRestfulServer();
    beanFactory.autowireBean(jpaRestfulServer);
    servletRegistrationBean.setServlet(jpaRestfulServer);
    servletRegistrationBean.addUrlMappings("/fhir/*");
    servletRegistrationBean.setLoadOnStartup(1);
    return servletRegistrationBean;
  }

}
