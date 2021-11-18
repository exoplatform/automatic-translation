/*
 * Copyright (C) 2021 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.automatic.translation.impl;

import org.exoplatform.automatic.translation.api.AutomaticTranslationComponentPlugin;
import org.exoplatform.automatic.translation.api.AutomaticTranslationService;
import org.exoplatform.automatic.translation.api.dto.AutomaticTranslationConfiguration;
import org.exoplatform.automatic.translation.impl.connectors.GoogleTranslateConnector;
import org.exoplatform.commons.api.settings.ExoFeatureService;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.services.listener.ListenerService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AutomaticTranslationServiceTest {
  private static final String AUTOMATIC_TRANSLATION_API_KEY          = "automaticTranslationApiKey";

  private static final String AUTOMATIC_TRANSLATION_ACTIVE_CONNECTOR = "automaticTranslationActiveConnector";

  private static final String FEATURE_NAME                           = "automatic-translation";

  @Mock
  SettingService              settingService;

  @Mock
  ExoFeatureService           exoFeatureService;

  @Mock
  ListenerService             listenerService;

  @Before
  public void setUp() {
    settingService = Mockito.mock(SettingService.class);
    exoFeatureService = Mockito.mock(ExoFeatureService.class);
    listenerService = Mockito.mock(ListenerService.class);
  }

  @Test
  public void testAddConnector() {

    AutomaticTranslationService translationService = new AutomaticTranslationServiceImpl(settingService,
                                                                                         exoFeatureService,
                                                                                         listenerService);
    AutomaticTranslationComponentPlugin translationConnector = new GoogleTranslateConnector(settingService);
    translationConnector.setName("google");
    translationConnector.setDescription("google");

    translationService.addConnector(translationConnector);
    assertEquals(1, translationService.getConnectors().size());
    assertEquals("google", translationService.getConnectors().get("google").getName());
  }

  @Test
  public void testGetActiveConnectorWhenNotConfigured() {

    when(settingService.get(Context.GLOBAL, Scope.GLOBAL, AUTOMATIC_TRANSLATION_ACTIVE_CONNECTOR)).thenReturn(null);
    when(exoFeatureService.isActiveFeature(FEATURE_NAME)).thenReturn(true);
    AutomaticTranslationService translationService = new AutomaticTranslationServiceImpl(settingService,
                                                                                         exoFeatureService,
                                                                                         listenerService);

    assertNull(translationService.getActiveConnector());
  }

  @Test
  public void testGetActiveConnectorWhenConfigured() {
    when(exoFeatureService.isActiveFeature("automatic-translation")).thenReturn(true);
    SettingValue setting = new SettingValue<>("google");
    when(settingService.get(Context.GLOBAL, Scope.GLOBAL, AUTOMATIC_TRANSLATION_ACTIVE_CONNECTOR)).thenReturn(setting);
    AutomaticTranslationService translationService = new AutomaticTranslationServiceImpl(settingService,
                                                                                         exoFeatureService,
                                                                                         listenerService);

    AutomaticTranslationComponentPlugin translationConnector = new GoogleTranslateConnector(settingService);
    translationConnector.setName("google");
    translationConnector.setDescription("google");

    translationService.addConnector(translationConnector);

    assertEquals("google", translationService.getActiveConnector());
  }

  @Test
  public void testCallTranslateForActiveConnector() {
    SettingValue setting = new SettingValue<>("google");
    when(exoFeatureService.isActiveFeature("automatic-translation")).thenReturn(true);
    when(settingService.get(Context.GLOBAL, Scope.GLOBAL, AUTOMATIC_TRANSLATION_ACTIVE_CONNECTOR)).thenReturn(setting);

    AutomaticTranslationComponentPlugin translationConnector = mock(GoogleTranslateConnector.class);
    when(translationConnector.getName()).thenReturn("google");
    String message = "message to translate";
    when(translationConnector.translate(message, Locale.FRANCE)).thenReturn("message translated");
    AutomaticTranslationService translationService = new AutomaticTranslationServiceImpl(settingService,
                                                                                         exoFeatureService,
                                                                                         listenerService);
    translationService.addConnector(translationConnector);
    assertEquals("message translated", translationService.translate(message, Locale.FRANCE, "test", 1L));
  }

  @Test
  public void testSetActiveConnector() {
    when(exoFeatureService.isActiveFeature("automatic-translation")).thenReturn(true);

    AutomaticTranslationService translationService = new AutomaticTranslationServiceImpl(settingService,
                                                                                         exoFeatureService,
                                                                                         listenerService);
    String connectorName = "google";
    AutomaticTranslationComponentPlugin translationConnector = new GoogleTranslateConnector(settingService);
    translationConnector.setName("google");
    translationConnector.setDescription("google");

    translationService.addConnector(translationConnector);

    try {
      translationService.setActiveConnector(connectorName);
      verify(settingService, times(1)).set(eq(Context.GLOBAL),
                                           eq(Scope.GLOBAL),
                                           eq(AUTOMATIC_TRANSLATION_ACTIVE_CONNECTOR),
                                           argThat(argument -> argument.getValue().equals(connectorName)));
    } catch (Exception e) {
      fail();
    }


  }

  @Test
  public void testGetActiveConnectorWithFeatureNotActive() {

    SettingValue setting = new SettingValue<>("google");
    when(settingService.get(Context.GLOBAL, Scope.GLOBAL, AUTOMATIC_TRANSLATION_ACTIVE_CONNECTOR)).thenReturn(setting);

    AutomaticTranslationService translationService = new AutomaticTranslationServiceImpl(settingService,
                                                                                         exoFeatureService,
                                                                                         listenerService);

    assertFalse(translationService.isFeatureActive());

  }

  @Test
  public void testSetNonExistingActiveConnector() {
    when(exoFeatureService.isActiveFeature("automatic-translation")).thenReturn(true);

    AutomaticTranslationService translationService = new AutomaticTranslationServiceImpl(settingService,
                                                                                         exoFeatureService,
                                                                                         listenerService);
    String connectorName = "google";

    try {
      translationService.setActiveConnector(connectorName);
      fail();
    } catch (Exception e) {
    }
  }

  @Test
  public void testSetActiveConnectorWhenFeatureNotActive() {
    AutomaticTranslationService translationService = new AutomaticTranslationServiceImpl(settingService,
                                                                                         exoFeatureService,
                                                                                         listenerService);
    String connectorName = "google";
    AutomaticTranslationComponentPlugin translationConnector = new GoogleTranslateConnector(settingService);
    translationConnector.setName("google");
    translationConnector.setDescription("google");

    translationService.addConnector(translationConnector);
    try {
      translationService.setActiveConnector(connectorName);
      fail();
    } catch (Exception e) {

    }
  }

  @Test
  public void testUnSetConnector() {
    when(exoFeatureService.isActiveFeature("automatic-translation")).thenReturn(true);

    AutomaticTranslationService translationService = new AutomaticTranslationServiceImpl(settingService,
                                                                                         exoFeatureService,
                                                                                         listenerService);
    String connectorName = "";

    try {
      translationService.setActiveConnector(connectorName);
      connectorName = null;
      translationService.setActiveConnector(connectorName);
    } catch (Exception e) {
      fail();
    }

  }

  @Test
  public void testSetApiKeyWhenConnectorNotExists() {
    AutomaticTranslationService translationService = new AutomaticTranslationServiceImpl(settingService,
                                                                                         exoFeatureService,
                                                                                         listenerService);
    String connectorName = "google";

    try {
      translationService.setApiKey(connectorName, "123456");
      fail();
    } catch (Exception e) {
    }
  }

  @Test
  public void testSetApiKeyWhenConnectorExists() {
    AutomaticTranslationService translationService = new AutomaticTranslationServiceImpl(settingService,
                                                                                         exoFeatureService,
                                                                                         listenerService);
    String connectorName = "google";
    AutomaticTranslationComponentPlugin translationConnector = new GoogleTranslateConnector(settingService);
    translationConnector.setName("google");
    translationConnector.setDescription("google");

    translationService.addConnector(translationConnector);

    try {
      translationService.setApiKey(connectorName, "123456");
      verify(settingService, times(1)).set(eq(Context.GLOBAL),
                                           eq(Scope.GLOBAL),
                                           eq(AUTOMATIC_TRANSLATION_API_KEY + "-" + connectorName),
                                           argThat(argument -> argument.getValue().equals("123456")));
    } catch (Exception e) {
      fail();
    }

  }

  @Test
  public void testGetConfiguration() {
    when(exoFeatureService.isActiveFeature("automatic-translation")).thenReturn(true);

    SettingValue settingConnector = new SettingValue<>("systran");
    when(settingService.get(Context.GLOBAL, Scope.GLOBAL, AUTOMATIC_TRANSLATION_ACTIVE_CONNECTOR)).thenReturn(settingConnector);
    SettingValue settingKey = new SettingValue<>("123456");
    when(settingService.get(Context.GLOBAL, Scope.GLOBAL, AUTOMATIC_TRANSLATION_API_KEY + "-systran")).thenReturn(settingKey);

    AutomaticTranslationService translationService = new AutomaticTranslationServiceImpl(settingService,
                                                                                         exoFeatureService,
                                                                                         listenerService);
    AutomaticTranslationComponentPlugin translationConnector = new GoogleTranslateConnector(settingService);
    translationConnector.setName("google");
    translationConnector.setDescription("google");
    translationService.addConnector(translationConnector);

    AutomaticTranslationComponentPlugin translationConnector2 = new GoogleTranslateConnector(settingService);
    translationConnector2.setName("systran");
    translationConnector2.setDescription("systran");
    translationService.addConnector(translationConnector2);

    AutomaticTranslationConfiguration configuration = translationService.getConfiguration();

    assertEquals(2, configuration.getConnectors().size());
    assertEquals("google", configuration.getConnectors().get(0).getName());
    assertEquals("systran", configuration.getConnectors().get(1).getName());
    assertEquals("123456", configuration.getActiveApiKey());
    assertEquals("systran", configuration.getActiveConnector());
    assertEquals(null, configuration.getConnectors().get(0).getApiKey());
    assertEquals("123456", configuration.getConnectors().get(1).getApiKey());

  }
}
