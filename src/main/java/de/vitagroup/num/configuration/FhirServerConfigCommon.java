package de.vitagroup.num.configuration;

import ca.uhn.fhir.jpa.api.config.DaoConfig;
import ca.uhn.fhir.jpa.model.config.PartitionSettings;
import ca.uhn.fhir.jpa.model.entity.ModelConfig;
import de.vitagroup.num.properties.HapiProperties;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@AllArgsConstructor
@EnableTransactionManagement
public class FhirServerConfigCommon {

  private final HapiProperties hapiProperties;

  @Bean
  public DaoConfig daoConfig() {
    DaoConfig daoConfig = new DaoConfig();

    Integer maxFetchSize =  hapiProperties.getMax_page_size();
    daoConfig.setFetchSizeDefaultMaximum(maxFetchSize);

    Long reuseCachedSearchResultsMillis = hapiProperties.getReuse_cached_search_results_millis();
    daoConfig.setReuseCachedSearchResultsForMillis(reuseCachedSearchResultsMillis);

    Long retainCachedSearchesMinutes = hapiProperties.getRetain_cached_searches_mins();
    daoConfig.setExpireSearchResultsAfterMillis(retainCachedSearchesMinutes * 60 * 1000);

    daoConfig.setFilterParameterEnabled(hapiProperties.getFilter_search_enabled());

    return daoConfig;
  }

  @Bean
  public PartitionSettings partitionSettings() {
    return new PartitionSettings();
  }

  @Bean
  public ModelConfig modelConfig() {
    ModelConfig modelConfig = new ModelConfig();
    modelConfig.setAllowContainsSearches(hapiProperties.getAllow_contains_searches());
    modelConfig.setAllowExternalReferences(hapiProperties.getAllow_external_references());
    modelConfig.setDefaultSearchParamsCanBeOverridden(hapiProperties.getAllow_override_default_search_params());
    return modelConfig;
  }

}
