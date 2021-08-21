package admin;

import common.RMIHandler;
import java.io.FileNotFoundException;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;

public class StartAdmin {

  /*
    Takes only 1 CMD Argument, i.e. the port on which the admin rmi will run.
   */


  public static void main(String[] args) throws FileNotFoundException {

//    System.setProperty("sun.rmi.transport.tcp.responseTimeout", "200");
    Registry registry;
    Integer PORT = Integer.valueOf(args[0]);
    Integer PRIORITY = Integer.valueOf(args[1]);

    try {
      Admin admin = new RMIAdminImpl("127.0.0.1", PORT, PRIORITY);
      RMIHandler.registerRemoteObject("Admin", admin, PORT,
          String.format("Started admin on HOST: %s PORT: %d",
              Inet4Address.getLocalHost().getHostAddress(), PORT));
    } catch (RemoteException e) {
      e.printStackTrace();
    } catch (UnknownHostException e) {
      e.printStackTrace();
    }
  }

}