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
import org.exoplatform.automatic.translation.api.AutomaticTranslationComponentPlugin;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import java.util.Locale;

public class DeepLTranslateConnector extends AutomaticTranslationComponentPlugin {

  private static final Log             LOG                     = ExoLogger.getLogger(DeepLTranslateConnector.class);

  private static final String          DEEPL_TRANSLATE_SERVICE = "deepl-translate";

  private Translator                   translator;

  private final TextTranslationOptions textTranslationOptions;

  public DeepLTranslateConnector(SettingService settingService) {
    super(settingService);
    textTranslationOptions = new TextTranslationOptions();
    textTranslationOptions.setTagHandling("html");
    textTranslationOptions.setPreserveFormatting(true);
    textTranslationOptions.setSentenceSplittingMode(SentenceSplittingMode.All);
  }

  @Override
  public String translate(String message, Locale targetLocale) {
    long startTime = System.currentTimeMillis();
    try {
      return getDeeplTranslator().translateText(message, null, getLocaleLanguage(targetLocale), textTranslationOptions).getText();
    } catch (DeepLException e) {
      LOG.error("remote_service={} operation={} parameters=\"message length:{},targetLocale:{}\" status=ko "
          + "duration_ms={} error_msg=\"{}\"",
                DEEPL_TRANSLATE_SERVICE,
                "translate",
                message.length(),
                targetLocale.getLanguage(),
                System.currentTimeMillis() - startTime,
                e.getMessage());
    } catch (InterruptedException e) {
      LOG.error("DeepL API translation thread has been interrupted", e);
      Thread.currentThread().interrupt();
    }
    return null;
  }

  private String getLocaleLanguage(Locale locale) {
    if (locale.getLanguage().equalsIgnoreCase("en")) {
      return "en-US";
    } else if (locale.getLanguage().equalsIgnoreCase("pt")) {
      return "pt-PT";
    } else {
      return locale.getLanguage();
    }
  }

  private Translator getDeeplTranslator() {
    if (translator == null) {
      translator = new Translator(getApiKey());
    }
    return translator;
  }
}
