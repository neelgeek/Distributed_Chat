package protocol;

/**
 * Represents a payload with information on a user or chat client
 */
public class UserInfoPayload extends AbstractNetworkEntity implements Replicable {

  private String userName;
  private String SOCKET_PORT;

  public UserInfoPayload() {
  }

//  public UserInfoPayload(String userID, String userName, String HOST, int PORT,
//      boolean isActive) {
//    super(userID, HOST, PORT, isActive);
//    this.userName = userName;
//  }

  public UserInfoPayload(String entityID, String HOST, int PORT, boolean isActive,
      String userName, String SOCKET_PORT) {
    super(entityID, HOST, PORT, isActive);
    this.userName = userName;
    this.SOCKET_PORT = SOCKET_PORT;
  }

  public UserInfoPayload(String userID, String userName, boolean isActive) {
    super(userID, isActive);
    this.userName = userName;
  }

  public String getUserName() {
    return userName;
  }

  public String getSOCKET_PORT() {
    return SOCKET_PORT;
  }
}
