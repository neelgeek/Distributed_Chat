package admin;

import broker.Broker;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;
import protocol.BrokerInfoPayload;
import protocol.UserInfoPayload;

public class RMIAdminImpl implements Admin {

  Map<String, BrokerInfoPayload> brokerRecord = new HashMap<>();
  Map<String, Long> brokerTimeouts = new HashMap<>();

  public RMIAdminImpl() {
  }

  @Override
  public BrokerInfoPayload registerNewUser(UserInfoPayload user)
      throws RemoteException, InterruptedException {

    return null;
  }

  @Override
  public boolean registerNewBroker(BrokerInfoPayload broker)
      throws RemoteException, InterruptedException {
    if (!this.brokerRecord.containsKey(broker.getEntityID())) {
      this.announceNewBroker(broker);
      this.brokerRecord.put(broker.getEntityID(), broker);
      this.brokerTimeouts.put(broker.getEntityID(), System.currentTimeMillis());
      return true;
    }
    return false;
  }

  /**
   * Announces the info about the newly joined broker to currently active brokers;
   *
   * @param newBroker
   */
  private void announceNewBroker(BrokerInfoPayload newBroker) {
    Registry registry;
    for (BrokerInfoPayload broker : this.brokerRecord.values()) {
      try {
        registry = LocateRegistry.getRegistry(broker.getHOST(), broker.getPORT());
        Broker<BrokerInfoPayload> brokerStub = (Broker<BrokerInfoPayload>) registry.lookup(
            "Broker");
        brokerStub.sendBrokerUpdate(newBroker);
      } catch (RemoteException | NotBoundException e) {
        e.printStackTrace();
      }
    }
  }
}
