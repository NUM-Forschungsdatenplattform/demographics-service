package de.vitagroup.num.service;

import ca.uhn.fhir.jpa.api.dao.DaoRegistry;
import ca.uhn.fhir.jpa.searchparam.SearchParameterMap;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.ReferenceParam;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.Consent;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConsentService {

  private static final String PATIENT_REFERENCE_PREFIX = "Patient/";

  private final DaoRegistry daoRegistry;

  public Optional<Consent> retrieveConsent(String patientId) {
    SearchParameterMap searchParameterMap = new SearchParameterMap();
    ReferenceParam patientReference = new ReferenceParam(PATIENT_REFERENCE_PREFIX + patientId);
    searchParameterMap.add(Consent.SP_PATIENT, patientReference);

    IBundleProvider bundleProvider =
      daoRegistry.getResourceDao(Consent.class).search(searchParameterMap);

    if (bundleProvider.isEmpty()) {
      return Optional.empty();
    }

    int bundleSize = bundleProvider.size();
    return Optional.of((Consent) bundleProvider.getResources(bundleSize - 1, bundleSize).get(0));
  }
}
