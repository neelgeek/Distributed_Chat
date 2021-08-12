package common;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Defines an interface for the classes that act as a server that receives heartbeats from its
 * client instances
 *
 * @param <I> Type in which the client info is conveyed.
 */
public interface HeartbeatReceiver<I> extends Remote {

  /**
   * Should be called by the client instance as a heartbeat call.
   *
   * @param clientInfo payload containing the information about the client
   * @throws RemoteException
   */
  void sendHeartBeat(I clientInfo) throws RemoteException;

}
