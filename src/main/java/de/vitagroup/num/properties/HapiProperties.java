package de.vitagroup.num.properties;

import ca.uhn.fhir.context.FhirVersionEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;


@Data
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "hapi.fhir")
public class HapiProperties {

  private List<String> supportedResourceTypes = new ArrayList<>();
  private Integer default_page_size = 20;
  private Integer defer_indexing_for_codesystems_of_size = 100;
  private Integer max_page_size = Integer.MAX_VALUE;
  private Boolean allow_contains_searches = true;
  private Boolean allow_external_references = true;
  private Boolean allow_override_default_search_params = true;
  private Boolean filter_search_enabled = true;
  private Long retain_cached_searches_mins = 60L;
  private Long reuse_cached_search_results_millis = 60000L;
  private FhirVersionEnum fhir_version = FhirVersionEnum.R4;
  private List<String> supported_resource_types = new ArrayList<>();

}

