package de.vitagroup.num;

import ca.uhn.fhir.jpa.subscription.channel.config.SubscriptionChannelConfig;
import ca.uhn.fhir.jpa.subscription.match.config.SubscriptionProcessorConfig;
import ca.uhn.fhir.jpa.subscription.match.config.WebsocketDispatcherConfig;
import ca.uhn.fhir.jpa.subscription.submit.config.SubscriptionSubmitterConfig;
import de.vitagroup.num.configuration.FhirTesterConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

@ServletComponentScan(basePackageClasses = {JpaRestfulServer.class})
@SpringBootApplication(exclude = {ElasticsearchRestClientAutoConfiguration.class})
@Import({SubscriptionSubmitterConfig.class, SubscriptionProcessorConfig.class, SubscriptionChannelConfig.class, WebsocketDispatcherConfig.class})
public class Application extends SpringBootServletInitializer {

  public static void main(String[] args) {
    System.setProperty("spring.batch.job.enabled", "false");
    SpringApplication.run(Application.class, args);
  }

  @Override
  protected SpringApplicationBuilder configure(
    SpringApplicationBuilder builder) {
    return builder.sources(Application.class);
  }

  @Autowired
  AutowireCapableBeanFactory beanFactory;

  @Bean
  public ServletRegistrationBean hapiServletRegistration() {
    ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean();
    JpaRestfulServer jpaRestfulServer = new JpaRestfulServer();
    beanFactory.autowireBean(jpaRestfulServer);
    servletRegistrationBean.setServlet(jpaRestfulServer);
    servletRegistrationBean.addUrlMappings("/fhir/*");
    servletRegistrationBean.setLoadOnStartup(1);

    return servletRegistrationBean;
  }

  @Bean
  public ServletRegistrationBean overlayRegistrationBean() {

    AnnotationConfigWebApplicationContext annotationConfigWebApplicationContext = new AnnotationConfigWebApplicationContext();
    annotationConfigWebApplicationContext.register(FhirTesterConfig.class);

    DispatcherServlet dispatcherServlet = new DispatcherServlet(
      annotationConfigWebApplicationContext);
    dispatcherServlet.setContextClass(AnnotationConfigWebApplicationContext.class);
    dispatcherServlet.setContextConfigLocation(FhirTesterConfig.class.getName());

    ServletRegistrationBean registrationBean = new ServletRegistrationBean();
    registrationBean.setServlet(dispatcherServlet);
    registrationBean.addUrlMappings("/*");
    registrationBean.setLoadOnStartup(1);
    return registrationBean;

  }
}
