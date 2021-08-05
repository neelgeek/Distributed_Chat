package admin;

import common.OutputHandler;
import java.rmi.AlreadyBoundException;
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
      registry.bind("Admin", admin);
      OutputHandler.printWithTimestamp(String.format("Start admin on PORT: %d", PORT));
    } catch (RemoteException e) {
      e.printStackTrace();
    } catch (AlreadyBoundException e) {
      e.printStackTrace();
    }
  }

}
