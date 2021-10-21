package org.exoplatform.automatic.translation.impl;

import org.exoplatform.automatic.translation.api.AutomaticTranslationComponentPlugin;
import org.exoplatform.automatic.translation.api.AutomaticTranslationService;
import org.exoplatform.commons.api.settings.ExoFeatureService;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AutomaticTranslationServiceTest {

  private static final String AUTOMATIC_TRANSLATION_ACTIVE_CONNECTOR = "automaticTranslationActiveConnector";
  private static final String FEATURE_NAME = "automatic-translation";


  @Mock
  SettingService settingService;

  @Mock
  ExoFeatureService exoFeatureService;

  @Before
  public void setUp() {
    settingService = Mockito.mock(SettingService.class);
    exoFeatureService = Mockito.mock(ExoFeatureService.class);
  }



  @Test
  public void testAddConnector() {

    AutomaticTranslationService translationService = new AutomaticTranslationServiceImpl(settingService,exoFeatureService);
    AutomaticTranslationComponentPlugin translationConnector = new AutomaticTranslationComponentPlugin();
    translationConnector.setName("google");
    translationConnector.setDescription("google");

    translationService.addConnector(translationConnector);
    assertEquals(1,translationService.getConnectors().size());
    assertEquals("google",translationService.getConnectors().get("google").getName());
  }

  @Test
  public void testGetActiveConnectorWhenNotConfigured() {

    when(settingService.get(Context.GLOBAL, Scope.GLOBAL,AUTOMATIC_TRANSLATION_ACTIVE_CONNECTOR)).thenReturn(null);
    when(exoFeatureService.isActiveFeature(FEATURE_NAME)).thenReturn(true);
    AutomaticTranslationService translationService = new AutomaticTranslationServiceImpl(settingService,exoFeatureService);

    assertNull(translationService.getActiveConnector());
  }

  @Test
  public void testGetActiveConnectorWhenConfigured() {
    SettingValue setting = new SettingValue<>("google");
    System.setProperty("exo.feature."+FEATURE_NAME+".enabled","true");
    when(settingService.get(eq(Context.GLOBAL), eq(Scope.GLOBAL),eq(AUTOMATIC_TRANSLATION_ACTIVE_CONNECTOR))).thenReturn(setting);
    AutomaticTranslationService translationService = new AutomaticTranslationServiceImpl(settingService,exoFeatureService);
    assertEquals("google",translationService.getActiveConnector());
  }


  @Test
  public void testSetActiveConnector() {
    AutomaticTranslationService translationService = new AutomaticTranslationServiceImpl(settingService,exoFeatureService);
    String connectorName = "google";
    AutomaticTranslationComponentPlugin translationConnector = new AutomaticTranslationComponentPlugin();
    translationConnector.setName("google");
    translationConnector.setDescription("google");

    translationService.addConnector(translationConnector);

    assertTrue(translationService.setActiveConnector(connectorName));
    verify(settingService,times(1)).set(eq(Context.GLOBAL),
                                                              eq(Scope.GLOBAL),
                                                              eq(AUTOMATIC_TRANSLATION_ACTIVE_CONNECTOR),
                                                              argThat(argument -> argument.getValue().equals(connectorName)));


  }

  @Test
  public void testGetActiveConnectorWithFeatureNotActivated() {

    SettingValue setting = new SettingValue<>("google");
    when(settingService.get(Context.GLOBAL, Scope.GLOBAL,AUTOMATIC_TRANSLATION_ACTIVE_CONNECTOR)).thenReturn(setting);

    AutomaticTranslationService translationService = new AutomaticTranslationServiceImpl(settingService,exoFeatureService);

    assertNull(translationService.getActiveConnector());

  }

  @Test
  public void testSetNonExistingActiveConnector() {
    AutomaticTranslationService translationService = new AutomaticTranslationServiceImpl(settingService,exoFeatureService);
    String connectorName = "google";

    assertFalse(translationService.setActiveConnector(connectorName));
  }
}
