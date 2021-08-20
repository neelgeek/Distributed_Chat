package protocol;

import java.net.Socket;

/**
 * Represents a payload with information on a user or chat client
 */
public class UserInfoPayload extends AbstractNetworkEntity implements Replicable {

  private String userName;


  // port number of the server socket (i.e. the PeerHarbor) of the user
  private int SOCKET_PORT;

  // the client socket of this user as seen by another particular user. The same user
  // can have different values for this according to which user you view it from
  // private Socket userSocket;

  public UserInfoPayload() {
  }

//  public UserInfoPayload(String userID, String userName, String HOST, int PORT,
//      boolean isActive) {
//    super(userID, HOST, PORT, isActive);
//    this.userName = userName;
//  }

  public UserInfoPayload(String entityID, String HOST, int PORT, boolean isActive,
      String userName, int SOCKET_PORT) {
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

  public int getSOCKET_PORT() {
    return SOCKET_PORT;
  }

  public void setSOCKET_PORT(int SOCKET_PORT) {
    this.SOCKET_PORT = SOCKET_PORT;
  }

//  public Socket getUserSocket() {
//    return userSocket;
//  }
//
//  public void setUserSocket(Socket userSocket) {
//    this.userSocket = userSocket;
//  }
}
