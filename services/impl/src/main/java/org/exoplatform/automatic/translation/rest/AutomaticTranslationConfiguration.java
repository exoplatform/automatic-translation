package org.exoplatform.automatic.translation.rest;

import java.io.Serializable;
import java.util.List;

public class AutomaticTranslationConfiguration implements Serializable {

  public String activeConnector;

  public List<Connector> connectors;

  public String          activeApiKey;

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
    public String name;

    public String description;

    public String apiKey;

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
