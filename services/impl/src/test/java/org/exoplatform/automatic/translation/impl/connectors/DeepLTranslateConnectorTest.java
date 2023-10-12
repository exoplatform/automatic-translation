/*
 * Copyright (C) 2023 eXo Platform SAS.
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
package org.exoplatform.automatic.translation.impl.connectors;

import com.deepl.api.Translator;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Locale;

import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DeepLTranslateConnectorTest {

  @Mock
  private SettingService          settingService;

  @Mock
  private Translator              translator;

  private DeepLTranslateConnector deepLTranslateConnector;

  @Before
  public void setUp() {
    when(settingService.get(any(), any(), anyString())).thenAnswer(invocation -> SettingValue.create("key"));
    deepLTranslateConnector = new DeepLTranslateConnector(settingService);
  }

  @Test
  public void translate() {
    // Need real auth-key to have a result
    String text = deepLTranslateConnector.translate("<h1>Bonjour</h1>", new Locale("en"));
    assertNull(text);
  }
}
