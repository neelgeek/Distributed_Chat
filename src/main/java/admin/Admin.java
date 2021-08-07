package admin;

import java.rmi.RemoteException;
import protocol.BrokerInfoPayload;
import protocol.UserInfoPayload;

public interface Admin extends java.rmi.Remote {

  /**
   * Method to register first time users to the service
   *
   * @param user containing the info about the new user
   * @return BrokerInfoPayload informing the user about one of the active brokers
   * @throws InterruptedException
   */
  BrokerInfoPayload registerNewUser(UserInfoPayload user)
      throws java.rmi.RemoteException, InterruptedException;

  /**
   * Method to register first time brokers to the service
   *
   * @param broker containing the info about the new broker
   * @return boolean check informing the broker if it has been successfully registered
   */
  boolean registerNewBroker(BrokerInfoPayload broker)
      throws java.rmi.RemoteException, InterruptedException;

  /**
   * Informs the admin that the given broker is still active. Method call works as a heartbeat and
   * should be called by the broker every second.
   *
   * @param brokerInfo Payload containing the info about the broker client
   * @throws RemoteException
   */
  void sendBrokerHeartbeat(BrokerInfoPayload brokerInfo) throws RemoteException;

}
