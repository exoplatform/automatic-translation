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
package org.exoplatform.automatic.translation.impl.connectors;

import org.apache.commons.io.IOUtils;
import org.exoplatform.automatic.translation.api.AutomaticTranslationComponentPlugin;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class GoogleTranslateConnector extends AutomaticTranslationComponentPlugin {

  private static final Log    LOG                      = ExoLogger.getLogger(GoogleTranslateConnector.class);

  private static final String GOOGLE_TRANSLATE_SERVICE = "google-translate";

  private static final String API_URL                  = "https://translation.googleapis.com/language/translate/v2";

  private static final String KEY_PARAM                = "key";

  private static final String DATA_PATTERN             = "{'q': '{message}', 'target': '{targetLocale}'}";

  public GoogleTranslateConnector(SettingService settingService) {
    super(settingService);
  }

  @Override
  public String translate(String message, Locale targetLocale) {
    long startTime = System.currentTimeMillis();

    String serviceUrl = API_URL + "?" + KEY_PARAM + "=" + getApiKey();
    String data = DATA_PATTERN.replace("{message}", message).replace("{targetLocale}", targetLocale.getLanguage());
    try {
      URL url = new URL(serviceUrl);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("POST");
      connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
      connection.setRequestProperty("Content-Length", String.valueOf(data.getBytes(StandardCharsets.UTF_8)));
      connection.setDoOutput(true);
      connection.setDoInput(true);

      OutputStream outputStream = connection.getOutputStream();
      outputStream.write(data.getBytes(StandardCharsets.UTF_8));

      connection.connect();
      int responseCode = connection.getResponseCode();
      if (responseCode == HttpURLConnection.HTTP_OK) {

        // read the response
        InputStream in = new BufferedInputStream(connection.getInputStream());
        String response = IOUtils.toString(in, StandardCharsets.UTF_8);
        JSONObject jsonResponse = new JSONObject(response);
        return jsonResponse.getJSONObject("data").getJSONArray("translations").getJSONObject(0).getString("translatedText");
      } else {
        LOG.error("remote_service={} operation={} parameters=\"message length:{},targetLocale:{}\" status=ko "
            + "duration_ms={} error_msg=\"Error sending translation request, status : {} \"",
                  GOOGLE_TRANSLATE_SERVICE,
                  "translate",
                  message.length(),
                  targetLocale.getLanguage(),
                  System.currentTimeMillis() - startTime,
                  responseCode);
        return null;
      }
    } catch (Exception e) {
      LOG.error("Error when trying to send translation request to google API");
    }
    return null;
  }
}
