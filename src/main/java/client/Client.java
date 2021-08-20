package client;

import admin.Admin;
import broker.Broker;
import client.ThreadingTCP.ClientToThreadInterface;
import client.ThreadingTCP.PeerHarbor;
import client.ThreadingTCP.PeerListener;
import common.OutputHandler;
import common.PingHeartbeat;
import common.RMIHandler;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import protocol.BrokerInfoPayload;
import protocol.UserInfoPayload;

/**
 * class represents client
 */
public class Client implements ClientToBrokerInterface, ClientToThreadInterface {

  private final String adminHost;
  private Integer adminPort;
  private final String adminName = "Admin";
  private final String brokerName = "Broker";
  private BrokerInfoPayload bip = null;
  private Broker brokerStub;

  private PeerHarbor harbor;

  private Map<String, Socket> connectedPeers = new HashMap<>();

  private UserInfoPayload selfRecord;
  private ScheduledExecutorService clientHeartbeatService = Executors.newSingleThreadScheduledExecutor();

  private List<DataOutputStream> peerOutputStreams = new ArrayList<>();

  public Client(String username, String adminHost, Integer adminPort) {
    this.adminHost = adminHost;
    this.adminPort = adminPort;

    this.selfRecord = new UserInfoPayload(UUID.randomUUID().toString(), username, true);
    OutputHandler.printWithTimestamp(String.format("Client Info: %s", selfRecord));
    PeerRouter.setConnectedPeers(connectedPeers);
  }


  /**
   * Creates a separate thread and starts sending heartbeat to Broker
   */
  public void startSendingHearbeat() {
    clientHeartbeatService.scheduleAtFixedRate(
        new PingHeartbeat(bip.getHOST(), bip.getPORT(), "Broker", selfRecord), 1, 1,
        TimeUnit.SECONDS);
  }

  /**
   * Note: IF using TCP, delete this method
   *
   * @param PORT
   * @param successMessage
   */
  public void registerRMI(String objectName, int PORT, String successMessage) {
    RMIHandler.registerRemoteObject(objectName, this, PORT, successMessage);
  }

  /**
   * Requests the admin to assign a broker to this client
   *
   * @throws RemoteException
   * @throws InterruptedException
   */
  public void discoverBroker() throws RemoteException, InterruptedException {

    if (adminPort == null) {
      adminPort = 1099;
    }

    try {
      Admin admin = RMIHandler.fetchRemoteObject(adminName, adminHost, adminPort);
      UserInfoPayload uip = new UserInfoPayload(selfRecord.getEntityID(), selfRecord.getUserName(),
          true);
      assert admin != null;
      bip = admin.registerNewUser(uip);
      brokerStub = RMIHandler.fetchRemoteObject(brokerName, bip.getHOST(), bip.getPORT());
      registerUser();
      startSendingHearbeat();
    } catch (NullPointerException e) {
      e.printStackTrace();
      OutputHandler.printWithTimestamp("Error: admin object is null");
    } catch (RemoteException | InterruptedException e) {
      e.printStackTrace();
    }

  }

  public boolean registerUser() {
    try {
      return this.brokerStub.registerUser(selfRecord);
    } catch (RemoteException e) {
      e.printStackTrace();
    }
    return false;
  }

  public void startClient() throws RemoteException {
    String targetUser = "";
    while (true) {
      Scanner sc = new Scanner(System.in);
      String user = sc.nextLine();
      String message = sc.nextLine();

      if (!user.equals("")) {
        sendMessageToPeer(user, message);
        targetUser = user;
      } else {
        sendMessageToPeer(targetUser, message);
      }
    }
  }


  @Override
  public void sendUserUpdateInfo(List userUpdateInfo) throws RemoteException {

  }
  // entid

  @Override
  public void startPeerHarbor(int portNo) throws RemoteException {
    this.harbor = new PeerHarbor(portNo, connectedPeers);
    setListenerPort(harbor.getHarborPort());
    harbor.start();
  }

  private void setListenerPort(int portNo) throws RemoteException {
    selfRecord.setSOCKET_PORT(portNo);
    brokerStub.setUserPortNumber(selfRecord.getEntityID(), portNo);
  }

  public void sendMessageToPeer(String username, String message) throws RemoteException {
    PeerRouter.sendMessageToPeer(selfRecord, username, message, brokerStub.getActiveUsers());
  }


  private List<UserInfoPayload> getActiveUsers() throws RemoteException {
    return brokerStub.getActiveUsers();
  }

  // TODO: check to see if a client is already connected!
  /*public void connectWithAllPeers() throws RemoteException {
    List<UserInfoPayload> uips = brokerStub.getActiveUsers();

    for (UserInfoPayload user : uips) {
      if (user.getUserName().equals(this.selfRecord.getUserName())) {
        continue;
      }

      try {
        PeerRouter.sendMessageToPeer(user.getUserName(), "", );
        Socket s1 = new Socket(host, portNo);
        peerSockets.add(s1);
        OutputHandler.printWithTimestamp("Success: connected with" + user.getUserName() + " on " + portNo);
        // s1.setSoTimeout(1000);
        OutputStream s1out = s1.getOutputStream();
        DataOutputStream dos = new DataOutputStream(s1out);
        peerOutputStreams.add(dos);
      } catch (UnknownHostException e) {
        OutputHandler.printWithTimestamp("Error: could not identify host");
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }*/


}
