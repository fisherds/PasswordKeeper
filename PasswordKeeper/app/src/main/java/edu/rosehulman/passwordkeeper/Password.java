package edu.rosehulman.passwordkeeper;


import com.google.firebase.database.Exclude;

public class Password {

  @Exclude
  public String key;

  public String service;
  public String username;
  public String password;

  public Password() {
    // Required!  Do not remove!
  }

  public Password(String username, String password, String service) {
    this(null, username, password, service);
  }

  public Password(String key, String username, String password, String service) {
    this.key = key;
    this.service = service;
    this.username = username;
    this.password = password;
  }
}
