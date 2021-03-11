package de.vitagroup.num.interceptors;

import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.interceptor.auth.AuthorizationInterceptor;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;

import java.util.ArrayList;
import java.util.List;

public class TestAuthzInterceptor extends AuthorizationInterceptor {
  @Override
  public List<IAuthRule> buildRuleList(RequestDetails pReqDetails) {
    List<IAuthRule> result = new ArrayList<>();
//    result.add(new RuleBuilder().allowAll("rule_allow_all").build());
    return new RuleBuilder().allowAll("rule_allow_all").build();
  }
}
