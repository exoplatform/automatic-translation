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
import org.exoplatform.commons.api.settings.ExoFeatureService;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AutomaticTranslationServiceImpl implements AutomaticTranslationService {

  private static final String                              AUTOMATIC_TRANSLATION_ACTIVE_CONNECTOR =
                                                                                                  "automaticTranslationActiveConnector";

  private static final String                              FEATURE_NAME                           = "automatic-translation";

  private Map<String, AutomaticTranslationComponentPlugin> translationConnectors;

  SettingService                                           settingService;

  ExoFeatureService                                        exoFeatureService;

  public AutomaticTranslationServiceImpl(SettingService settingService, ExoFeatureService exoFeatureService) {
    this.translationConnectors = new HashMap<>();
    this.settingService = settingService;
    this.exoFeatureService = exoFeatureService;
  }

  @Override
  public void addConnector(AutomaticTranslationComponentPlugin translationConnector) {
    this.translationConnectors.put(translationConnector.getName(), translationConnector);
  }

  @Override
  public Map<String, AutomaticTranslationComponentPlugin> getConnectors() {
    return this.translationConnectors;
  }

  @Override
  public AutomaticTranslationConfiguration getConfiguration() {
    String activeConnector = getActiveConnector();
    AutomaticTranslationConfiguration configuration = new AutomaticTranslationConfiguration(new ArrayList<>(), activeConnector);
    getConnectors().forEach((name, connector) -> {
      if (name.equals(activeConnector)) {
        configuration.setActiveApiKey(connector.getApiKey());
        configuration.addConnector(name, connector.getDescription(), connector.getApiKey());
      } else {
        configuration.addConnector(name, connector.getDescription(), null);
      }
    });
    return configuration;
  }

  @Override
  public String getActiveConnector() {
    SettingValue<?> value = settingService.get(Context.GLOBAL, Scope.GLOBAL, AUTOMATIC_TRANSLATION_ACTIVE_CONNECTOR);

    return value != null && value.getValue() != null && !value.getValue().toString().isEmpty()
        && isActiveFeature() ? value.getValue().toString() : null;
  }

  private boolean isActiveFeature() {
    return exoFeatureService.isActiveFeature(FEATURE_NAME);
  }

  @Override
  public void setActiveConnector(String name) {
    if (!isActiveFeature()) {
      throw new RuntimeException("Unable to change automatic translation connector as feature is not active");
    }
    // should return false if connector name do not exists
    // but not if the name is empty (correspond to unset case)
    if (name == null) {
      name = "";
    }
    String finalName = name;
    if (finalName.isBlank()
        || translationConnectors.keySet().stream().anyMatch(connectorName -> connectorName.equals(finalName))) {
      settingService.set(Context.GLOBAL, Scope.GLOBAL, AUTOMATIC_TRANSLATION_ACTIVE_CONNECTOR, new SettingValue<>(name));
    } else {
      throw new RuntimeException("Unable to change automatic translation connector as provided connector name do not exists");
    }
  }

  @Override
  public void setApiKey(String connector, String apikey) {
    AutomaticTranslationComponentPlugin connectorPlugin = translationConnectors.get(connector);
    if (connectorPlugin == null) {
      throw new RuntimeException("Provided connector do not exists " + connector);
    } else {
      connectorPlugin.setApiKey(apikey);
    }
  }

  @Override
  public String translate(String message, Locale targetLang) {
    String activeConnector = getActiveConnector();
    if (activeConnector == null) {
      throw new RuntimeException("Automatic Translation No active connector configured");
    }
    if (translationConnectors.get(activeConnector) == null) {
      throw new RuntimeException("Automatic Translation Connector " + activeConnector + " not found");
    }
    return translationConnectors.get(activeConnector).translate(message, targetLang);
  }

}
