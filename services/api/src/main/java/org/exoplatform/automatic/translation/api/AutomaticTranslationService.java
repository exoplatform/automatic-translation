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
package org.exoplatform.automatic.translation.api;

import org.exoplatform.automatic.translation.api.dto.AutomaticTranslationConfiguration;

import java.util.Locale;
import java.util.Map;

public interface AutomaticTranslationService {

  /**
   * Add a translation connector
   *
   * @param translationConnector The connector to add
   */
  void addConnector(AutomaticTranslationComponentPlugin translationConnector);

  /**
   * Get available connectors list
   *
   * @return The connector list
   */
  Map<String, AutomaticTranslationComponentPlugin> getConnectors();

  /**
   * Get the actual configuration
   *
   * @return The configuration
   */
  AutomaticTranslationConfiguration getConfiguration();

  /**
   * Get current Active connector
   *
   * @return The connector name
   */
  String getActiveConnector();

  /**
   * Set the active connector
   *
   * @param name The connector name to activate
   */
  void setActiveConnector(String name);

  /**
   * Set the apiKey for the provided connector
   *
   * @param connector The connector name
   * @param apikey The apikey to set
   */
  void setApiKey(String connector, String apikey);

  /**
   * Translate a message in the provided locale, by using the active connector
   *
   * @param message The message to translate
   * @param targetLang The locale in which we want to translate
   * @param contentType The type of the content translated (activity, comment,
   *          news ...)
   * @param spaceId The space in which the content is present
   * @return The translated message
   */
  String translate(String message, Locale targetLang, String contentType, long spaceId);

  /**
   * Return if the feature is active
   *
   * @return true if the feature is active
   */
  boolean isFeatureActive();
}
