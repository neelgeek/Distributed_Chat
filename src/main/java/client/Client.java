package client;

import admin.Admin;
import common.OutputHandler;
import common.PingHeartbeat;
import common.RMIHandler;
import protocol.BrokerInfoPayload;
import protocol.UserInfoPayload;

import java.rmi.RemoteException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * class represents client
 */
public class Client implements ClientToBrokerInterface {

    private final String adminHost;
    private Integer adminPort;
    private final String adminName = "Admin";
    private BrokerInfoPayload bip = null;

    private UserInfoPayload selfRecord;
    private ScheduledExecutorService clientHeartbeatService = Executors.newSingleThreadScheduledExecutor();

    public Client(String username, String adminHost, Integer adminPort) {
        this.adminHost = adminHost;
        this.adminPort = adminPort;

        this.selfRecord = new UserInfoPayload(UUID.randomUUID().toString(), username, true);
    }


    /**
     * Creates a separate thread and starts sending heartbeat to Broker
     */
    public void startSendingHearbeat() {
        clientHeartbeatService.scheduleAtFixedRate(
                new PingHeartbeat(bip.getHOST(),bip.getPORT(), "Broker", selfRecord), 1, 1,
                TimeUnit.SECONDS);
    }

    /**
     * Note: IF using TCP, delete this method
     * @param PORT
     * @param successMessage
     */
    public void registerRMI(int PORT, String successMessage) {
        RMIHandler.registerRemoteObject(selfRecord.getEntityID(), this, PORT, successMessage);
    }

    /**
     * Requests the admin to assign a broker to this client
     * @throws RemoteException
     * @throws InterruptedException
     */
    public void discoverBroker() throws RemoteException, InterruptedException {

        if (adminPort == null) {
            adminPort = 1099;
        }

        try {
            Admin admin = RMIHandler.fetchRemoteObject(adminName, adminHost, adminPort);
            UserInfoPayload uip = new UserInfoPayload(selfRecord.getEntityID(), selfRecord.getUserName(), true);
            assert admin != null;
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
