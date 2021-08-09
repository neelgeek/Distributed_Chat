package admin;

import common.OutputHandler;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class StartAdmin {

  public static void main(String[] args) {
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
