package org.exoplatform.automatic.translation.impl;

import org.exoplatform.automatic.translation.api.AutomaticTranslationComponentPlugin;
import org.exoplatform.automatic.translation.api.AutomaticTranslationService;
import org.exoplatform.commons.api.settings.ExoFeatureService;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;

import java.util.HashMap;
import java.util.Map;

public class AutomaticTranslationServiceImpl implements AutomaticTranslationService {

  private static final String AUTOMATIC_TRANSLATION_ACTIVE_CONNECTOR = "automaticTranslationActiveConnector";

  private static final String FEATURE_NAME = "automatic-translation";

  private Map<String, AutomaticTranslationComponentPlugin> translationConnectors;

  SettingService settingService;
  ExoFeatureService exoFeatureService;

  public AutomaticTranslationServiceImpl(SettingService settingService, ExoFeatureService exoFeatureService) {
    this.translationConnectors=new HashMap<>();
    this.settingService=settingService;
    this.exoFeatureService=exoFeatureService;
  }

  @Override
  public void addConnector(AutomaticTranslationComponentPlugin translationConnector) {
    this.translationConnectors.put(translationConnector.getName(),translationConnector);
  }

  @Override
  public Map<String, AutomaticTranslationComponentPlugin> getConnectors() {
    return this.translationConnectors;
  }

  @Override
  public String getActiveConnector() {
    SettingValue value = settingService.get(Context.GLOBAL, Scope.GLOBAL, AUTOMATIC_TRANSLATION_ACTIVE_CONNECTOR);


    return value != null && value.getValue()!=null && isActiveFeature() ?
           value.getValue().toString() : null;
  }

  private boolean isActiveFeature() {
    String enable = System.getProperty("exo.feature."+FEATURE_NAME+".enabled");
    return (enable!=null && !enable.isBlank() && enable.equals("true"));
    //after feature finished, return this :
    //return exoFeatureService.isActiveFeature(FEATURE_NAME);
  }

  @Override
  public boolean setActiveConnector(String name) {
    //should return false if connector name do not exists
    if (translationConnectors.keySet().stream().anyMatch(connectorName -> connectorName.equals(name))) {
      settingService.set(Context.GLOBAL, Scope.GLOBAL, AUTOMATIC_TRANSLATION_ACTIVE_CONNECTOR, new SettingValue<>(name));
      return true;
    } else {
      return false;
    }
  }

}
