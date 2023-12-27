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

import org.apache.commons.lang3.StringUtils;
import org.exoplatform.automatic.translation.api.AutomaticTranslationComponentPlugin;
import org.exoplatform.automatic.translation.api.AutomaticTranslationService;
import org.exoplatform.automatic.translation.api.dto.AutomaticTranslationConfiguration;
import org.exoplatform.automatic.translation.api.dto.AutomaticTranslationEvent;
import org.exoplatform.automatic.translation.api.dto.AutomaticTranslationFeaturesOptions;
import org.exoplatform.commons.api.settings.ExoFeatureService;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.ws.frameworks.json.JsonGenerator;
import org.exoplatform.ws.frameworks.json.JsonParser;
import org.exoplatform.ws.frameworks.json.impl.*;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AutomaticTranslationServiceImpl implements AutomaticTranslationService {

  private static final String                              AUTOMATIC_TRANSLATION_ACTIVE_CONNECTOR =
                                                                                                  "automaticTranslationActiveConnector";

  private static final String AUTOMATIC_TRANSLATION_FEATURES_OPTIONS =
          "automaticTranslationFeaturesOptions";

  private static final String                              EXO_TRANSLATION_EVENT_TRANSLATE        =
                                                                                           "exo.automatic-translation.event.translate";

  private static final String                              FEATURE_NAME                           = "automatic-translation";

  private Map<String, AutomaticTranslationComponentPlugin> translationConnectors;

  private static final Log                                 LOG                                    =
                                                               ExoLogger.getLogger(AutomaticTranslationServiceImpl.class);

  SettingService                                           settingService;

  ExoFeatureService                                        exoFeatureService;

  ListenerService                                          listenerService;

  public AutomaticTranslationServiceImpl(SettingService settingService,
                                         ExoFeatureService exoFeatureService,
                                         ListenerService listenerService) {
    this.translationConnectors = new HashMap<>();
    this.settingService = settingService;
    this.exoFeatureService = exoFeatureService;
    this.listenerService = listenerService;
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
    configuration.setFeaturesOptions(getFeaturesOptions());
    return configuration;
  }

  @Override
  public String getActiveConnector() {
    SettingValue<?> value = settingService.get(Context.GLOBAL, Scope.GLOBAL, AUTOMATIC_TRANSLATION_ACTIVE_CONNECTOR);

    return value != null && value.getValue() != null && !value.getValue().toString().isEmpty()
        && translationConnectors.get(value.getValue().toString()) != null ? value.getValue().toString() : null;
  }

  @Override
  public AutomaticTranslationFeaturesOptions getFeaturesOptions() {
    SettingValue<?> settingsValue = settingService.get(Context.GLOBAL, Scope.GLOBAL, AUTOMATIC_TRANSLATION_FEATURES_OPTIONS);
    String settingsValueString = settingsValue == null || settingsValue.getValue() == null ? null
            : settingsValue.getValue().toString();
    AutomaticTranslationFeaturesOptions featuresOptions = new AutomaticTranslationFeaturesOptions();
    if (StringUtils.isNotEmpty(settingsValueString)) {
      featuresOptions = fromJsonString(settingsValueString);
    }
    return featuresOptions;
  }

  @Override
  public void setFeaturesOptions(AutomaticTranslationFeaturesOptions featuresOptions) {
    String settingsString = toJsonString(featuresOptions);
    settingService.set(Context.GLOBAL, Scope.GLOBAL, AUTOMATIC_TRANSLATION_FEATURES_OPTIONS, SettingValue.create(settingsString));
  }


  @Override
  public boolean isFeatureActive() {
    boolean isActiveFeature = exoFeatureService.isActiveFeature(FEATURE_NAME);

    boolean isActiveConnector = getActiveConnector() != null;
    boolean haveApiKey = isActiveConnector && translationConnectors.get(getActiveConnector()).getApiKey() != null
        && !translationConnectors.get(getActiveConnector()).getApiKey().isEmpty();
    return isActiveFeature && isActiveConnector && haveApiKey;
  }

  @Override
  public void setActiveConnector(String name) {
    if (!exoFeatureService.isActiveFeature(FEATURE_NAME)) {
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
  public String translate(String message, Locale targetLang, String contentType, long spaceId) {
    String activeConnector = getActiveConnector();
    if (activeConnector == null) {
      throw new RuntimeException("Automatic Translation No active connector configured");
    }
    if (translationConnectors.get(activeConnector) == null) {
      throw new RuntimeException("Automatic Translation Connector " + activeConnector + " not found");
    }
    String translatedMessage = translationConnectors.get(activeConnector).translate(message, targetLang);
    if (translatedMessage != null) {
      try {
        AutomaticTranslationEvent event = new AutomaticTranslationEvent();
        event.setSpaceId(spaceId);
        event.setMessageLength(message.length());
        event.setTargetLanguage(targetLang.getLanguage());
        event.setConnectorName(translationConnectors.get(activeConnector).getName());
        event.setContentType(contentType);
        listenerService.broadcast(EXO_TRANSLATION_EVENT_TRANSLATE, this, event);
      } catch (Exception e) {
        LOG.error("Unable to broadcast event {}", EXO_TRANSLATION_EVENT_TRANSLATE, e);
      }

    }
    return translatedMessage;
  }

  private static String toJsonString(Object object) {
    try {
      JsonGenerator jsonGenerator = new JsonGeneratorImpl();
      return jsonGenerator.createJsonObject(object).toString();
    } catch (JsonException e) {
      throw new IllegalStateException("Error parsing object to string " + object, e);
    }
  }

  private static AutomaticTranslationFeaturesOptions fromJsonString(String value) {
    try {
      if (StringUtils.isBlank(value)) {
        return null;
      }
      JsonDefaultHandler jsonDefaultHandler = new JsonDefaultHandler();
      JsonParser jsonParser = new JsonParserImpl();
      jsonParser.parse(new ByteArrayInputStream(value.getBytes()), jsonDefaultHandler);
      return ObjectBuilder.createObject(AutomaticTranslationFeaturesOptions.class, jsonDefaultHandler.getJsonObject());
    } catch (JsonException e) {
      throw new IllegalStateException("Error creating object from string : " + value, e);
    }
  }




}
