package broker;

import admin.Admin;
import common.HeartbeatReceiver;
import common.OutputHandler;
import common.PingHeartbeat;
import common.StatusMaintainer;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import protocol.BrokerInfoPayload;
import protocol.UserInfoPayload;

public class BrokerImpl extends UnicastRemoteObject implements Broker,
    HeartbeatReceiver<UserInfoPayload>, StatusMaintainer {

  private HashMap<String, BrokerInfoPayload> peerBrokers = new HashMap<>();
  private Admin adminServer;
  private Registry registry;
  private BrokerInfoPayload selfRecord;
  private String ADMIN_HOST;
  private int ADMIN_PORT, SELF_PORT;
  private ScheduledExecutorService brokerHeartbeatService = Executors.newSingleThreadScheduledExecutor();

  public BrokerImpl(String ADMIN_HOST, Integer ADMIN_PORT, Integer SELF_PORT)
      throws RemoteException {
    super();
    this.ADMIN_HOST = ADMIN_HOST;
    this.ADMIN_PORT = ADMIN_PORT;
    this.SELF_PORT = SELF_PORT;

    this.selfRecord = new BrokerInfoPayload(UUID.randomUUID().toString(), SELF_PORT, true);
    try {
      registry = LocateRegistry.getRegistry(ADMIN_HOST, ADMIN_PORT);
      this.adminServer = (Admin) registry.lookup("Admin");
      boolean connectAdmin = adminServer.registerNewBroker(selfRecord);
      if (connectAdmin) {
        OutputHandler.printWithTimestamp(String.format("Connected to Admin server!"));
      }
    } catch (NotBoundException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    this.brokerHeartbeatService.scheduleAtFixedRate(
        new PingHeartbeat(ADMIN_HOST, ADMIN_PORT, "Admin", selfRecord), 1, 1,
        TimeUnit.SECONDS);
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
  public void sendHeartBeat(UserInfoPayload clientInfo) throws RemoteException {
    try {
      String remoteHost = RemoteServer.getClientHost();
      clientInfo.setHOST(remoteHost);
      OutputHandler.printWithTimestamp(
          String.format("Heartbeat Received from Client at: %s", remoteHost));
    } catch (ServerNotActiveException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void setClientInactive(String clientID) {

  }
}

