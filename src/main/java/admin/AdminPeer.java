package admin;

import java.rmi.Remote;
import java.rmi.RemoteException;
import protocol.AdminInfoPayload;

public interface AdminPeer extends Remote {

  /**
   * Used by the other peers to check if the leader is still active
   *
   * @return True if the peer is active, else false
   */
  boolean isActive() throws RemoteException;

  /**
   * @param initiator
   */
  void startElection(ElectionProcessor initiator) throws RemoteException;

  /**
   * @param leader
   */
  void announceCoordinator(AdminInfoPayload leader) throws RemoteException;
}
