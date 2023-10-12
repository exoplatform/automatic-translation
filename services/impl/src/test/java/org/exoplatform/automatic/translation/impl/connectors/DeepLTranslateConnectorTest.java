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

import com.deepl.api.*;
import org.exoplatform.commons.api.settings.SettingService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.util.Locale;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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
    deepLTranslateConnector = new DeepLTranslateConnector(settingService);
  }

  @Test
  public void translate() throws NoSuchFieldException, IllegalAccessException, DeepLException, InterruptedException {
    // Need real auth-key to have a real result
    TextTranslationOptions textTranslationOptions = new TextTranslationOptions();
    textTranslationOptions.setTagHandling("html");
    textTranslationOptions.setPreserveFormatting(true);
    textTranslationOptions.setSentenceSplittingMode(SentenceSplittingMode.All);
    TextResult textResult = new TextResult("<h1>Hello</h1>", "fr");
    Field textTranslationOptionsField = deepLTranslateConnector.getClass().getDeclaredField("textTranslationOptions");
    textTranslationOptionsField.setAccessible(true);
    textTranslationOptionsField.set(deepLTranslateConnector, textTranslationOptions);
    when(translator.translateText("<h1>Bonjour</h1>", null, "en-US", textTranslationOptions)).thenReturn(textResult);
    Field field = deepLTranslateConnector.getClass().getDeclaredField("translator");
    field.setAccessible(true);
    field.set(deepLTranslateConnector, translator);
    String text = deepLTranslateConnector.translate("<h1>Bonjour</h1>", new Locale("en"));
    assertNotNull(text);
    when(translator.translateText("<h1>Bonjour</h1>",
                                  null,
                                  "en-US",
                                  textTranslationOptions)).thenThrow(new DeepLException("error"));
    text = deepLTranslateConnector.translate("<h1>Bonjour</h1>", new Locale("en"));
    assertNull(text);

  }
}
