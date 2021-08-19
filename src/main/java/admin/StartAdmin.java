package admin;

import common.OutputHandler;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class StartAdmin {

  /*
    Takes only 1 CMD Argument, i.e. the port on which the admin rmi will run.
   */
  public static void main(String[] args) {
//    System.setProperty("sun.rmi.transport.tcp.responseTimeout", "200");
    Registry registry;
    Integer PORT = Integer.valueOf(args[0]);

    try {
      registry = LocateRegistry.createRegistry(PORT);

      Admin admin = new RMIAdminImpl();
      registry.rebind("Admin", admin);
      OutputHandler.printWithTimestamp(String.format("Start admin on HOST: %s PORT: %d",
          Inet4Address.getLocalHost().getHostAddress(), PORT));
    } catch (RemoteException e) {
      e.printStackTrace();
    } catch (UnknownHostException e) {
      e.printStackTrace();
    }
  }

}
