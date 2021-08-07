package broker;

import admin.Admin;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import protocol.BrokerInfoPayload;
import protocol.UserInfoPayload;

public class BrokerImpl implements Broker {

  private HashMap<String, BrokerInfoPayload> peerBrokers = new HashMap<>();
  private Admin adminServer;
  private Registry registry;

  public BrokerImpl(String ADMIN_HOST, Integer ADMIN_PORT) {
    try {
      registry = LocateRegistry.getRegistry(ADMIN_HOST, ADMIN_PORT);
    } catch (RemoteException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void sendBrokerUpdate(BrokerInfoPayload brokerInfo) throws RemoteException {
    if (!brokerInfo.isActive() && peerBrokers.containsKey(brokerInfo.getEntityID())) {
      this.peerBrokers.remove(brokerInfo.getEntityID());
    } else if (!peerBrokers.containsKey(brokerInfo.getEntityID())) {
      this.peerBrokers.put(brokerInfo.getEntityID(), brokerInfo);
    }
  }

  @Override
  public void sendClientHeartbeat(UserInfoPayload clientInfo) throws RemoteException {

  }
}
