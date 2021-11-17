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
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.exoplatform.automatic.translation.api.AutomaticTranslationComponentPlugin;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class GoogleTranslateConnector extends AutomaticTranslationComponentPlugin {

  private static final Log    LOG                      = ExoLogger.getLogger(GoogleTranslateConnector.class);

  private static final String GOOGLE_TRANSLATE_SERVICE = "google-translate";

  private static final String API_URL                  = "https://translation.googleapis.com/language/translate/v2";

  private static final String KEY_PARAM                = "key";

  private static final String DATA_PATTERN             = "{'q': '{message}', 'target': '{targetLocale}'}";

  private static final int    DEFAULT_POOL_CONNECTION  = 100;

  private HttpClient          httpClient;

  public GoogleTranslateConnector(SettingService settingService) {
    super(settingService);

    PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
    connectionManager.setDefaultMaxPerRoute(DEFAULT_POOL_CONNECTION);
    HttpClientBuilder httpClientBuilder = HttpClients.custom()
                                                     .setConnectionManager(connectionManager)
                                                     .setConnectionReuseStrategy(new DefaultConnectionReuseStrategy())
                                                     .setMaxConnPerRoute(DEFAULT_POOL_CONNECTION);
    this.httpClient = httpClientBuilder.build();
  }

  @Override
  public String translate(String message, Locale targetLocale) {
    long startTime = System.currentTimeMillis();

    String serviceUrl = API_URL + "?" + KEY_PARAM + "=" + getApiKey();
    // we use here targetLocale.getLanguage instead of targetLocale.toLanguageTag as
    // Google Cloud Translation supports
    // only the language and not the country,
    // except for chinese. But as zh correspond to simplified chinese, we can deal
    // with that :
    // https://cloud.google.com/translate/docs/languages
    String data = DATA_PATTERN.replace("{message}", message).replace("{targetLocale}", targetLocale.getLanguage());
    try {
      HttpPost httpTypeRequest = new HttpPost(serviceUrl);
      httpTypeRequest.setEntity(new StringEntity(data, ContentType.APPLICATION_JSON));
      HttpResponse httpResponse = httpClient.execute(httpTypeRequest);
      String response = null;
      int statusCode = httpResponse.getStatusLine().getStatusCode();
      if (statusCode == HttpURLConnection.HTTP_OK) {

        // read the response
        if (httpResponse.getEntity() != null) {
          try (InputStream is = httpResponse.getEntity().getContent()) {
            response = IOUtils.toString(is, StandardCharsets.UTF_8);
          }
        }

        JSONObject jsonResponse = new JSONObject(response);
        return jsonResponse.getJSONObject("data").getJSONArray("translations").getJSONObject(0).getString("translatedText");

      } else {
        String errorMessage = "Error when calling Google Api Translation";
        try (InputStream is = httpResponse.getEntity().getContent()) {
          JSONObject jsonResponse = new JSONObject(IOUtils.toString(is, StandardCharsets.UTF_8));
          if (jsonResponse.getJSONObject("error") != null && jsonResponse.getJSONObject("error").getString("message") != null) {
            errorMessage = jsonResponse.getJSONObject("error").getString("message");
          }
        }
        LOG.error("remote_service={} operation={} parameters=\"message length:{},targetLocale:{}\" status=ko "
            + "duration_ms={} error_msg=\"{}, status : {} \"",
                  GOOGLE_TRANSLATE_SERVICE,
                  "translate",
                  message.length(),
                  targetLocale.getLanguage(),
                  System.currentTimeMillis() - startTime,
                  errorMessage,
                  statusCode);
        return null;
      }

    } catch (Exception e) {
      LOG.error("Error when trying to send translation request to google API", e);
    }
    return null;
  }

}
