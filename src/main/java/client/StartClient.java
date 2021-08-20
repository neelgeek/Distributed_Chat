package client;

import java.rmi.RemoteException;

public class StartClient {

  /**
   * starts the client and asks for a broker from the admin
   *
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
    Integer clientPort = Integer.valueOf(args[3]);
    //TODO: this is the port to be used for the socket
    // Integer clientSocketPort = Integer.valueOf(args[4]);

    String successMessage = "Client successfully registered on PORT: " + clientPort;
    Client client = new Client(username, adminHost, adminPORT);
    client.discoverBroker();
    client.registerRMI(rmiName, clientPort, successMessage);

    client.startPeerHarbor(0);
    // client.connectWithAllPeers();
    client.startClient();

    /*List<UserInfoPayload> users = client.getActiveUsers();
    System.out.println("Active Users:");
    for (UserInfoPayload uip : users) {
      System.out.println(uip.getUserName());
    }*/
  }
}
