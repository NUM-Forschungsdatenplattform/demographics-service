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
import de.vitagroup.num.abac.AbacFeign;
import de.vitagroup.num.interceptors.ResourceAuthorizationInterceptor;
import de.vitagroup.num.interceptors.ResourceInterceptor;
import de.vitagroup.num.properties.HapiProperties;
import java.util.List;
import javax.servlet.ServletException;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

@NoArgsConstructor
public class BaseJpaRestfulServer extends RestfulServer {

  @Autowired
  private DaoRegistry daoRegistry;

  @Autowired
  private DaoConfig daoConfig;

  @Autowired
  private ISearchParamRegistry searchParamRegistry;

  @Autowired
  private IFhirSystemDao fhirSystemDao;

  @Autowired
  private ResourceProviderFactory resourceProviders;

  @Autowired
  private IJpaSystemProvider jpaSystemProvider;

  @Autowired
  private DatabaseBackedPagingProvider databaseBackedPagingProvider;

  @Autowired
  private HapiProperties hapiProperties;

  @Autowired
  private AbacFeign abacFeign;

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
    registerInterceptor(new ResourceInterceptor(abacFeign));
    registerInterceptor(new ResourceAuthorizationInterceptor());

    FhirVersionEnum fhirVersion = fhirSystemDao.getContext().getVersion().getVersion();

    if (fhirVersion == FhirVersionEnum.R4) {
      JpaConformanceProviderR4 confProvider =
        new JpaConformanceProviderR4(this, fhirSystemDao, daoConfig, searchParamRegistry);
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
    daoConfig.setDeferIndexingForCodesystemsOfSize(
      hapiProperties.getDefer_indexing_for_codesystems_of_size());
  }
}
