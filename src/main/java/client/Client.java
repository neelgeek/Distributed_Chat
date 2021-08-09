package client;

import admin.AdminInterface;
import common.OutputHandler;
import common.RMIHandler;
import protocol.BrokerInfoPayload;
import protocol.UserInfoPayload;

import java.rmi.RemoteException;
import java.util.List;

/**
 * class represents client
 */
public class Client implements ClientToBrokerInterface {

    private String rmiName;
    private String username;
    private BrokerInfoPayload bip = null;

    public Client(String rmiName, String username) {
        this.rmiName = rmiName;
        this.username = username;
    }

    /**
     * Requests the admin to assign a broker to this client
     * @param adminName RMI name of the admin
     * @param host hostname of the rmi registry
     * @throws RemoteException
     * @throws InterruptedException
     */
    public void discoverBroker(String adminName, String host) throws RemoteException, InterruptedException {

        try {
            AdminInterface admin = RMIHandler.fetchRemoteObject(adminName, host);
            UserInfoPayload uip = new UserInfoPayload(rmiName, username, true);
            bip = admin.registerNewUser(uip);
        } catch (NullPointerException e) {
            OutputHandler.printWithTimestamp("Error: admin object is null");
        } catch (RemoteException | InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void sendUserUpdateInfo(List userUpdateInfo) throws RemoteException {

    }
    // entid
}
