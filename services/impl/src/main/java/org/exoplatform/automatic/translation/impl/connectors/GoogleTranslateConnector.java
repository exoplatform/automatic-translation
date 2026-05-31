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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.json.JSONObject;

import org.exoplatform.automatic.translation.api.AutomaticTranslationComponentPlugin;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class GoogleTranslateConnector extends AutomaticTranslationComponentPlugin {

  private static final Log          LOG                      = ExoLogger.getLogger(GoogleTranslateConnector.class);

  private static final String       GOOGLE_TRANSLATE_SERVICE = "google-translate";

  private static final String       API_URL                  = "https://translation.googleapis.com/language/translate/v2";

  private static final String       KEY_PARAM                = "key";

  private static final int          DEFAULT_POOL_CONNECTION  = 100;

  private static final String       ERROR                    = "error";

  private final CloseableHttpClient httpClient;

  public GoogleTranslateConnector(SettingService settingService) {
    super(settingService);

    PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
    connectionManager.setDefaultMaxPerRoute(DEFAULT_POOL_CONNECTION);
    connectionManager.setMaxTotal(DEFAULT_POOL_CONNECTION);
    this.httpClient = HttpClients.custom()
                                 .setConnectionManager(connectionManager)
                                 .build();
  }

  @Override
  public String translate(String message, Locale targetLocale) {
    long startTime = System.currentTimeMillis();

    String serviceUrl = API_URL + "?" + KEY_PARAM + "=" + getApiKey();

    // We use targetLocale.getLanguage() instead of targetLocale.toLanguageTag()
    // because
    // Google Cloud Translation supports only the language and not the country,
    // except for Chinese. As "zh" corresponds to simplified Chinese, we can
    // deal with that.
    // https://cloud.google.com/translate/docs/languages
    String targetLanguage = targetLocale.getLanguage();

    try {
      JSONObject requestBody = new JSONObject();
      requestBody.put("q", message);
      requestBody.put("target", targetLanguage);

      HttpPost request = new HttpPost(serviceUrl);
      request.setEntity(new StringEntity(requestBody.toString(), ContentType.APPLICATION_JSON));

      HttpResult httpResult = execute(request);
      int statusCode = httpResult.statusCode();

      if (statusCode == HttpURLConnection.HTTP_OK) {
        JSONObject jsonResponse = new JSONObject(httpResult.body());
        return jsonResponse.getJSONObject("data")
                           .getJSONArray("translations")
                           .getJSONObject(0)
                           .getString("translatedText");
      }

      String errorMessage = getErrorMessage(httpResult.body(), "Error when calling Google Translation API");

      LOG.error("remote_service={} operation={} parameters=\"message length:{},targetLocale:{}\" status=ko " +
          "duration_ms={} error_msg=\"{}, status : {} \"",
                GOOGLE_TRANSLATE_SERVICE,
                "translate",
                message.length(),
                targetLanguage,
                System.currentTimeMillis() - startTime,
                errorMessage,
                statusCode);
      return null;
    } catch (Exception e) {
      LOG.error("Error when trying to send translation request to Google Translation API", e);
    }
    return null;
  }

  private HttpResult execute(ClassicHttpRequest request) throws IOException {
    return httpClient.execute(request, this::toHttpResult);
  }

  private HttpResult toHttpResult(ClassicHttpResponse response) throws IOException, ParseException {
    String body = response.getEntity() == null ? "" : EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
    return new HttpResult(response.getCode(), body);
  }

  private String getErrorMessage(String responseBody, String defaultMessage) {
    try {
      JSONObject jsonResponse = new JSONObject(responseBody);
      JSONObject error = jsonResponse.optJSONObject(ERROR);
      return error == null ? defaultMessage : error.optString("message", defaultMessage);
    } catch (Exception e) {
      return defaultMessage;
    }
  }

  private record HttpResult(int statusCode, String body) {
  }

}
