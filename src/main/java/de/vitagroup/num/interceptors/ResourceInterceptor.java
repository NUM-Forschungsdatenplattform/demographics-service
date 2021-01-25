package de.vitagroup.num.interceptors;

import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.rest.api.server.IPreResourceShowDetails;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.ForbiddenOperationException;
import ca.uhn.fhir.rest.server.servlet.ServletRequestDetails;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Consent;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@Interceptor
public class ResourceInterceptor {

  @Hook(Pointcut.STORAGE_PRESHOW_RESOURCES)
  public void resourceRead(
    IPreResourceShowDetails showDetails,
    RequestDetails requestDetails,
    ServletRequestDetails servletRequestDetails) {

    showDetails.forEach(this::checkPatientReference);
  }

  private void checkPatientReference(IBaseResource resource) {
    if (resource instanceof Consent) {
      Jwt jwt =
        ((JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication())
          .getToken();

      String tokenPatientId = jwt.getClaim("patient_id");
      String reference = ((Consent) resource).getPatient().getReference();

      if (StringUtils.isEmpty(tokenPatientId)
        || !(Patient.class.getSimpleName() + "/" + tokenPatientId).equals(reference)) {
        throw new ForbiddenOperationException("Reading of not owned consent is not allowed.");
      }
    }
  }

  @Hook(Pointcut.STORAGE_PRESTORAGE_RESOURCE_CREATED)
  public void resourceCreated(RequestDetails theRequest, IBaseResource theResource) {
    System.out.println(theResource.getIdElement());
  }

  @Hook(Pointcut.STORAGE_PRESTORAGE_RESOURCE_UPDATED)
  public void resourceUpdated(RequestDetails theRequest, IBaseResource theResource) {
    System.out.println(theResource.getIdElement());
  }
}
