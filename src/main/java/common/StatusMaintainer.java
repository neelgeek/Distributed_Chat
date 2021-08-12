package common;

/**
 * An interface for server class that periodically checks the status of its client instances and
 * needs to set them to inactive if they don't respond in a set period of time.
 */
public interface StatusMaintainer {

  /**
   * The server will set the status of the client to inActive for the given clientID.
   *
   * @param clientID
   */
  void setClientInactive(String clientID);

}
