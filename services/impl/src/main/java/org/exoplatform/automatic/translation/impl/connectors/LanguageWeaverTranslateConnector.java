/*
 * Copyright (C) 2026 eXo Platform SAS.
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
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.json.JSONObject;

import org.exoplatform.automatic.translation.api.AutomaticTranslationComponentPlugin;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.resources.LocaleConfigService;

public class LanguageWeaverTranslateConnector extends AutomaticTranslationComponentPlugin {

  private static final Log             LOG                     = ExoLogger.getLogger(LanguageWeaverTranslateConnector.class);

  private static final String          LW_TRANSLATE_SERVICE    = "language-weaver-translate";

  public static final String           LW_URL                  = "lwUrl";

  private static final int             DEFAULT_POOL_CONNECTION = 20;

  public static final String           ERROR                   = "error";

  public static final String           MESSAGE                 = "message";

  private final CloseableHttpClient    httpClient;

  private final LocaleConfigService    localeConfigService;

  private String                       languageWeaverUrl;

  private String                       translateUrl            = "/api/v2/translations/quick";                               // only
                                                                                                                             // for
                                                                                                                             // cloud
                                                                                                                             // version

  private String                       languagePairUrl         = "/api/v2/language-pairs";                                   // only
                                                                                                                             // for
                                                                                                                             // cloud
                                                                                                                             // version

  private volatile Map<String, String> languageCode;

  public LanguageWeaverTranslateConnector(SettingService settingService,
                                          LocaleConfigService localeConfigService,
                                          InitParams initParams) {
    super(settingService);
    this.localeConfigService = localeConfigService;

    if (initParams.containsKey(LW_URL) && StringUtils.isNotBlank(initParams.getValueParam(LW_URL).getValue())) {
      languageWeaverUrl = initParams.getValueParam(LW_URL).getValue();
    }

    PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
    connectionManager.setDefaultMaxPerRoute(DEFAULT_POOL_CONNECTION);
    connectionManager.setMaxTotal(DEFAULT_POOL_CONNECTION);
    this.httpClient = HttpClients.custom()
                                 .setConnectionManager(connectionManager)
                                 .build();
  }

  private void initLanguageCode() {
    if (languageCode != null) {
      return;
    }

    Map<String, String> loadedLanguageCode = new HashMap<>();
    String serviceUrl = languageWeaverUrl + languagePairUrl;
    String basicToken = toBasicToken(getApiKey());

    try {
      long startTime = System.currentTimeMillis();

      HttpGet request = new HttpGet(serviceUrl);
      request.setHeader("Authorization", "Basic " + basicToken);

      HttpResult httpResult = execute(request);
      int statusCode = httpResult.statusCode();

      if (statusCode == HttpURLConnection.HTTP_OK) {
        JSONObject jsonResponse = new JSONObject(httpResult.body());
        for (Object obj : jsonResponse.getJSONArray("languagePairs")) {
          JSONObject langPair = (JSONObject) obj;
          String targetLanguageId = langPair.getString("targetLanguageId");
          String targetLanguageTag = langPair.getString("targetLanguageTag");
          loadedLanguageCode.put(targetLanguageTag, targetLanguageId);
        }
        languageCode = loadedLanguageCode;
      } else {
        String errorMessage = getErrorMessage(httpResult.body(), "Error when calling Language Weaver API");

        LOG.error("remote_service={} operation={} status=ko " + "duration_ms={} error_msg=\"{}, status : {} \"",
                  LW_TRANSLATE_SERVICE,
                  "getLanguagePairs",
                  System.currentTimeMillis() - startTime,
                  errorMessage,
                  statusCode);
      }
    } catch (Exception e) {
      LOG.error("Error when trying to get language pairs from Language Weaver API", e);
    }
  }

  @Override
  public String translate(String message, Locale targetLocale) {
    initLanguageCode();

    if (languageCode == null) {
      LOG.error("Language Weaver language pairs are not initialized");
      return null;
    }

    long startTime = System.currentTimeMillis();

    String serviceUrl = languageWeaverUrl + translateUrl;
    String base64EncodedMessage = Base64.getEncoder().encodeToString(message.getBytes(StandardCharsets.UTF_8));
    String localeCode = languageCode.get(targetLocale.toString());

    // Code used by Language Weaver. If not found, use ISO3 language code.
    // Example: Ger instead of Deu.
    String targetLocaleCode = "Aut" + StringUtils.capitalize(localeCode != null ? localeCode : targetLocale.getISO3Language());
    String basicToken = toBasicToken(getApiKey());

    try {
      HttpPost request = new HttpPost(serviceUrl);
      request.setHeader("Authorization", "Basic " + basicToken);

      List<NameValuePair> params = new ArrayList<>(2);
      params.add(new BasicNameValuePair("languagePairId", targetLocaleCode));
      params.add(new BasicNameValuePair("input", base64EncodedMessage));

      request.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));

      HttpResult httpResult = execute(request);
      int statusCode = httpResult.statusCode();

      if (statusCode == HttpURLConnection.HTTP_OK) {
        JSONObject jsonResponse = new JSONObject(httpResult.body());
        return new String(Base64.getDecoder().decode(jsonResponse.getString("translation")), StandardCharsets.UTF_8);
      }

      JSONObject jsonResponse = new JSONObject(httpResult.body());
      String errorMessage = getErrorMessage(jsonResponse, "Error when calling Language Weaver API");

      if (isAutoDetectLanguagePairError(jsonResponse)) {
        Locale defaultLocale = localeConfigService.getDefaultLocaleConfig().getLocale();
        if (!targetLocale.equals(defaultLocale)) {
          return translate(message, defaultLocale);
        }
      }

      LOG.error("remote_service={} operation={} parameters=\"message length:{},targetLocale:{}\" status=ko " +
          "duration_ms={} error_msg=\"{}, status : {} \"",
                LW_TRANSLATE_SERVICE,
                "translate",
                message.length(),
                targetLocale.getLanguage(),
                System.currentTimeMillis() - startTime,
                errorMessage,
                statusCode);
      return null;
    } catch (Exception e) {
      LOG.error("Error when trying to send translation request to Language Weaver API", e);
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

  private String toBasicToken(String apiKey) {
    return Base64.getEncoder().encodeToString((apiKey + ":").getBytes(StandardCharsets.UTF_8));
  }

  private String getErrorMessage(String responseBody, String defaultMessage) {
    try {
      return getErrorMessage(new JSONObject(responseBody), defaultMessage);
    } catch (Exception e) {
      return defaultMessage;
    }
  }

  private String getErrorMessage(JSONObject jsonResponse, String defaultMessage) {
    JSONObject error = jsonResponse.optJSONObject(ERROR);
    if (error == null) {
      return defaultMessage;
    }
    return error.optString("details", error.optString(MESSAGE, defaultMessage));
  }

  private boolean isAutoDetectLanguagePairError(JSONObject jsonResponse) {
    JSONObject error = jsonResponse.optJSONObject(ERROR);
    return error != null
           && "failed to auto-detect source language and find a matching language pair".equals(error.optString(MESSAGE));
  }

  private record HttpResult(int statusCode, String body) {
  }

}
