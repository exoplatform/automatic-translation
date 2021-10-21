package org.exoplatform.automatic.translation.api;

import java.util.Map;

public interface AutomaticTranslationService {

  void addConnector(AutomaticTranslationComponentPlugin translationConnector);
  Map<String,AutomaticTranslationComponentPlugin> getConnectors();
  String getActiveConnector();
  boolean setActiveConnector(String name);
  //boolean isFeatureEnabled();
  //String translate(String message, Locale targetLang);
  //void activateConnector(String name);
  //String getActiveConnector();
}
