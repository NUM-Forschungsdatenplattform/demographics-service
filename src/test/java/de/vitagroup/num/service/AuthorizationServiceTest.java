package de.vitagroup.num.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.hl7.fhir.r4.model.Consent;
import org.hl7.fhir.r4.model.Consent.provisionComponent;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Reference;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AuthorizationServiceTest {

  @InjectMocks
  private AuthorizationService authorizationService;

  @Mock
  private ConsentService consentService;

  @Test
  public void shouldReturnAuthorization() {
    when(consentService.retrieveConsent(anyString())).thenReturn(retrieveConsent("2050-01-01"));

    Map<String, Object> values = new HashMap<>();
    values.put("patient_id", "52");
    values.put("organization_id", "53");
    boolean isAuthorized = authorizationService.checkIsAuthorized("has_consent", values);
    assertThat(isAuthorized, is(true));
  }

  @Test
  public void shouldNotReturnAuthorization() {
    when(consentService.retrieveConsent(anyString())).thenReturn(retrieveConsent("2050-01-01"));

    Map<String, Object> values = new HashMap<>();
    values.put("patient_id", "52");
    values.put("organization_id", "55");
    boolean isAuthorized = authorizationService.checkIsAuthorized("has_consent", values);
    assertThat(isAuthorized, is(false));
  }

  @Test
  public void shouldNotReturnAuthorizationForExpiredConsent() {
    when(consentService.retrieveConsent(anyString())).thenReturn(retrieveConsent("2020-01-01"));

    Map<String, Object> values = new HashMap<>();
    values.put("patient_id", "52");
    values.put("organization_id", "53");
    boolean isAuthorized = authorizationService.checkIsAuthorized("has_consent", values);
    assertThat(isAuthorized, is(false));
  }

  @Test
  public void shouldNotReturnAuthorizationForMissingPatientId() {
    when(consentService.retrieveConsent(anyString())).thenReturn(retrieveConsent("2050-01-01"));

    Map<String, Object> values = new HashMap<>();
    values.put("organization_id", "55");
    boolean isAuthorized = authorizationService.checkIsAuthorized("has_consent", values);
    assertThat(isAuthorized, is(false));
  }

  @Test
  public void shouldNotReturnAuthorizationForMissingOrganizationId() {
    when(consentService.retrieveConsent(anyString())).thenReturn(retrieveConsent("2050-01-01"));

    Map<String, Object> values = new HashMap<>();
    values.put("patient_id", "52");
    boolean isAuthorized = authorizationService.checkIsAuthorized("has_consent", values);
    assertThat(isAuthorized, is(false));
  }

  private Optional<Consent> retrieveConsent(String validUntil) {
    Consent consent = new Consent();
    consent.setPatient(new Reference("Patient/52"));
    consent.setOrganization(List.of(new Reference("Organization/53")));

    Consent.provisionComponent provisionComponent = new provisionComponent();
    Period period = new Period();
    period.setStartElement(new DateTimeType("2000-01-01"));
    period.setEndElement(new DateTimeType((validUntil)));
    provisionComponent.setPeriod(period);
    consent.setProvision(provisionComponent);
    return Optional.of(consent);
  }
}
