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
        String rmiName = "client";
        String adminName = "admin";

        String username = args[0];
        Integer PORT = Integer.valueOf(args[1]);
        String successMessage = "Client " + rmiName + " successfully registered";
        Client client = new Client(rmiName, username);

        try {
            RMIHandler.registerRemoteObject(rmiName, client, PORT, successMessage);
            client.discoverBroker(adminName, null, null);
        } catch (RemoteException | InterruptedException e) {
            OutputHandler.printWithTimestamp("Error: broker discovery failed");
            e.printStackTrace();
        }
    }
}
