package broker;

import java.rmi.Remote;
import java.rmi.RemoteException;
import protocol.BrokerInfoPayload;

/**
 * Defines the interface for implementation of a Broker in the cluster.
 */
public interface Broker extends Remote {

  /**
   * Sends an update about another broker that has become active or inactive. This method is used by
   * the admin server to sends broker updates to active brokers.
   *
   * @param brokerInfo Payload holding broker info
   */
  void sendBrokerUpdate(BrokerInfoPayload brokerInfo) throws RemoteException;
}
