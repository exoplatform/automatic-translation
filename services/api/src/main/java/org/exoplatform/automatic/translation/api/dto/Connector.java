package org.exoplatform.automatic.translation.api.dto;

import java.io.Serializable;

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
