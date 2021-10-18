package org.exoplatform.automatic.translation.impl;

import org.exoplatform.automatic.translation.api.AutomaticTranslationComponentPlugin;
import org.exoplatform.automatic.translation.api.AutomaticTranslationService;

import java.util.HashMap;
import java.util.Map;

public class AutomaticTranslationServiceImpl implements AutomaticTranslationService {

  private Map<String, AutomaticTranslationComponentPlugin> translationConnectors;

  public AutomaticTranslationServiceImpl() {
    this.translationConnectors=new HashMap<>();
  }

  @Override
  public void addConnector(AutomaticTranslationComponentPlugin translationConnector) {
    this.translationConnectors.put(translationConnector.getName(),translationConnector);
  }

  @Override
  public Map<String, AutomaticTranslationComponentPlugin> getConnectors() {
    return this.translationConnectors;
  }
}
