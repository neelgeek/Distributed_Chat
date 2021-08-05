package admin;

import java.rmi.RemoteException;
import protocol.BrokerInfoPayload;
import protocol.UserInfoPayload;

public class RMIAdminImpl implements Admin {

  @Override
  public BrokerInfoPayload registerNewUser(UserInfoPayload user)
      throws RemoteException, InterruptedException {
    return null;
  }

  @Override
  public boolean registerNewBroker(BrokerInfoPayload broker)
      throws RemoteException, InterruptedException {
    return false;
  }
}
