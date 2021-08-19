package admin;

import java.rmi.Remote;
import java.rmi.RemoteException;
import protocol.Replicable;

/**
 * A Paxos Proposer participates in a Paxos run and acts as the proposer. The {@link PaxosProposer}
 * is the only proposer in the system and all the servers forward the client requests to the
 * proposer so that it can start a paxos run for the replication.
 */
public interface PaxosProposer extends Remote {

  /**
   * Submit a request to the proposer so that it can start a paxos run for replication.
   *
   * @param replicablePayload
   * @throws RemoteException
   */
  void submitRequest(Replicable replicablePayload)
      throws RemoteException;

//  /**
//   * Tells the proposer about Acceptors to connect to
//   *
//   * @param ACCEPTOR_PORTS List of ports where the acceptors are running
//   */
//  void connectAcceptors(Integer[] ACCEPTOR_PORTS) throws RemoteException;

}
