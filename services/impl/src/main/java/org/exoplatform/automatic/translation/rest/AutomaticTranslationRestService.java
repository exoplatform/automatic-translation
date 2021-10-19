package org.exoplatform.automatic.translation.rest;

import io.swagger.annotations.Api;
import org.exoplatform.automatic.translation.api.AutomaticTranslationService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;

import javax.ws.rs.Path;

@Path("/automatic-translation")
@Api(value = "/automatic-translation")
public class AutomaticTranslationRestService implements ResourceContainer {

  private AutomaticTranslationService automaticTranslationService;

  private static final Log LOG = ExoLogger.getLogger(AutomaticTranslationRestService.class);

  public AutomaticTranslationRestService(AutomaticTranslationService automaticTranslationService) {
    this.automaticTranslationService = automaticTranslationService;
  }
}

