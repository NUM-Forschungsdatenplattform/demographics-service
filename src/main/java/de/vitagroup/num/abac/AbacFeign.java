package de.vitagroup.num.abac;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "abac", url = "${abac.url}")
public interface AbacFeign {

  final String CONSENT_REST_PATH = "rest/v1/event/listener/callbacks/consent/consent";

  @PostMapping(CONSENT_REST_PATH)
  void addConsent(@RequestBody ConsentEvent consentEvent);

  @DeleteMapping(CONSENT_REST_PATH)
  void removeConsent(@RequestBody ConsentEvent consentEvent);
}
