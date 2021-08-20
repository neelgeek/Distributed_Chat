package client.ThreadingTCP;

import protocol.UserInfoPayload;

import java.io.DataOutputStream;
import java.rmi.RemoteException;
import java.util.List;

public interface ClientToThreadInterface {

    // void addPeerToGroup(UserInfoPayload uip);

    void startPeerHarbor(int portNo) throws RemoteException;

    // List<UserInfoPayload> getAllConnectedPeers();

}
