package broker;

import admin.Admin;
import admin.PaxosProposer;
import common.ClientStatusChecker;
import common.HeartbeatReceiver;
import common.OutputHandler;
import common.PaxosServer;
import common.PingHeartbeat;
import common.RMIHandler;
import common.StatusMaintainer;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import protocol.BrokerInfoPayload;
import protocol.GroupChat;
import protocol.GroupChatInfoPayload;
import protocol.PaxActions;
import protocol.PaxMessage;
import protocol.Replicable;
import protocol.UserInfoPayload;

public class BrokerImpl extends UnicastRemoteObject implements Broker,
    HeartbeatReceiver<UserInfoPayload>, StatusMaintainer, PaxosServer {

  private HashMap<String, BrokerInfoPayload> peerBrokers = new HashMap<>();
  protected HashMap<String, UserInfoPayload> userRecord = new HashMap<>();
  private HashMap<String, Long> userTimeouts = new HashMap<>();
  private HashMap<String, GroupChat> groupChatsRecord = new HashMap<>();

  private Admin adminServer;
  private PaxosProposer adminProposer;
  private Registry registry;
  private BrokerInfoPayload selfRecord;
  private String ADMIN_HOST;
  private int ADMIN_PORT, SELF_PORT;
  private ScheduledExecutorService brokerHeartbeatService = Executors.newSingleThreadScheduledExecutor();
  private ScheduledExecutorService clientStatusService = Executors.newSingleThreadScheduledExecutor();
  private ScheduledExecutorService announcementProcessorService = Executors.newScheduledThreadPool(
      1);
  ;

  // For Paxos
  private Integer maxID;
  private Integer last_accepted_proposalID;
  private Replicable last_accepted_value;
  private ConcurrentHashMap<Integer, Replicable> announcementMap;

  public BrokerImpl(String ADMIN_HOST, Integer ADMIN_PORT, Integer SELF_PORT)
      throws RemoteException {
    super();
    this.ADMIN_HOST = ADMIN_HOST;
    this.ADMIN_PORT = ADMIN_PORT;
    this.SELF_PORT = SELF_PORT;
    this.announcementMap = new ConcurrentHashMap<>();

    this.selfRecord = new BrokerInfoPayload(UUID.randomUUID().toString(), SELF_PORT, true);
    OutputHandler.printWithTimestamp(String.format("Broker Info: %s", selfRecord));
    try {
      this.adminServer = RMIHandler.fetchRemoteObject("Admin", ADMIN_HOST,
          ADMIN_PORT);
      boolean connectAdmin = adminServer.registerNewBroker(selfRecord);
      if (connectAdmin) {
        OutputHandler.printWithTimestamp(String.format("Connected to Admin server!"));
        getActivePeers(adminServer);
        updateActiveUsers();
      }

      this.adminProposer = RMIHandler.fetchRemoteObject("Admin", ADMIN_HOST,
          ADMIN_PORT);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    this.brokerHeartbeatService.scheduleAtFixedRate(
        new PingHeartbeat(ADMIN_HOST, ADMIN_PORT, "Admin", selfRecord), 1, 1,
        TimeUnit.SECONDS);

    this.clientStatusService.scheduleAtFixedRate(
        new ClientStatusChecker(this.userTimeouts, this, new Long(2)), 2, 2, TimeUnit.SECONDS);

    announcementProcessorService.scheduleAtFixedRate(
        new AnnouncementProcessor(announcementMap), 1, 1,
        TimeUnit.SECONDS);
  }

  protected BrokerImpl() throws RemoteException {
    super();
  }


  private void getActivePeers(Admin admin) throws RemoteException {
    ArrayList<BrokerInfoPayload> activePeers = (ArrayList<BrokerInfoPayload>) admin.getActiveBrokers();
    for (BrokerInfoPayload peer : activePeers) {
      if (peer.getEntityID().equals(selfRecord.getEntityID())) {
        continue;
      }
      this.peerBrokers.put(peer.getEntityID(), peer);
      OutputHandler.printWithTimestamp(
          String.format("Added Active peer with id: %s and Host: %s", peer.getEntityID(),
              peer.getHOST()));
    }
  }

  private void updateActiveUsers() {
    Broker peerBrokerStub = null;
    for (BrokerInfoPayload peer : peerBrokers.values()) {
      peerBrokerStub = RMIHandler.fetchRemoteObject("Broker", peer.getHOST(), peer.getPORT());
      if (peerBrokerStub != null) {
        break;
      }
    }
    try {
      ArrayList<UserInfoPayload> activeUsers = (ArrayList<UserInfoPayload>) peerBrokerStub.getActiveUsers();
      for (UserInfoPayload user : activeUsers) {
        this.userRecord.put(user.getEntityID(), user);
      }
      OutputHandler.printWithTimestamp(
          String.format("Added %d Active user(s) fetched from Peer broker", activeUsers.size()));
    } catch (RemoteException e) {
      e.printStackTrace();
      updateActiveUsers();
    }


  }

  @Override
  public void sendBrokerUpdate(BrokerInfoPayload brokerInfo) throws RemoteException {
    if (!brokerInfo.isActive() && peerBrokers.containsKey(brokerInfo.getEntityID())) {
      this.peerBrokers.remove(brokerInfo.getEntityID());
      OutputHandler.printWithTimestamp(
          String.format("Broker with ID: %s HOST: %s timed out. Setting status to inActive.",
              brokerInfo.getEntityID(), brokerInfo.getHOST()));
    } else if (!peerBrokers.containsKey(brokerInfo.getEntityID())) {
      this.peerBrokers.put(brokerInfo.getEntityID(), brokerInfo);
      OutputHandler.printWithTimestamp(
          String.format("New Peer Broker with ID: %s HOST: %s added.",
              brokerInfo.getEntityID(), brokerInfo.getHOST()));
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
      this.adminProposer.submitRequest(userInfoPayload);
      OutputHandler.printWithTimestamp(String.format("User registered: %s", userInfoPayload));
      return true;
    }
    return false;
  }

  @Override
  public List<UserInfoPayload> getActiveUsers() throws RemoteException {
    ArrayList<UserInfoPayload> activeUsers = this.userRecord.values().stream()
        .filter(user -> user.isActive()).collect(
            Collectors.toCollection(ArrayList::new));
    return activeUsers;
  }

  @Override
  public List<GroupChat> getGroupChats() throws RemoteException {
    return (List<GroupChat>) this.groupChatsRecord.values();
  }

  @Override
  public GroupChat createGroupChat(String groupName, UserInfoPayload creator)
      throws RemoteException {
    GroupChatInfoPayload newChat = new GroupChatInfoPayload(groupName);
    newChat.addParticipant(creator);
    this.groupChatsRecord.put(newChat.getGroupID(), newChat);
    return newChat;
  }

  @Override
  public GroupChat joinGroupChat(UserInfoPayload joiningUser, GroupChat groupChat)
      throws RemoteException {
    GroupChatInfoPayload gc = (GroupChatInfoPayload) groupChat;
    if (this.groupChatsRecord.containsKey(gc.getGroupID())) {
      GroupChatInfoPayload existingGroup = (GroupChatInfoPayload) this.groupChatsRecord.get(
          gc.getGroupID());
      existingGroup.addParticipant(joiningUser);
      //TODO: Add announcement logic
      return existingGroup;
    }
    return null;
  }

  @Override
  public boolean leaveGroupChat(UserInfoPayload leavingUser, GroupChat groupChat)
      throws RemoteException {
    GroupChatInfoPayload gc = (GroupChatInfoPayload) groupChat;
    if (groupChatsRecord.containsKey(gc.getGroupID())) {
      GroupChatInfoPayload existingGroup = (GroupChatInfoPayload) groupChatsRecord.get(
          gc.getGroupID());
      existingGroup.removeParticipant(leavingUser);
      return true;
    }
    return false;
  }

  @Override
  public void sendHeartBeat(UserInfoPayload clientInfo) throws RemoteException {
    try {
      String remoteHost = RemoteServer.getClientHost();
      clientInfo.setHOST(remoteHost);
//      OutputHandler.printWithTimestamp(
//          String.format("Heartbeat Received from Client at: %s", remoteHost));
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

  private void endPaxosRun() {
    this.last_accepted_proposalID = null;
    this.last_accepted_value = null;
    this.maxID = 0;
  }

  private void announceLearners(PaxMessage announceMessage) {
    for (BrokerInfoPayload peer : peerBrokers.values()) {
      PaxosServer learner = RMIHandler.fetchRemoteObject("Broker", peer.getHOST(), peer.getPORT());
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


/**
 * The Announcements from the Acceptors are added to a {@link ConcurrentHashMap} named
 * announcementMap so that duplicate transactions don't take place in the store. This thread
 * periodically checks the operations accumulated in the map and then starts new threads to carry
 * them out on the key value store.
 */
class AnnouncementProcessor extends BrokerImpl implements Runnable {

  private ConcurrentHashMap<Integer, Replicable> announcementMap;

  public AnnouncementProcessor(
      ConcurrentHashMap<Integer, Replicable> announcementMap) throws RemoteException {
    super();
    this.announcementMap = announcementMap;
  }

  @Override
  public void run() {
    for (Integer proposalID : this.announcementMap.keySet()) {
      Replicable requestPayload = this.announcementMap.get(proposalID);
      if (requestPayload instanceof UserInfoPayload) {
        userRecord.put(((UserInfoPayload) requestPayload).getEntityID(),
            (UserInfoPayload) requestPayload);
        OutputHandler.printWithTimestamp(
            String.format("User data replicated for new user with ID: %s at HOST: %s",
                ((UserInfoPayload) requestPayload).getEntityID(),
                ((UserInfoPayload) requestPayload).getHOST()));
      }
      this.announcementMap.remove(proposalID);
    }
  }
}
