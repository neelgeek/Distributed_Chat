package client.ThreadingTCP;

import protocol.UserInfoPayload;

import java.io.DataOutputStream;
import java.rmi.RemoteException;
import java.util.List;

public interface ClientToThreadInterface {

    void addPeerToOutputStream(DataOutputStream dos);

    void startPeerListener(int portNo);

    List<DataOutputStream> getAllOutputStreams();

    void setListenerPort(int portNo) throws RemoteException;
}
