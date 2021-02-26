package de.vitagroup.num.abac;

import ca.uhn.fhir.context.FhirContext;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import org.hl7.fhir.r4.model.Consent;

public class ConsentSerializer extends JsonSerializer<Consent> {

  @Override
  public void serialize(Consent value, JsonGenerator gen, SerializerProvider serializers)
    throws IOException {
    gen.writeRawValue(FhirContext.forR4().newJsonParser().encodeResourceToString(value));
  }
}
