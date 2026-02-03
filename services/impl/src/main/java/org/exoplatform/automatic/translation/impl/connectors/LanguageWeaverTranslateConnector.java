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

import org.apache.commons.lang3.StringUtils;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;
import org.exoplatform.automatic.translation.api.AutomaticTranslationComponentPlugin;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.resources.LocaleConfigService;
import org.json.JSONObject;

import javax.net.ssl.SSLContext;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LanguageWeaverTranslateConnector extends AutomaticTranslationComponentPlugin {

  private static final Log LOG = ExoLogger.getLogger(LanguageWeaverTranslateConnector.class);

  private static final String          LW_TRANSLATE_SERVICE = "language-weaver-translate";

  public static final String LW_URL = "lwUrl";

  private static final int        DEFAULT_POOL_CONNECTION = 20;

  public static final String ERROR = "error";

  public static final String MESSAGE = "message";

  private HttpClient httpClient;
  private String languageWeaverUrl;
  private String translateUrl = "/api/v2/translations/quick"; // only for cloud version
  private String languagePairUrl = "/api/v2/language-pairs"; // only for cloud version

  private Map<String, String> languageCode;

  private LocaleConfigService localeConfigService;

  public LanguageWeaverTranslateConnector(SettingService settingService, LocaleConfigService localeConfigService, InitParams initParams) {
    super(settingService);
    this.localeConfigService = localeConfigService;
    if (initParams.containsKey(LW_URL) && !StringUtils.isEmpty(initParams.getValueParam(LW_URL).getValue())) {
      languageWeaverUrl = initParams.getValueParam(LW_URL).getValue();
    }
    PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
    connectionManager.setDefaultMaxPerRoute(DEFAULT_POOL_CONNECTION);
    HttpClientBuilder httpClientBuilder = HttpClients.custom()
                                                     .setConnectionManager(connectionManager)
                                                     .setConnectionReuseStrategy(new DefaultConnectionReuseStrategy())
                                                     .setMaxConnPerRoute(DEFAULT_POOL_CONNECTION);

    this.httpClient = httpClientBuilder.build();
  }

  private void initLanguageCode() {
    if (languageCode == null) {
      languageCode = new HashMap<>();
      String serviceUrl = languageWeaverUrl + languagePairUrl;
      String basicToken = Base64.getEncoder().encodeToString((getApiKey()+":").getBytes());
      try {
        long startTime = System.currentTimeMillis();

        HttpGet httpTypeRequest = new HttpGet(serviceUrl);
        httpTypeRequest.setHeader("Authorization", "Basic " + basicToken);

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
          for (Object obj : jsonResponse.getJSONArray("languagePairs")) {
            JSONObject langPair = (JSONObject) obj;
            String targetLanguageId = langPair.getString("targetLanguageId");
            String targetLanguageTag = langPair.getString("targetLanguageTag");
            languageCode.put(targetLanguageTag, targetLanguageId);
          }
        } else {
          String errorMessage = "Error when calling Language Weaver API";
          try (InputStream is = httpResponse.getEntity().getContent()) {
            JSONObject jsonResponse = new JSONObject(IOUtils.toString(is, StandardCharsets.UTF_8));
            if (jsonResponse.getJSONObject(ERROR) != null && jsonResponse.getJSONObject(ERROR).getString(MESSAGE) != null) {
              errorMessage = jsonResponse.getJSONObject(ERROR).getString("details");
            }
          }
          LOG.error("remote_service={} operation={} status=ko "
                        + "duration_ms={} error_msg=\"{}, status : {} \"",
                    LW_TRANSLATE_SERVICE,
                    "getLanguagePairs",
                    System.currentTimeMillis() - startTime,
                    errorMessage,
                    statusCode);
          languageCode = null;
        }

      } catch (Exception e) {
        LOG.error("Error when trying to send translation request to Language Weaver API", e);
      }
    }

  }

  @Override
  public String translate(String message, Locale targetLocale) {

    initLanguageCode();

    long startTime = System.currentTimeMillis();

    String serviceUrl = languageWeaverUrl + translateUrl;
    String base64EncodedMessage = Base64.getEncoder().encodeToString(message.getBytes());
    String localeCode = languageCode.get(targetLocale.toString());

    //code used by language weaver if not found use ISO3 language code
    //example Ger instead of Deu
    String targetLocaleCode = "Aut"+StringUtils.capitalize(localeCode != null ? localeCode : targetLocale.getISO3Language());
    String basicToken = Base64.getEncoder().encodeToString((getApiKey()+":").getBytes());

    try {
      HttpPost httpTypeRequest = new HttpPost(serviceUrl);
      httpTypeRequest.setHeader("Authorization", "Basic " + basicToken);

      List<NameValuePair> params = new ArrayList<>(2);
      params.add(new BasicNameValuePair("languagePairId",targetLocaleCode));
      params.add(new BasicNameValuePair("input",base64EncodedMessage));

      UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(params, "UTF-8");
      httpTypeRequest.setEntity(urlEncodedFormEntity);

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
        return new String(Base64.getDecoder().decode(jsonResponse.getString("translation").getBytes()));

      } else {
        String errorMessage = "Error when calling Language Weaver API";

        try (InputStream is = httpResponse.getEntity().getContent()) {
          JSONObject jsonResponse = new JSONObject(IOUtils.toString(is, StandardCharsets.UTF_8));
          if (jsonResponse.getJSONObject(ERROR) != null && jsonResponse.getJSONObject(ERROR).getString(MESSAGE) != null) {
            errorMessage = jsonResponse.getJSONObject(ERROR).getString("details");
          }

          if (jsonResponse.getJSONObject(ERROR).getString(MESSAGE) != null && jsonResponse.getJSONObject(ERROR).getString(MESSAGE).equals("failed to auto-detect source language and find a matching language pair")) {
            //language pair not available
            //try with platform default locale if different from target locale
            Locale defaultLocale = localeConfigService.getDefaultLocaleConfig().getLocale();
            if (!targetLocale.equals(defaultLocale)) {
              return translate(message, defaultLocale);
            }
          }
        }



        LOG.error("remote_service={} operation={} parameters=\"message length:{},targetLocale:{}\" status=ko "
                      + "duration_ms={} error_msg=\"{}, status : {} \"",
                  LW_TRANSLATE_SERVICE,
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
