package protocol;

import java.io.Serializable;

/**
 * Represents a payload with information on a user or chat client
 */
public class UserInfoPayload implements Serializable {

  private String userID;
  private String userName;
  private String HOST;
  private String PORT;
  private boolean isActive;

  public UserInfoPayload(String userID, String userName, String HOST, String PORT,
      boolean isActive) {
    this.userID = userID;
    this.userName = userName;
    this.HOST = HOST;
    this.PORT = PORT;
    this.isActive = isActive;
  }

  public UserInfoPayload(String userID, String userName, boolean isActive) {
    this.userID = userID;
    this.userName = userName;
    this.isActive = isActive;
  }

  public String getUserID() {
    return userID;
  }

  public String getUserName() {
    return userName;
  }

  public String getHOST() {
    return HOST;
  }

  public String getPORT() {
    return PORT;
  }

  public boolean isActive() {
    return isActive;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public void setActive(boolean active) {
    isActive = active;
  }
}
