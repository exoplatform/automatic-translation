package org.exoplatform.automatic.translation.api;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.container.component.BaseComponentPlugin;

public class AutomaticTranslationComponentPlugin extends BaseComponentPlugin {

  private static final String AUTOMATIC_TRANSLATION_API_KEY = "automaticTranslationApiKey";

  protected SettingService settingService;
  public AutomaticTranslationComponentPlugin (SettingService settingService) {
    super();
    this.settingService = settingService;
  }

  public boolean setApiKey(String apiKey) {
    SettingValue settingValue = new SettingValue(apiKey);
    settingService.set(Context.GLOBAL, Scope.GLOBAL,AUTOMATIC_TRANSLATION_API_KEY+"-"+this.getName(),settingValue);
    return true;
  }

  public String getApiKey() {
    SettingValue setting = settingService.get(Context.GLOBAL, Scope.GLOBAL,AUTOMATIC_TRANSLATION_API_KEY+"-"+this.getName());
    if (setting!=null && setting.getValue()!=null) {
      return setting.getValue().toString();
    } else {
      return null;
    }

  }


}
