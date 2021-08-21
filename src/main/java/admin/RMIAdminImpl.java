package admin;

import broker.Broker;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import common.AnnouncementProcessor;
import common.ClientStatusChecker;
import common.HeartbeatReceiver;
import common.OutputHandler;
import common.PaxosReplication;
import common.PaxosServer;
import common.RMIHandler;
import common.StatusMaintainer;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import protocol.AdminInfoPayload;
import protocol.BrokerInfoPayload;
import protocol.PaxActions;
import protocol.PaxMessage;
import protocol.Replicable;
import protocol.UserInfoPayload;

public class RMIAdminImpl extends UnicastRemoteObject implements Admin, AdminPeer, StatusMaintainer,
    HeartbeatReceiver<BrokerInfoPayload>, PaxosProposer, PaxosServer {

  private AdminInfoPayload selfInfo;
  private Map<String, BrokerInfoPayload> brokerRecord = new HashMap<>();
  private Map<String, Long> brokerTimeouts = new HashMap<>();
  private ScheduledExecutorService brokerStatusChecker = Executors.newSingleThreadScheduledExecutor();

  private ThreadPoolExecutor paxosProcessor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
  private Integer current_proposalID = 0;

  protected AdminInfoPayload[] peerAdmins = new AdminInfoPayload[0];
  protected Integer selfPriority = 0;
  protected AdminInfoPayload currentLeader;
  private ScheduledExecutorService leaderStatusChecker;
  private ThreadPoolExecutor electionThread = (ThreadPoolExecutor) Executors.newFixedThreadPool(
      1);
  protected boolean IS_ELECTION_STARTED = false;
  protected boolean RECEIVED_COORDINATOR_ANNOUNCEMENT = false;

  private PaxosProposer adminProposer;
  private Integer maxID;
  private Integer last_accepted_proposalID;
  private Replicable last_accepted_value;
  private ConcurrentHashMap<Integer, Replicable> announcementMap;
  private ScheduledExecutorService announcementProcessorService = Executors.newScheduledThreadPool(
      1);

  protected RMIAdminImpl(String HOST, int PORT, Integer selfPriority) throws RemoteException {
    this.selfPriority = selfPriority;
    selfInfo = new AdminInfoPayload(UUID.randomUUID().toString(), HOST, PORT, true, selfPriority);
    this.announcementMap = new ConcurrentHashMap<>();
    try {
      Gson gson = new Gson();
      JsonArray object = (JsonArray) JsonParser.parseReader(new InputStreamReader(
          StartAdmin.class.getResourceAsStream("/admin-config.json")));
      peerAdmins = gson.fromJson(object, AdminInfoPayload[].class);
    } catch (NullPointerException e) {
      e.printStackTrace();
    }

    currentLeader = Arrays.stream(peerAdmins).max(
        Comparator.comparingInt(AdminInfoPayload::getPRIORITY)).get();

    if (currentLeader.getPORT() == selfInfo.getPORT()
        && currentLeader.getPRIORITY().compareTo(selfPriority) == 0) {
      currentLeader = selfInfo;
      OutputHandler.printWithTimestamp("Current Peer leader set to self");
    } else {
      setAdminProposer();
    }

    startLeaderStatusChecker();
    startClientStatusChecker();
    announcementProcessorService.scheduleAtFixedRate(
        new AnnouncementProcessor(announcementMap, this), 1, 1,
        TimeUnit.SECONDS);
  }


  public AdminInfoPayload getSelfInfo() {
    return selfInfo;
  }

  private void startLeaderStatusChecker() {
    if (currentLeader != selfInfo) {
      if (this.leaderStatusChecker != null) {
        this.leaderStatusChecker.shutdownNow();
      }
      OutputHandler.printWithTimestamp(
          String.format("Starting Leader Status Checker for leader at HOST: %s PORT: %s",
              currentLeader.getHOST(), currentLeader.getPORT()));
      this.leaderStatusChecker = Executors.newSingleThreadScheduledExecutor();
      this.leaderStatusChecker.scheduleWithFixedDelay(
          new CheckLeaderStatus(currentLeader.getHOST(), currentLeader.getPORT(), this), 2,
          2,
          TimeUnit.SECONDS);
    }
  }

  private void startClientStatusChecker() {
    if (currentLeader == selfInfo) {
      OutputHandler.printWithTimestamp("Starting Client Status Checker!");
      this.brokerStatusChecker.scheduleWithFixedDelay(
          new ClientStatusChecker(brokerTimeouts, this, new Long(2)), 5,
          2,
          TimeUnit.SECONDS);
    }
  }

  private void setAdminProposer() {
    adminProposer = RMIHandler.fetchRemoteObject("Admin", currentLeader.getHOST(),
        currentLeader.getPORT());
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

      this.brokerRecord.put(broker.getEntityID(), broker);
      this.brokerTimeouts.put(broker.getEntityID(), System.currentTimeMillis());
      this.submitRequest(broker);
    } catch (ServerNotActiveException e) {
      e.printStackTrace();
    }

    if (!this.brokerRecord.containsKey(broker.getEntityID())) {
      this.brokerRecord.put(broker.getEntityID(), broker);
      this.brokerTimeouts.put(broker.getEntityID(), System.currentTimeMillis());
      this.adminProposer.submitRequest(broker);
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
//      try {
//        this.submitRequest(brokerInfo);
//      } catch (RemoteException e) {
//        OutputHandler.printWithTimestamp("ERROR: " + e.getMessage());
//      }
    }
  }

  @Override
  public void submitRequest(Replicable replicablePayload) throws RemoteException {
    current_proposalID += 1;
    if (replicablePayload instanceof UserInfoPayload) {
      paxosProcessor.submit(new PaxosReplication(current_proposalID,
          brokerRecord.values().toArray(new BrokerInfoPayload[0]), replicablePayload, "Broker"));
    } else if (replicablePayload instanceof BrokerInfoPayload) {
      paxosProcessor.submit(new PaxosReplication(current_proposalID,
          peerAdmins, replicablePayload, "Admin"));
    }


  }

  protected void startElection() {
    if (RECEIVED_COORDINATOR_ANNOUNCEMENT) {
      return;
    }
    this.leaderStatusChecker.shutdownNow(); //Stop check leader thread;
    this.IS_ELECTION_STARTED = true;
    //Start election thread;
    try {
      electionThread.submit(new ElectionProcessor(this));
    } catch (RemoteException e) {
      e.printStackTrace();
    }
  }

  @Override
  public boolean isActive() {
    return true;
  }

  @Override
  public void startElection(ElectionProcessor initiator) throws RemoteException {
    OutputHandler.printWithTimestamp("Incoming Election request from lower Priority peer");
    if (!IS_ELECTION_STARTED) {
      // Start election
      startElection();
    }
    initiator.sendAnswer();
  }

  @Override
  public void announceCoordinator(AdminInfoPayload leader) throws RemoteException {
    OutputHandler.printWithTimestamp(
        String.format("Leader announcement received, setting new leader to: %s", leader));
    RECEIVED_COORDINATOR_ANNOUNCEMENT = true;
    currentLeader = leader;
    endElection();
    startClientStatusChecker();
  }

  protected void announceCoordinatorToBrokers(AdminInfoPayload leader) {
    for (BrokerInfoPayload broker : brokerRecord.values()) {
      Broker brokerStub = RMIHandler.fetchRemoteObject("Broker", broker.getHOST(),
          broker.getPORT());
      if (broker != null) {
        try {
          brokerStub.announceNewLeader(leader);
        } catch (RemoteException e) {
          e.printStackTrace();
        }
      }
    }
  }

  protected void endElection() {
    OutputHandler.printWithTimestamp("Election Ended");
    startLeaderStatusChecker();
    IS_ELECTION_STARTED = false;
    RECEIVED_COORDINATOR_ANNOUNCEMENT = false;
  }

  @Override
  public PaxMessage prepare(Integer proposalID) throws RemoteException {
    if (last_accepted_proposalID == null || proposalID.compareTo(maxID) > 0) {
      maxID = proposalID;
      if (last_accepted_proposalID != null) {
        return new PaxMessage(proposalID, PaxActions.PROMISE, last_accepted_proposalID,
            this.last_accepted_value);
      } else {
        return new PaxMessage(proposalID, PaxActions.PROMISE, -1,
            null);
      }
    }
    return new PaxMessage(proposalID, PaxActions.NACK, maxID,
        last_accepted_value);
  }

  @Override
  public PaxMessage accept(PaxMessage acceptMessage) throws RemoteException {
    if (acceptMessage.getProposalID().compareTo(maxID) == 0) {
      last_accepted_proposalID = acceptMessage.getProposalID();
      last_accepted_value = acceptMessage.getProposedValue();
      PaxMessage announceMessage = new PaxMessage(acceptMessage.getProposalID(),
          acceptMessage.getProposedValue(), PaxActions.ACCEPTED);
      this.announceLearners(announceMessage);
      return announceMessage;
    } else {
      return new PaxMessage(acceptMessage.getProposalID(), PaxActions.NACK, -1, null);
    }
  }

  @Override
  public void announce(PaxMessage acceptedValue) throws RemoteException {
    this.announcementMap.put(acceptedValue.getProposalID(), acceptedValue.getProposedValue());
  }

  @Override
  public void put(String entityID, Replicable toReplicate) throws RemoteException {
    this.brokerRecord.put(entityID, (BrokerInfoPayload) toReplicate);
    this.brokerTimeouts.put(entityID, System.currentTimeMillis());
  }

  @Override
  public void remove(String entityID) throws RemoteException {
    this.brokerRecord.remove(entityID);
    this.brokerTimeouts.remove(entityID);
  }

  private void endPaxosRun() {
    this.last_accepted_proposalID = null;
    this.last_accepted_value = null;
    this.maxID = 0;
  }

  private void announceLearners(PaxMessage announceMessage) {
    for (AdminInfoPayload peer : peerAdmins) {
      PaxosServer learner = RMIHandler.fetchRemoteObject("Admin", peer.getHOST(), peer.getPORT());
      if (learner != null) {
        try {
          learner.announce(announceMessage);
        } catch (RemoteException e) {
          e.printStackTrace();
        }
      }
    }
    this.endPaxosRun();
  }
}


class CheckLeaderStatus implements Runnable {

  String ADMIN_HOST;
  int ADMIN_PORT;
  RMIAdminImpl master;
  AdminPeer adminStub = null;

  public CheckLeaderStatus(String ADMIN_HOST, int ADMIN_PORT, RMIAdminImpl master) {
    this.ADMIN_HOST = ADMIN_HOST;
    this.ADMIN_PORT = ADMIN_PORT;
    this.master = master;
    this.adminStub = RMIHandler.fetchRemoteObject("Admin", ADMIN_HOST, ADMIN_PORT);
  }


  @Override
  public void run() {
//    OutputHandler.printWithTimestamp("Checking Leader health");
    if (adminStub == null) {
      OutputHandler.printWithTimestamp(
          String.format("Unable to connect to Leader at HOST: %s", ADMIN_HOST));
      master.startElection();
    } else {
      try {
        adminStub.isActive();
      } catch (RemoteException e) {
        OutputHandler.printWithTimestamp(
            String.format("Admin leader did not respond at HOST: %s and PORT: %s", ADMIN_HOST,
                ADMIN_PORT));
        master.startElection();
      }
    }
  }


}

class ElectionProcessor extends UnicastRemoteObject implements Runnable, Remote, Serializable {

  boolean ANSWER_RECEIVED = false;
  RMIAdminImpl master;

  protected ElectionProcessor() throws RemoteException {

  }

  public ElectionProcessor(RMIAdminImpl master)
      throws RemoteException {
    this.master = master;
  }

  public void sendAnswer() {
    this.ANSWER_RECEIVED = true;
  }


  @Override
  public void run() {
    OutputHandler.printWithTimestamp("Starting a new Leader Election");
    master.IS_ELECTION_STARTED = true;

    ArrayList<AdminInfoPayload> higherPriorityPeers = Arrays.stream(master.peerAdmins)
        .filter(peer -> peer.getPRIORITY() > master.selfPriority).collect(
            Collectors.toCollection(ArrayList::new));
    for (AdminInfoPayload peer : higherPriorityPeers) {
      AdminPeer peerStub = RMIHandler.fetchRemoteObject("Admin", peer.getHOST(), peer.getPORT());
      if (peerStub != null) {
        try {
          peerStub.startElection(this);
        } catch (RemoteException e) {
          OutputHandler.printWithTimestamp("ERROR" + e.getMessage());
        }
      }
    }
    try {
      Thread.sleep(100);
      if (!ANSWER_RECEIVED) {
        // Make myself co-coordinator
        OutputHandler.printWithTimestamp("No answer received, announcing self as Leader");
        master.announceCoordinator(master.getSelfInfo());
        master.announceCoordinatorToBrokers(master.getSelfInfo());
        ArrayList<AdminInfoPayload> lowerPriorityPeers = Arrays.stream(master.peerAdmins)
            .filter(peer -> peer.getPRIORITY() < master.selfPriority).collect(
                Collectors.toCollection(ArrayList::new));
        for (AdminInfoPayload peer : lowerPriorityPeers) {
          AdminPeer peerStub = RMIHandler.fetchRemoteObject("Admin", peer.getHOST(),
              peer.getPORT());
          if (peerStub != null) {
            try {
              peerStub.announceCoordinator(master.getSelfInfo());
            } catch (RemoteException e) {
              OutputHandler.printWithTimestamp("ERROR" + e.getMessage());
            }
          }
        }

      } else {
        OutputHandler.printWithTimestamp("Answer received, waiting for Leader Announcement");
        // wait for some time to receive the coordinator message
        Thread.sleep(100);
        // if still no co-coordinator, start a new election
        if (!master.RECEIVED_COORDINATOR_ANNOUNCEMENT) {
          OutputHandler.printWithTimestamp(
              "No leader announcement received, starting a new election");
          master.endElection();
          master.startElection();
        }
      }
    } catch (InterruptedException | RemoteException e) {
      OutputHandler.printWithTimestamp("ERROR" + e.getMessage());
    }
    master.endElection();
  }


}
