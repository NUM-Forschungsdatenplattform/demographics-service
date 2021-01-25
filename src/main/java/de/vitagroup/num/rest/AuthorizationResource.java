package de.vitagroup.num.rest;

import de.vitagroup.num.service.AuthorizationService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/rest/v1/policy")
@RequiredArgsConstructor
public class AuthorizationResource {

  private static final String ERROR_DESCRIPTION = "error_description";
  private static final List<String> ALLOWED_POLICY_NAMES = List.of("has_consent");

  private final AuthorizationService authorizationService;

  @PostMapping("/execute/name/{name}")
  public ResponseEntity<?> executeByNameSimple(
    @PathVariable("name") String name, @RequestBody Map<String, Object> ctx) {

    log.debug("Executing authorization request, ctx={}", ctx);

    if (!ALLOWED_POLICY_NAMES.contains(name)) {
      return getErrorResponse(
        "Policy name has to be one of " + StringUtils.join(ALLOWED_POLICY_NAMES, ","));
    }
    if (authorizationService.checkIsAuthorized(name, ctx)) {
      return ResponseEntity.ok().build();
    } else {
      return getErrorResponse("Not Authorized!");
    }
  }

  private ResponseEntity<?> getErrorResponse(String message) {
    Map<String, String> errors = new HashMap<>();
    errors.put(ERROR_DESCRIPTION, message);
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errors);
  }
}
