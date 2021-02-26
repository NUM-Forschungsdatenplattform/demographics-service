package de.vitagroup.num.abac;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import lombok.Data;
import org.hl7.fhir.r4.model.Consent;

@Data
@Builder
public class ConsentEvent {

  private boolean insert;

  @JsonSerialize(using = ConsentSerializer.class)
  private final Consent consent;
}
