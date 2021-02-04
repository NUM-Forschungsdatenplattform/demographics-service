package de.vitagroup.num.interceptors;

import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.rest.api.server.IPreResourceShowDetails;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.ForbiddenOperationException;
import ca.uhn.fhir.rest.server.servlet.ServletRequestDetails;
import de.vitagroup.num.abac.AbacFeign;
import de.vitagroup.num.abac.ConsentEvent;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Consent;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@Interceptor
@RequiredArgsConstructor
public class ResourceInterceptor {

  private final AbacFeign abacFeign;

  @Hook(Pointcut.STORAGE_PRESHOW_RESOURCES)
  public void resourceRead(
    IPreResourceShowDetails showDetails,
    RequestDetails requestDetails,
    ServletRequestDetails servletRequestDetails) {

    showDetails.forEach(this::checkPatientReference);
  }

  private void checkPatientReference(IBaseResource resource) {
    if (resource instanceof Consent) {
      checkPatientReference((Consent) resource);
    }
  }

  @Hook(Pointcut.STORAGE_PRESTORAGE_RESOURCE_CREATED)
  public void resourceCreated(RequestDetails theRequest, IBaseResource theResource) {
    if (theResource instanceof Consent) {

      Consent consent = (Consent) theResource;
      checkPatientReference(consent);
      abacFeign.addConsent(
        ConsentEvent.builder().consent(consent).insert(true).build());
    }
  }

  @Hook(Pointcut.STORAGE_PRESTORAGE_RESOURCE_UPDATED)
  public void resourceUpdated(RequestDetails theRequest, IBaseResource theResource) {
    if (theResource instanceof Consent) {
      Consent consent = (Consent) theResource;
      checkPatientReference(consent);
      abacFeign.addConsent(
        ConsentEvent.builder().consent(consent).insert(true).build());
    }
  }

  @Hook(Pointcut.STORAGE_PRESTORAGE_RESOURCE_DELETED)
  public void resourceDeleted(RequestDetails theRequest, IBaseResource theResource) {
    if (theResource instanceof Consent) {
      Consent consent = (Consent) theResource;
      checkPatientReference(consent);
      abacFeign.addConsent(
        ConsentEvent.builder().consent(consent).insert(false).build());
    }
  }

  private void checkPatientReference(Consent resource) {
    Jwt jwt =
      ((JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication())
        .getToken();

    String tokenPatientId = jwt.getClaim("patient_id");
    String reference = resource.getPatient().getReference();

    if (StringUtils.isEmpty(tokenPatientId)
      || !(Patient.class.getSimpleName() + "/" + tokenPatientId).equals(reference)) {
      throw new ForbiddenOperationException(
        "Reading/modifying of not owned consent is not allowed.");
    }
  }
}
