package de.vitagroup.num.interceptors;

import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.rest.api.RestOperationTypeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;
import ca.uhn.fhir.rest.server.interceptor.auth.AuthorizationInterceptor;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Consent;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@Interceptor
public class ResourceAuthorizationInterceptor extends AuthorizationInterceptor {

  private static final String REALM_ACCESS = "realm_access";
  private static final String ROLES_CLAIM = "roles";
  private static final String ADMIN_ROLE = "admin";
  private static final String PATIENT_ID = "patient_id";
  private static final String SOF_PATIENT_ID = "patient";

  @Override
  public List<IAuthRule> buildRuleList(RequestDetails theRequestDetails) {
    // allow unconditional access to metadata requests
    if (theRequestDetails.getRestOperationType() == RestOperationTypeEnum.METADATA) {
      return new RuleBuilder().allowAll("SOF_allow_all").build();
    }

    // returning this empty will block all requests
    List<IAuthRule> rules = new ArrayList<>();

    Jwt jwt =
      ((JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication())
        .getToken();

    if (jwt != null) {
      String tokenPatientId = jwt.getClaim(PATIENT_ID);
      String smartOnFhirPatientId = jwt.getClaim(SOF_PATIENT_ID);

      // sof is a mutually exclusive case with its own logic
      if (StringUtils.isNotEmpty(smartOnFhirPatientId)) {
        addSmartOFPatientRules(smartOnFhirPatientId, rules);
      } else if (StringUtils.isNotEmpty(tokenPatientId)) {
        addPatientRules(tokenPatientId, rules);
      } else if (checkHasAdminRole(jwt)) {
        addOrganizationRules(rules);
        addKeycloakOperationsRules(rules);
      } else {
        throw new AuthenticationException("Missing or invalid Authorization header value");
      }
      rules.addAll(new RuleBuilder().denyAll("rule_deny_resource").build());
    }

    return rules;
  }

  /*
  add rules that allow keycloak server to operate on fhir resources
  this is requied for keycloak to implement SMART on FHIR related behaviour,
  such as creating a Patient resource during registration, or finding the
  Patient resource id of a user during login
   */
  private void addKeycloakOperationsRules(List<IAuthRule> pRules) {
    /*TODO: If you use HAPI FHIR client in keycloak, it'll make a call to metadata endpoint.
     * the problem is, you cannot create a rule for the MetadataResource because
     * that resource does not have the ResourceDef annotation that the rule processing
     * code requires. So adding that rule will lead to an exception and a failure to
     * authorise. I solved that problem by disabling metadata call from the FHIR client
     * but this is something to look into, because any other client can make that call.*/
    pRules.addAll(buildCreateRule("rule_create_patient_resource", Patient.class));
    pRules.addAll(buildReadRule("rule_read_patient_resource", Patient.class));
  }

  private void addOrganizationRules(List<IAuthRule> rules) {
    rules.addAll(buildCreateRule("rule_create_organization_resource", Organization.class));
    rules.addAll(buildReadRule("rule_read_organization_resource", Organization.class));
    rules.addAll(buildWriteRule("rule_update_organization_resource", Organization.class));
  }

  private void addSmartOFPatientRules(String pSmartOnFhirPatientId, List<IAuthRule> rules) {
    // no rule for create -> should be done by keycloak registration at the moment
    IdType sofId = new IdType(Patient.class.getSimpleName(), pSmartOnFhirPatientId);
    rules.addAll(buildReadRule("rule_read_own_sof_patient_resource", Patient.class, sofId));
    rules.addAll(buildWriteRule("rule_update_own_sof_patient_resource", Patient.class, sofId));
  }

  private void addPatientRules(String tokenPatientId, List<IAuthRule> rules) {
    IdType patientId = new IdType(Patient.class.getSimpleName(), tokenPatientId);

    rules.addAll(buildCreateRule("rule_create_patient_resource", Patient.class));
    rules.addAll(buildReadRule("rule_read_own_patient_resource", Patient.class, patientId));
    rules.addAll(buildWriteRule("rule_update_own_patient_resource", Patient.class, patientId));

    rules.addAll(buildCreateRule("rule_create_consent_resource", Consent.class));
    rules.addAll(buildReadRule("rule_read_consent_resource", Consent.class));
    rules.addAll(buildWriteRule("rule_update_consent_resource", Consent.class));
    rules.addAll(buildDeleteRule("rule_delete_consent_resource", Consent.class));
  }

  private List<IAuthRule> buildCreateRule(String name, Class<? extends IBaseResource> resource) {
    return new RuleBuilder().allow(name).create().resourcesOfType(resource).withAnyId().build();
  }

  private List<IAuthRule> buildReadRule(String name, Class<? extends IBaseResource> resource) {
    return new RuleBuilder().allow(name).read().resourcesOfType(resource).withAnyId().build();
  }

  private List<IAuthRule> buildReadRule(
    String name, Class<? extends IBaseResource> resource, IdType id) {
    return new RuleBuilder()
      .allow(name)
      .read()
      .resourcesOfType(resource)
      .inCompartment(resource.getSimpleName(), id)
      .build();
  }

  private List<IAuthRule> buildWriteRule(String name, Class<? extends IBaseResource> resource) {
    return new RuleBuilder().allow(name).write().resourcesOfType(resource).withAnyId().build();
  }

  private List<IAuthRule> buildDeleteRule(String name, Class<? extends IBaseResource> resource) {
    return new RuleBuilder().allow(name).delete().resourcesOfType(resource).withAnyId().build();
  }

  private List<IAuthRule> buildWriteRule(
    String name, Class<? extends IBaseResource> resource, IdType id) {
    return new RuleBuilder()
      .allow(name)
      .write()
      .resourcesOfType(resource)
      .inCompartment(resource.getSimpleName(), id)
      .build();
  }

  private boolean checkHasAdminRole(Jwt jwt) {
    JSONObject realmAccess = jwt.getClaim(REALM_ACCESS);
    if (realmAccess != null) {
      final JSONArray roles = (JSONArray) realmAccess.get(ROLES_CLAIM);

      if (CollectionUtils.isNotEmpty(roles)) {
        return roles.stream().anyMatch(role -> role.equals(ADMIN_ROLE));
      }
    }
    return false;
  }
}
