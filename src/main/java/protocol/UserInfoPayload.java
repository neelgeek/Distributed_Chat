package protocol;

import java.io.Serializable;

/**
 * Represents a payload with information on a user or chat client
 */
public class UserInfoPayload extends AbstractNetworkEntity implements Serializable {

  private String userName;


  public UserInfoPayload(String userID, String userName, String HOST, String PORT,
      boolean isActive) {
    super(userID, HOST, PORT, isActive);
    this.userName = userName;
  }

  public UserInfoPayload(String userID, String userName, boolean isActive) {
    super(userID, isActive);
    this.userName = userName;
  }

  public String getUserName() {
    return userName;
  }
}
