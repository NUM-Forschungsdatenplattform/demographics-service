package de.vitagroup.num;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.jpa.api.config.DaoConfig;
import ca.uhn.fhir.jpa.api.dao.DaoRegistry;
import ca.uhn.fhir.jpa.api.dao.IFhirSystemDao;
import ca.uhn.fhir.jpa.provider.IJpaSystemProvider;
import ca.uhn.fhir.jpa.provider.r4.JpaConformanceProviderR4;
import ca.uhn.fhir.jpa.search.DatabaseBackedPagingProvider;
import ca.uhn.fhir.jpa.searchparam.registry.ISearchParamRegistry;
import ca.uhn.fhir.narrative.DefaultThymeleafNarrativeGenerator;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.provider.ResourceProviderFactory;
import de.vitagroup.num.properties.HapiProperties;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.ServletException;
import java.util.List;

@NoArgsConstructor
public class BaseJpaRestfulServer extends RestfulServer {

  @Autowired
  DaoRegistry daoRegistry;

  @Autowired
  DaoConfig daoConfig;

  @Autowired
  ISearchParamRegistry searchParamRegistry;

  @Autowired
  IFhirSystemDao fhirSystemDao;

  @Autowired
  ResourceProviderFactory resourceProviders;

  @Autowired
  IJpaSystemProvider jpaSystemProvider;

  @Autowired
  DatabaseBackedPagingProvider databaseBackedPagingProvider;

  @Autowired
  HapiProperties hapiProperties;

  @Override
  protected void initialize() throws ServletException {
    super.initialize();

    List<String> supportedResourceTypes = hapiProperties.getSupportedResourceTypes();

    if (!supportedResourceTypes.isEmpty() && !supportedResourceTypes.contains("SearchParameter")) {
      supportedResourceTypes.add("SearchParameter");
      daoRegistry.setSupportedResourceTypes(supportedResourceTypes);
    }

    setFhirContext(fhirSystemDao.getContext());
    registerProviders(resourceProviders.createProviders());
    registerProvider(jpaSystemProvider);

    FhirVersionEnum fhirVersion = fhirSystemDao.getContext().getVersion().getVersion();

    if (fhirVersion == FhirVersionEnum.R4) {
      JpaConformanceProviderR4 confProvider = new JpaConformanceProviderR4(this, fhirSystemDao,
        daoConfig, searchParamRegistry);
      confProvider.setImplementationDescription("HAPI FHIR R4 Server");
      setServerConformanceProvider(confProvider);
    } else {
      throw new IllegalStateException();
    }

    FhirContext ctx = getFhirContext();
    ctx.setNarrativeGenerator(new DefaultThymeleafNarrativeGenerator());
    setDefaultPrettyPrint(true);
    setDefaultResponseEncoding(EncodingEnum.JSON);
    setPagingProvider(databaseBackedPagingProvider);
    daoConfig.setDeferIndexingForCodesystemsOfSize(hapiProperties.getDefer_indexing_for_codesystems_of_size());
  }

}
