package com.echolocation19.test2.Model;

public class Message {
  private String senderId;
  private String username;
  private String message;
  private String recipientId;
  private String imageUrl;
  private boolean isMine;

  public Message() {
  }

  public Message(String senderId, String username, String message, String recipientId, String imageUrl, boolean isMine) {
    this.senderId = senderId;
    this.username = username;
    this.message = message;
    this.recipientId = recipientId;
    this.imageUrl = imageUrl;
    this.isMine = isMine;
  }

  public boolean isMine() {
    return isMine;
  }

  public void setMine(boolean mine) {
    isMine = mine;
  }

  public String getSenderId() {
    return senderId;
  }

  public void setSenderId(String senderId) {
    this.senderId = senderId;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getRecipientId() {
    return recipientId;
  }

  public void setRecipientId(String recipientId) {
    this.recipientId = recipientId;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }
}
