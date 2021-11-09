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
package org.exoplatform.automatic.translation.rest;

import org.exoplatform.automatic.translation.api.AutomaticTranslationComponentPlugin;
import org.exoplatform.automatic.translation.api.AutomaticTranslationService;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AutomaticTranslationRestServiceTest {

  AutomaticTranslationRestService automaticTranslationRestService;

  @Mock
  AutomaticTranslationService automaticTranslationService;

  @Mock
  SettingService settingService;

  @Before
  public void setUp() {
    automaticTranslationService=mock(AutomaticTranslationService.class);
    settingService=mock(SettingService.class);
    automaticTranslationRestService = new AutomaticTranslationRestService(automaticTranslationService);
  }
  private void startSessionAs(String username) {
    Identity identity = new Identity(username);
    ConversationState state = new ConversationState(identity);
    ConversationState.setCurrent(state);
  }

  @Test
  public void testGetConfiguration() {
    Map<String, AutomaticTranslationComponentPlugin> connectors = new HashMap<>();
    AutomaticTranslationComponentPlugin googleConnector = new AutomaticTranslationComponentPlugin(settingService);
    googleConnector.setName("google");
    googleConnector.setDescription("google connector description");
    connectors.put("google",googleConnector);
    AutomaticTranslationComponentPlugin systranConnector = new AutomaticTranslationComponentPlugin(settingService);
    systranConnector.setName("systran");
    systranConnector.setDescription("systran connector description");
    connectors.put("systran",systranConnector);
    when(automaticTranslationService.getConnectors()).thenReturn(connectors);
    when(automaticTranslationService.getActiveConnector()).thenReturn("google");
    when(automaticTranslationService.isFeatureActive()).thenReturn(true);

    Response response = automaticTranslationRestService.configuration();
    assertEquals(200,response.getStatus());
    assertEquals("{\"connectors\":[{\"name\":\"google\",\"description\":\"google connector description\"},"
                     + "{\"name\":\"systran\",\"description\":\"systran connector description\"}],"
                     + "\"activeApiKey\":null,\"active\":\"google\",\"isActive\":true}",
                 response.getEntity());
  }
  @Test
  public void testSetActiveConnectorWhenOk() {
    when(automaticTranslationService.setActiveConnector(any())).thenReturn(true);
    Response response = automaticTranslationRestService.setActiveConnector("google");
    assertEquals(200,response.getStatus());
  }
  @Test
  public void testSetActiveConnectorWhenKo() {
    when(automaticTranslationService.setActiveConnector(any())).thenReturn(false);
    Response response = automaticTranslationRestService.setActiveConnector("google");
    assertEquals(403,response.getStatus());
  }

  @Test
  public void testSetApiKeyWhenOk() {
    when(automaticTranslationService.setApiKey(any(),any())).thenReturn(true);
    Response response = automaticTranslationRestService.setApiKey("google","123456");
    assertEquals(200,response.getStatus());
  }
  @Test
  public void testSetApiKeyWhenKo() {
    when(automaticTranslationService.setApiKey(any(),any())).thenReturn(false);
    Response response = automaticTranslationRestService.setApiKey("google","123456");
    assertEquals(403,response.getStatus());
  }




}
