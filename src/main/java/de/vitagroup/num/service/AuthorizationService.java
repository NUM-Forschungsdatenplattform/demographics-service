package de.vitagroup.num.service;

import ca.uhn.fhir.jpa.api.dao.DaoRegistry;
import ca.uhn.fhir.jpa.searchparam.SearchParameterMap;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.ReferenceParam;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.Consent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthorizationService {

  private static final String ORGANIZATION_ID = "organization_id";
  private static final String PATIENT_ID = "patient_id";
  private static final String PATIENT_REFERENCE_PREFIX = "Patient/";

  private final DaoRegistry daoRegistry;

  public boolean checkIsAuthorized(String policyName, Map<String, Object> values) {
    if (policyName.equals("has_consent")) {
      return checkHasConsent(values);
    } else {
      return true;
    }
  }

  private boolean checkHasConsent(Map<String, Object> values) {
    Optional<?> organizationId = Optional.ofNullable(values.get(ORGANIZATION_ID));
    if (organizationId.isEmpty()) {
      return false;
    }

    Optional<?> patientId = Optional.ofNullable(values.get(PATIENT_ID));
    if (patientId.isEmpty()) {
      return false;
    }

    Optional<Consent> consent = retrieveConsent(patientId.get().toString());
    if (consent.isEmpty()) {
      return false;
    }

    return consent.get().getOrganization().stream()
      .anyMatch(
        organization -> organization.getReference().equals("Organization/" + organizationId.get()));
  }

  private Optional<Consent> retrieveConsent(String patientId) {
    SearchParameterMap searchParameterMap = new SearchParameterMap();
    ReferenceParam patientReference = new ReferenceParam(PATIENT_REFERENCE_PREFIX + patientId);
    searchParameterMap.add(Consent.SP_PATIENT, patientReference);

    IBundleProvider bundleProvider =
      daoRegistry.getResourceDao(Consent.class).search(searchParameterMap);

    if (bundleProvider.isEmpty()) {
      return Optional.empty();
    }

    return Optional.of((Consent) bundleProvider.getResources(0, bundleProvider.size()).get(0));
  }
}
