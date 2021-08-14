package admin;

import broker.Broker;
import common.ClientStatusChecker;
import common.HeartbeatReceiver;
import common.OutputHandler;
import common.StatusMaintainer;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import protocol.BrokerInfoPayload;
import protocol.UserInfoPayload;

public class RMIAdminImpl extends UnicastRemoteObject implements Admin, StatusMaintainer,
    HeartbeatReceiver<BrokerInfoPayload> {

  Map<String, BrokerInfoPayload> brokerRecord = new HashMap<>();
  Map<String, Long> brokerTimeouts = new HashMap<>();
  ScheduledExecutorService brokerStatusChecker = Executors.newSingleThreadScheduledExecutor();


  protected RMIAdminImpl() throws RemoteException {
    this.brokerStatusChecker.scheduleWithFixedDelay(
        new ClientStatusChecker(brokerTimeouts, this, new Long(2)), 2,
        2,
        TimeUnit.SECONDS);
  }


  // Why does this always return NULL?
  // Who is supposed to call this method, client or broker?
  // If the client calls it, then RemoteServer.getClientHost will return
  // the host name of the client, when we want the host name of the broker
  @Override
  public BrokerInfoPayload registerNewUser(UserInfoPayload user)
      throws RemoteException, InterruptedException {
    try {
      String remoteHost = RemoteServer.getClientHost();
      user.setHOST(remoteHost);
      OutputHandler.printWithTimestamp(
          String.format("Incoming User Connection from Host: %s", remoteHost));
    } catch (ServerNotActiveException e) {
      e.printStackTrace();
    }

    if (this.brokerRecord.values().size() > 0) {
      List<BrokerInfoPayload> activeBrokers = new ArrayList<BrokerInfoPayload>(this.brokerRecord.values());
      return activeBrokers.get(new Random().nextInt(activeBrokers.size()));
    }
    // Return null if there are no active brokers
    return null;
  }

  @Override
  public boolean registerNewBroker(BrokerInfoPayload broker)
      throws RemoteException, InterruptedException {

    try {
      String remoteHost = RemoteServer.getClientHost();
      broker.setHOST(remoteHost);
      OutputHandler.printWithTimestamp(
          String.format("Incoming Broker Connection from Host: %s", remoteHost));
    } catch (ServerNotActiveException e) {
      e.printStackTrace();
    }

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
      if (broker.isActive()) {
        try {
          registry = LocateRegistry.getRegistry(broker.getHOST(), broker.getPORT());
          Broker brokerStub = (Broker) registry.lookup("Broker");
          brokerStub.sendBrokerUpdate(newBroker);
        } catch (RemoteException | NotBoundException e) {
          this.setClientInactive(broker.getEntityID());
        }
      }
    }
  }

  @Override
  public void sendHeartBeat(BrokerInfoPayload clientInfo) throws RemoteException {
    try {
      String remoteHost = RemoteServer.getClientHost();
      clientInfo.setHOST(remoteHost);
      OutputHandler.printWithTimestamp(
          String.format("Heartbeat received from Broker at: %s", remoteHost));
    } catch (ServerNotActiveException e) {
      e.printStackTrace();
    }
    // Update the last heartbeat time to current timestamp
    if (this.brokerRecord.containsKey(clientInfo.getEntityID())) {
      this.brokerTimeouts.put(clientInfo.getEntityID(), System.currentTimeMillis());
    } else {
      OutputHandler.printWithTimestamp(
          String.format("Heartbeat Received from Unregistered Broker at: %s",
              clientInfo.getHOST()));
    }
  }

  @Override
  public void setClientInactive(String clientID) {
    BrokerInfoPayload brokerInfo = brokerRecord.get(clientID);
    if (brokerInfo.isActive()) {
      brokerInfo.setActive(false);
      OutputHandler.printWithTimestamp(
          String.format("Broker with ID: %s HOST: %s timed out. Setting status to inActive.",
              clientID, brokerInfo.getHOST()));
    }
  }
}
