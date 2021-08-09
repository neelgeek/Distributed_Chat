package common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface HeartbeatReceiver<I> extends Remote {

  void sendHeartBeat(I clientInfo) throws RemoteException;

}
