package de.vitagroup.num.configuration;

import de.vitagroup.num.properties.AuditProperties;
import java.io.IOException;
import java.io.Writer;
import java.time.format.DateTimeFormatterBuilder;
import lombok.RequiredArgsConstructor;
import org.jdom2.Element;
import org.openehealth.ipf.commons.audit.AuditContext;
import org.openehealth.ipf.commons.audit.DefaultAuditContext;
import org.openehealth.ipf.commons.audit.marshal.SerializationStrategy;
import org.openehealth.ipf.commons.audit.marshal.dicom.DICOM2017c;
import org.openehealth.ipf.commons.audit.model.AuditMessage;
import org.openehealth.ipf.commons.audit.model.EventIdentificationType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class AuditConfig {

  private final AuditProperties auditProperties;

  @Bean
  public AuditContext auditContext() {
    DefaultAuditContext auditContext = new DefaultAuditContext();

    auditContext.setSerializationStrategy(
      new SerializationStrategy() {

        // Custom with correct EventDate length
        private final IHEConformDICOM dicom = new IHEConformDICOM();

        @Override
        public void marshal(AuditMessage auditMessage, Writer writer, boolean pretty)
          throws IOException {
          dicom.marshal(auditMessage, writer, pretty);
        }
      });

    auditContext.setAuditEnabled(true);
    auditContext.setAuditEnterpriseSiteId("Central transactional repository");
    auditContext.setAuditSourceId("Demographics service");
    auditContext.setAuditRepositoryHost(auditProperties.getHost());
    auditContext.setAuditRepositoryPort(auditProperties.getPort());

    return auditContext;
  }

  private static class IHEConformDICOM extends DICOM2017c {

    @Override
    protected Element eventIdentification(EventIdentificationType eventIdentification) {
      Element element = super.eventIdentification(eventIdentification);
      // Fix to long ISO8601 string
      element.setAttribute(
        "EventDateTime",
        new DateTimeFormatterBuilder()
          .appendInstant(5)
          .toFormatter()
          .format(eventIdentification.getEventDateTime()));
      return element;
    }
  }
}
