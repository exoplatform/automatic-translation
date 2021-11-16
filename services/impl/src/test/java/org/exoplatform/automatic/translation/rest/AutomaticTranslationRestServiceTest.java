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
import org.exoplatform.automatic.translation.api.dto.AutomaticTranslationConfiguration;
import org.exoplatform.common.http.HTTPStatus;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AutomaticTranslationRestServiceTest {

  AutomaticTranslationRestService automaticTranslationRestService;

  @Mock
  AutomaticTranslationService     automaticTranslationService;

  @Mock
  SettingService                  settingService;

  @Before
  public void setUp() {
    automaticTranslationService = mock(AutomaticTranslationService.class);
    settingService = mock(SettingService.class);
    automaticTranslationRestService = new AutomaticTranslationRestService(automaticTranslationService);
  }

  private void startSessionAs(String username) {
    Identity identity = new Identity(username);
    ConversationState state = new ConversationState(identity);
    ConversationState.setCurrent(state);
  }
  @Test
  public void testSetActiveConnectorWhenOk() {
    doNothing().when(automaticTranslationService).setActiveConnector(any());
    Response response = automaticTranslationRestService.setActiveConnector("google");
    assertEquals(HTTPStatus.NO_CONTENT, response.getStatus());
  }

  @Test
  public void testSetActiveConnectorWhenKo() {
    doThrow(new RuntimeException("Error")).when(automaticTranslationService).setActiveConnector(any());
    Response response = automaticTranslationRestService.setActiveConnector("google");
    assertEquals(HTTPStatus.BAD_REQUEST, response.getStatus());
  }

  @Test
  public void testSetApiKeyWhenOk() {
    doNothing().when(automaticTranslationService).setApiKey(any(), any());
    Response response = automaticTranslationRestService.setApiKey("google", "123456");
    assertEquals(HTTPStatus.NO_CONTENT, response.getStatus());
  }

  @Test
  public void testSetApiKeyWhenKo() {
    doThrow(new RuntimeException("Error")).when(automaticTranslationService).setApiKey(any(), any());
    Response response = automaticTranslationRestService.setApiKey("google", "123456");
    assertEquals(HTTPStatus.BAD_REQUEST, response.getStatus());
  }

}
