package client;

import common.OutputHandler;
import common.RMIHandler;

import java.rmi.RemoteException;

public class StartClient {

    /**
     * starts the client and asks for a broker from the admin
     * @param args arguments of the form "username PORT"
     * @throws RemoteException
     * @throws InterruptedException
     */
    public static void main(String args[]) throws RemoteException, InterruptedException {
        // change later to make the name unique
        String rmiName = "Client";
        String adminName = "Admin";

        String username = args[0];
        String adminHost = args[1];
        Integer adminPORT = Integer.valueOf(args[2]);
        Integer brokerPort = Integer.valueOf(args[3]);
        String successMessage = "Client " + rmiName + " successfully registered";
        Client client = new Client(username, adminHost, adminPORT);

        client.discoverBroker();
        client.startSendingHearbeat();

    }
}
