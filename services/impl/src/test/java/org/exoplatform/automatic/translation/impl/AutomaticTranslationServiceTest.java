package org.exoplatform.automatic.translation.impl;

import org.exoplatform.automatic.translation.api.AutomaticTranslationComponentPlugin;
import org.exoplatform.automatic.translation.api.AutomaticTranslationService;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class AutomaticTranslationServiceTest {
  
  @Test
  public void testAddConnector() {
    AutomaticTranslationService translationService = new AutomaticTranslationServiceImpl();
    AutomaticTranslationComponentPlugin translationConnector = new AutomaticTranslationComponentPlugin();
    translationConnector.setName("google");
    translationConnector.setDescription("google");

    translationService.addConnector(translationConnector);
    assertEquals(1,translationService.getConnectors().size());
    assertEquals("google",translationService.getConnectors().get("google").getName());
  }

}
