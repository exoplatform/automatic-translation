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

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.container.component.BaseComponentPlugin;

import java.util.Locale;

public abstract class AutomaticTranslationComponentPlugin extends BaseComponentPlugin {

  private static final String AUTOMATIC_TRANSLATION_API_KEY = "automaticTranslationApiKey";

  protected SettingService    settingService;

  public AutomaticTranslationComponentPlugin(SettingService settingService) {
    this.settingService = settingService;
  }

  public void setApiKey(String apiKey) {
    SettingValue<String> settingValue = new SettingValue<>(apiKey);
    settingService.set(Context.GLOBAL, Scope.GLOBAL, AUTOMATIC_TRANSLATION_API_KEY + "-" + this.getName(), settingValue);
  }

  public String getApiKey() {
    SettingValue<?> setting = settingService.get(Context.GLOBAL,
                                                 Scope.GLOBAL,
                                                 AUTOMATIC_TRANSLATION_API_KEY + "-" + this.getName());
    if (setting != null && setting.getValue() != null) {
      return setting.getValue().toString();
    } else {
      return null;
    }

  }

  public String translate(String message, Locale targetLocale) {
    throw new UnsupportedOperationException();
  }

}
