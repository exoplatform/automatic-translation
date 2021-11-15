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
package org.exoplatform.automatic.translation.rest;

import java.io.Serializable;
import java.util.List;

public class AutomaticTranslationConfiguration implements Serializable {

  private String activeConnector;

  private List<Connector> connectors;

  private String          activeApiKey;

  public String getActiveConnector() {
    return activeConnector;
  }

  public void setActiveConnector(String activeConnector) {
    this.activeConnector = activeConnector;
  }

  public List<Connector> getConnectors() {
    return connectors;
  }

  public void setConnectors(List<Connector> connectors) {
    this.connectors = connectors;
  }

  public String getActiveApiKey() {
    return activeApiKey;
  }

  public void setActiveApiKey(String activeApiKey) {
    this.activeApiKey = activeApiKey;
  }

  public class Connector implements Serializable {
    private String name;

    private String description;

    private String apiKey;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getDescription() {
      return description;
    }

    public void setDescription(String description) {
      this.description = description;
    }

    public String getApiKey() {
      return apiKey;
    }

    public void setApiKey(String apiKey) {
      this.apiKey = apiKey;
    }

    public Connector(String name, String description, String apiKey) {
      this.name = name;
      this.description = description;
      this.apiKey = apiKey;
    }
  }

  public AutomaticTranslationConfiguration(List<Connector> connectors, String activeConnector) {
    this.connectors = connectors;
    this.activeConnector = activeConnector;
  }

  public void addConnector(String name, String description, String apiKey) {
    connectors.add(new Connector(name, description, apiKey));
  }
}
