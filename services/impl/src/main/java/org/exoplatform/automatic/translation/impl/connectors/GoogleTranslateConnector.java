package org.exoplatform.automatic.translation.impl.connectors;

import org.exoplatform.automatic.translation.api.AutomaticTranslationComponentPlugin;
import org.exoplatform.commons.api.settings.SettingService;

public class GoogleTranslateConnector extends AutomaticTranslationComponentPlugin {

  public GoogleTranslateConnector(SettingService settingService) {
    super(settingService);
  }
}
