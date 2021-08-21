package common;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Calls the Heartbeat method in the admin, notifying it that this Broker is active.
 */
public class PingHeartbeat<I> implements Runnable {

  private String SERVER_HOST;
  private int SERVER_PORT;
  private String SERVER_STUB_NAME;
  private HeartbeatReceiver serverStub;
  private I selfInfo;


  public PingHeartbeat(String SERVER_HOST, int SERVER_PORT, String SERVER_STUB_NAME,
      I selfInfo) {
    this.SERVER_HOST = SERVER_HOST;
    this.SERVER_PORT = SERVER_PORT;
    this.SERVER_STUB_NAME = SERVER_STUB_NAME;
    this.serverStub = getServerStub();
    this.selfInfo = selfInfo;

  }

  @Override
  public void run() {
    if (this.serverStub == null) {
      this.serverStub = getServerStub();
    }
    try {
      this.serverStub.sendHeartBeat(selfInfo);
//      OutputHandler.printWithTimestamp(
//          "Sent Broker heart beat to " + SERVER_STUB_NAME);
    } catch (RemoteException | NullPointerException e) {
      OutputHandler.printWithTimestamp(
          "ERROR: Unable to connect to the " + SERVER_STUB_NAME + " for sending heartbeat");
    }
  }

  private HeartbeatReceiver getServerStub() {
    Registry registry;
    try {
      registry = LocateRegistry.getRegistry(SERVER_HOST, SERVER_PORT);
      HeartbeatReceiver serverStub = (HeartbeatReceiver) registry.lookup(SERVER_STUB_NAME);
      return serverStub;
    } catch (RemoteException e) {
      e.printStackTrace();
    } catch (NotBoundException e) {
      e.printStackTrace();
    }
    return null;
  }

  public void updateAdmin(String HOST, Integer PORT) {
    this.SERVER_HOST = HOST;
    this.SERVER_PORT = PORT;
    this.serverStub = getServerStub();
  }
}
