package broker;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Defines the interface for implementation of a Broker in the cluster.
 */
public interface Broker<T> extends Remote {

  /**
   * Sends a update about another broker that has become active or inactive. This method is used by
   * the admin server to sends broker updates to active brokers.
   *
   * @param brokerInfo Payload holding broker info
   */
  void sendBrokerUpdate(T brokerInfo) throws RemoteException;

}
