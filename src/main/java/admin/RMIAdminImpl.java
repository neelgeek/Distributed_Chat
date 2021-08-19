package admin;

import broker.Broker;
import common.ClientStatusChecker;
import common.HeartbeatReceiver;
import common.OutputHandler;
import common.PaxosServer;
import common.RMIHandler;
import common.StatusMaintainer;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import protocol.AbstractNetworkEntity;
import protocol.BrokerInfoPayload;
import protocol.PaxActions;
import protocol.PaxMessage;
import protocol.Replicable;
import protocol.UserInfoPayload;

public class RMIAdminImpl extends UnicastRemoteObject implements Admin, StatusMaintainer,
    HeartbeatReceiver<BrokerInfoPayload>, PaxosProposer {

  Map<String, BrokerInfoPayload> brokerRecord = new HashMap<>();
  Map<String, Long> brokerTimeouts = new HashMap<>();
  ScheduledExecutorService brokerStatusChecker = Executors.newSingleThreadScheduledExecutor();
  ThreadPoolExecutor paxosProcessor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);

  private Integer current_proposalID = 0;

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
      List<BrokerInfoPayload> activeBrokers = new ArrayList<BrokerInfoPayload>(
          this.brokerRecord.values());
      BrokerInfoPayload randomBroker = null;
      while (randomBroker == null || !randomBroker.isActive()) {
        randomBroker = activeBrokers.get(new Random().nextInt(activeBrokers.size()));
      }
      return randomBroker;
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
      this.brokerRecord.put(broker.getEntityID(), broker);
      this.brokerTimeouts.put(broker.getEntityID(), System.currentTimeMillis());
      this.announceBrokerUpdate(broker);
      return true;
    }
    return false;
  }

  @Override
  public List<BrokerInfoPayload> getActiveBrokers() throws RemoteException {
    return brokerRecord.values().stream().filter(peer -> peer.isActive())
        .collect(Collectors.toCollection(ArrayList::new));
  }

  /**
   * Announces the info about the newly joined broker to currently active brokers;
   *
   * @param newBroker
   */
  private void announceBrokerUpdate(BrokerInfoPayload newBroker) {
    Registry registry;
    for (BrokerInfoPayload broker : this.brokerRecord.values()) {
      if (broker.isActive() && !broker.getEntityID().equals(newBroker.getEntityID())) {
        try {
          Broker brokerStub = RMIHandler.fetchRemoteObject("Broker", broker.getHOST(),
              broker.getPORT());
          brokerStub.sendBrokerUpdate(newBroker);
        } catch (RemoteException e) {
          e.printStackTrace();
//          this.setClientInactive(broker.getEntityID());
        }
      }
    }
  }

  @Override
  public void sendHeartBeat(BrokerInfoPayload clientInfo) throws RemoteException {
    try {
      String remoteHost = RemoteServer.getClientHost();
      clientInfo.setHOST(remoteHost);
//      OutputHandler.printWithTimestamp(
//          String.format("Heartbeat received from Broker at: %s", remoteHost));
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
      announceBrokerUpdate(brokerInfo);
    }
  }

  @Override
  public void submitRequest(Replicable replicablePayload) throws RemoteException {
    current_proposalID += 1;
    paxosProcessor.submit(new PaxosReplication(current_proposalID,
        brokerRecord.values().toArray(new BrokerInfoPayload[0]), replicablePayload, "Broker"));

  }
}

class PaxosReplication implements Runnable {

  private Integer current_proposalID;
  private AbstractNetworkEntity[] PAX_SERVERS;
  private Replicable toReplicate;
  private String paxServerRMIName;

  public PaxosReplication(Integer current_proposalID, AbstractNetworkEntity[] PAX_SERVERS,
      Replicable toReplicate, String paxServerRMIName) {
    this.current_proposalID = current_proposalID;
    this.PAX_SERVERS = PAX_SERVERS;
    this.toReplicate = toReplicate;
    this.paxServerRMIName = paxServerRMIName;
  }

  @Override
  public void run() {
    OutputHandler.printWithTimestamp(
        String.format("Replication Request submitted for %s with id: %d", toReplicate.toString(),
            current_proposalID));

    ArrayList<PaxosServer> ACCEPTORS = new ArrayList<>();
    for (AbstractNetworkEntity paxServer : PAX_SERVERS) {
      PaxosServer server = RMIHandler.fetchRemoteObject(paxServerRMIName, paxServer.getHOST(),
          paxServer.getPORT());
      if (server != null) {
        ACCEPTORS.add(server);
      }
    }

    ArrayList<PaxMessage> promises = new ArrayList<>();
    for (PaxosServer acceptor : ACCEPTORS) {
      try {
        PaxMessage promise = acceptor.prepare(this.current_proposalID);
        promises.add(promise);
      } catch (RemoteException | NullPointerException e) {
        OutputHandler.printWithTimestamp(
            String.format("No response for PREPARE for id: %d", current_proposalID));
      }
    }

    ArrayList<PaxMessage> positivePromises = promises.stream()
        .filter(paxMessage -> paxMessage.getType().equals(
            PaxActions.PROMISE)).collect(Collectors.toCollection(ArrayList::new));

    if (positivePromises.size() >= (ACCEPTORS.size() / 2) + 1) {
      OutputHandler.printWithTimestamp(
          String.format("Majority achieved for PROMISE for id: %d ", current_proposalID));

      PaxMessage highestPromise = positivePromises.stream()
          .max(Comparator.comparing(PaxMessage::getAcceptedID))
          .orElseThrow(NoSuchElementException::new);

      PaxMessage acceptMessage;
      if (highestPromise.getAcceptedID() == -1) {
        acceptMessage = new PaxMessage(current_proposalID, toReplicate, PaxActions.ACCEPT);
      } else {
        acceptMessage = new PaxMessage(current_proposalID, highestPromise.getAcceptedValue(),
            PaxActions.ACCEPT);
      }

      ArrayList<PaxMessage> acceptedMessages = new ArrayList<>();
      for (PaxosServer acceptor : ACCEPTORS) {
        try {
          PaxMessage accepted = acceptor.accept(acceptMessage);
          acceptedMessages.add(accepted);
        } catch (RemoteException e) {
          OutputHandler.printWithTimestamp(
              String.format("No response for ACCEPT for id: %d", current_proposalID));
        }
      }

      ArrayList<PaxMessage> positiveAccepts = acceptedMessages.stream()
          .filter(paxMessage -> paxMessage.getType().equals(PaxActions.ACCEPTED))
          .collect(Collectors.toCollection(ArrayList::new));

      if (positiveAccepts.size() >= ACCEPTORS.size() / 2 + 1) {
//        finalResponse.setSuccess(true);
        OutputHandler.printWithTimestamp(
            String.format("Replication successful for ID: %d", current_proposalID));
      } else {
//        finalResponse.setSuccess(false);
//        finalResponse.setMessage("Failed to Replicate the value, due to failure in Acceptors");
        OutputHandler.printWithTimestamp(
            String.format(
                "Failed to Replicate the value for proposalID: %d, due to failure in Acceptors",
                current_proposalID));

      }
    } else {
//      finalResponse.setSuccess(false);
//      finalResponse.setMessage("Failed to Replicate the value, due to failure in Acceptors");
      OutputHandler.printWithTimestamp(
          String.format(
              "Failed to Replicate the value for proposalID: %d, due to failure in Acceptors",
              current_proposalID));
    }

  }
}
