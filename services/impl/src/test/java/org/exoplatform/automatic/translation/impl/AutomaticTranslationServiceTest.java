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
import org.exoplatform.automatic.translation.impl.connectors.GoogleTranslateConnector;
import org.exoplatform.commons.api.settings.ExoFeatureService;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
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

  @Before
  public void setUp() {
    settingService = Mockito.mock(SettingService.class);
    exoFeatureService = Mockito.mock(ExoFeatureService.class);
  }

  @Test
  public void testAddConnector() {

    AutomaticTranslationService translationService = new AutomaticTranslationServiceImpl(settingService, exoFeatureService);
    AutomaticTranslationComponentPlugin translationConnector = new AutomaticTranslationComponentPlugin(settingService);
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
    AutomaticTranslationService translationService = new AutomaticTranslationServiceImpl(settingService, exoFeatureService);

    assertNull(translationService.getActiveConnector());
  }

  @Test
  public void testGetActiveConnectorWhenConfigured() {
    SettingValue setting = new SettingValue<>("google");
    System.setProperty("exo.feature." + FEATURE_NAME + ".enabled", "true");
    when(settingService.get(Context.GLOBAL, Scope.GLOBAL, AUTOMATIC_TRANSLATION_ACTIVE_CONNECTOR)).thenReturn(setting);
    AutomaticTranslationService translationService = new AutomaticTranslationServiceImpl(settingService, exoFeatureService);
    assertEquals("google", translationService.getActiveConnector());
    System.setProperty("exo.feature." + FEATURE_NAME + ".enabled", "");
  }

  @Test
  public void testCallTranslateForActiveConnector() {
    SettingValue setting = new SettingValue<>("google");
    System.setProperty("exo.feature." + FEATURE_NAME + ".enabled", "true");
    when(settingService.get(Context.GLOBAL, Scope.GLOBAL, AUTOMATIC_TRANSLATION_ACTIVE_CONNECTOR)).thenReturn(setting);

    AutomaticTranslationComponentPlugin translationConnector = mock(GoogleTranslateConnector.class);
    when(translationConnector.getName()).thenReturn("google");
    String message = "message to translate";
    when(translationConnector.translate(message, Locale.FRANCE)).thenReturn("message translated");
    AutomaticTranslationService translationService = new AutomaticTranslationServiceImpl(settingService, exoFeatureService);
    translationService.addConnector(translationConnector);
    assertEquals("message translated", translationService.translate(message, Locale.FRANCE));
    System.setProperty("exo.feature." + FEATURE_NAME + ".enabled", "");
  }

  @Test
  public void testSetActiveConnector() {
    System.setProperty("exo.feature." + FEATURE_NAME + ".enabled", "true");

    AutomaticTranslationService translationService = new AutomaticTranslationServiceImpl(settingService, exoFeatureService);
    String connectorName = "google";
    AutomaticTranslationComponentPlugin translationConnector = new AutomaticTranslationComponentPlugin(settingService);
    translationConnector.setName("google");
    translationConnector.setDescription("google");

    translationService.addConnector(translationConnector);

    assertTrue(translationService.setActiveConnector(connectorName));
    verify(settingService, times(1)).set(eq(Context.GLOBAL),
                                         eq(Scope.GLOBAL),
                                         eq(AUTOMATIC_TRANSLATION_ACTIVE_CONNECTOR),
                                         argThat(argument -> argument.getValue().equals(connectorName)));

    System.setProperty("exo.feature." + FEATURE_NAME + ".enabled", "");

  }

  @Test
  public void testGetActiveConnectorWithFeatureNotActive() {

    SettingValue setting = new SettingValue<>("google");
    when(settingService.get(Context.GLOBAL, Scope.GLOBAL, AUTOMATIC_TRANSLATION_ACTIVE_CONNECTOR)).thenReturn(setting);

    AutomaticTranslationService translationService = new AutomaticTranslationServiceImpl(settingService, exoFeatureService);

    assertNull(translationService.getActiveConnector());

  }

  @Test
  public void testSetNonExistingActiveConnector() {
    System.setProperty("exo.feature." + FEATURE_NAME + ".enabled", "true");

    AutomaticTranslationService translationService = new AutomaticTranslationServiceImpl(settingService, exoFeatureService);
    String connectorName = "google";

    assertFalse(translationService.setActiveConnector(connectorName));
    System.setProperty("exo.feature." + FEATURE_NAME + ".enabled", "");
  }

  @Test
  public void testSetActiveConnectorWhenFeatureNotActive() {
    AutomaticTranslationService translationService = new AutomaticTranslationServiceImpl(settingService, exoFeatureService);
    String connectorName = "google";
    AutomaticTranslationComponentPlugin translationConnector = new AutomaticTranslationComponentPlugin(settingService);
    translationConnector.setName("google");
    translationConnector.setDescription("google");

    translationService.addConnector(translationConnector);

    assertFalse(translationService.setActiveConnector(connectorName));

  }

  @Test
  public void testUnSetConnector() {
    System.setProperty("exo.feature." + FEATURE_NAME + ".enabled", "true");

    AutomaticTranslationService translationService = new AutomaticTranslationServiceImpl(settingService, exoFeatureService);
    String connectorName = "";

    assertTrue(translationService.setActiveConnector(connectorName));

    connectorName = null;
    assertTrue(translationService.setActiveConnector(connectorName));
    System.setProperty("exo.feature." + FEATURE_NAME + ".enabled", "");
  }

  @Test
  public void testSetApiKeyWhenConnectorNotExists() {
    AutomaticTranslationService translationService = new AutomaticTranslationServiceImpl(settingService, exoFeatureService);
    String connectorName = "google";

    assertFalse(translationService.setApiKey(connectorName, "123456"));

  }

  @Test
  public void testSetApiKeyWhenConnectorExists() {
    AutomaticTranslationService translationService = new AutomaticTranslationServiceImpl(settingService, exoFeatureService);
    String connectorName = "google";
    AutomaticTranslationComponentPlugin translationConnector = new AutomaticTranslationComponentPlugin(settingService);
    translationConnector.setName("google");
    translationConnector.setDescription("google");

    translationService.addConnector(translationConnector);

    assertTrue(translationService.setApiKey(connectorName, "123456"));
    verify(settingService, times(1)).set(eq(Context.GLOBAL),
                                         eq(Scope.GLOBAL),
                                         eq(AUTOMATIC_TRANSLATION_API_KEY + "-" + connectorName),
                                         argThat(argument -> argument.getValue().equals("123456")));

  }
}
