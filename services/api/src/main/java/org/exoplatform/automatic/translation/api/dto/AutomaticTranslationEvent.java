package org.exoplatform.automatic.translation.api.dto;

public class AutomaticTranslationEvent {
  long spaceId;
  int    messageLength;
  String contentType;
  String targetLanguage;
  String connectorName;

  public long getSpaceId() {
    return spaceId;
  }

  public void setSpaceId(long spaceId) {
    this.spaceId = spaceId;
  }

  public int getMessageLength() {
    return messageLength;
  }

  public void setMessageLength(int messageLength) {
    this.messageLength = messageLength;
  }

  public String getContentType() {
    return contentType;
  }

  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  public String getTargetLanguage() {
    return targetLanguage;
  }

  public void setTargetLanguage(String targetLanguage) {
    this.targetLanguage = targetLanguage;
  }

  public String getConnectorName() {
    return connectorName;
  }

  public void setConnectorName(String connectorName) {
    this.connectorName = connectorName;
  }

}
