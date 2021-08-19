package common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import protocol.PaxMessage;


/**
 * A Paxos server participates in a Paxos run and acts as Acceptor and Learner.
 */
public interface PaxosServer extends Remote {

  /**
   * Tells the Acceptor to prepare for a run.
   *
   * @param proposalID Proposal ID to be prepared.
   * @return Info about Promise
   */
  PaxMessage prepare(Integer proposalID) throws RemoteException;

  /**
   * Tells the Acceptor to accept a value for a given proposalID.
   *
   * @param acceptMessage Info about the value to be accepted for the proposalID.
   * @return Status of the accept request, it can be rejected if a proposal with higher id is sent.
   */
  PaxMessage accept(PaxMessage acceptMessage) throws RemoteException;

  /**
   * Announces the Learner that a consensus has been reached on a value and it should write it.
   *
   * @param acceptedValue Accepted value
   */
  void announce(PaxMessage acceptedValue) throws RemoteException;

//  /**
//   * Tells the acceptor to connect to the proposer on given port
//   *
//   * @param PROPOSER_PORT Port where the proposer RMI registry is located
//   * @throws RemoteException
//   */
//  void connectProposer(Integer PROPOSER_PORT) throws RemoteException;
//
//  /**
//   * Tells the acceptor to connect to the learners on given port
//   *
//   * @param LEARNER_PORTS Port where the learner RMI registries is located
//   * @throws RemoteException
//   */
//  void connectLearners(Integer[] LEARNER_PORTS) throws RemoteException;
}
