package admin;

import java.rmi.RemoteException;
import java.util.List;
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
   * Returns a list of active brokers registered with the server
   *
   * @return
   */
  List<BrokerInfoPayload> getActiveBrokers() throws RemoteException;

}
