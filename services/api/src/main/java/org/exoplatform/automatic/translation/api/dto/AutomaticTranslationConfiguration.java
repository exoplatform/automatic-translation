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
package org.exoplatform.automatic.translation.api.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class AutomaticTranslationConfiguration implements Serializable {

  private String          activeConnector;

  private List<Connector> connectors;

  private String          activeApiKey;

  private AutomaticTranslationFeaturesOptions featuresOptions;

  public AutomaticTranslationConfiguration(List<Connector> connectors, String activeConnector) {
    this.connectors = connectors;
    this.activeConnector = activeConnector;
  }

  public void addConnector(String name, String description, String apiKey) {
    connectors.add(new Connector(name, description, apiKey));
  }
}
