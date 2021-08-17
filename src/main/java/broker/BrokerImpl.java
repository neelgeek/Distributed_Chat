package broker;

import admin.Admin;
import common.ClientStatusChecker;
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
import protocol.GroupChat;
import protocol.GroupChatInfoPayload;
import protocol.UserInfoPayload;

public class BrokerImpl extends UnicastRemoteObject implements Broker,
    HeartbeatReceiver<UserInfoPayload>, StatusMaintainer {

  private HashMap<String, BrokerInfoPayload> peerBrokers = new HashMap<>();
  private HashMap<String, UserInfoPayload> userRecord = new HashMap<>();
  private HashMap<String, Long> userTimeouts = new HashMap<>();

  private Admin adminServer;
  private Registry registry;
  private BrokerInfoPayload selfRecord;
  private String ADMIN_HOST;
  private int ADMIN_PORT, SELF_PORT;
  private ScheduledExecutorService brokerHeartbeatService = Executors.newSingleThreadScheduledExecutor();
  private ScheduledExecutorService clientStatusService = Executors.newSingleThreadScheduledExecutor();

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

    this.clientStatusService.scheduleAtFixedRate(
        new ClientStatusChecker(this.userTimeouts, this, new Long(2)), 2, 2, TimeUnit.SECONDS);
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
  public boolean registerUser(UserInfoPayload userInfoPayload) throws RemoteException {
    try {
      String remoteHost = RemoteServer.getClientHost();
      userInfoPayload.setHOST(remoteHost);
      OutputHandler.printWithTimestamp(
          String.format("Incoming User Connection from Host: %s", remoteHost));
    } catch (ServerNotActiveException e) {
      e.printStackTrace();
    }

    if (!this.userRecord.containsKey(userInfoPayload.getEntityID())) {
//      this.announceNewBroker(broker);
      this.userRecord.put(userInfoPayload.getEntityID(), userInfoPayload);
      this.userTimeouts.put(userInfoPayload.getEntityID(), System.currentTimeMillis());
      return true;
    }
    return false;
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
    if (this.userRecord.containsKey(clientInfo.getEntityID())) {
      this.userTimeouts.put(clientInfo.getEntityID(), System.currentTimeMillis());
    } else {
      OutputHandler.printWithTimestamp(
          String.format("Heartbeat Received from Unregistered User at: %s", clientInfo.getHOST()));
    }


  }

  @Override
  public void setClientInactive(String clientID) {
    UserInfoPayload userInfo = this.userRecord.get(clientID);
    if (userInfo.isActive()) {
      userInfo.setActive(false);
      OutputHandler.printWithTimestamp(
          String.format("User with ID: %s HOST: %s timed out. Setting status to inActive.",
              clientID, userInfo.getHOST()));
    }
  }
}

