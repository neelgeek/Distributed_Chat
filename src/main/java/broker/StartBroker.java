package broker;

import common.OutputHandler;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.UUID;

/**
 * Starts new Brokers
 */
public class StartBroker {

  public static void main(String[] args) {

    Registry registry;
    String ADMIN_HOST = args[0];
    Integer ADMIN_PORT = Integer.valueOf(args[1]);
    Integer PORT = Integer.valueOf(args[2]);

    try {
      Broker broker = new BrokerImpl(ADMIN_HOST, ADMIN_PORT, PORT);
      registry = LocateRegistry.createRegistry(PORT);
      registry.bind("Broker", broker);
    } catch (RemoteException e) {
      e.printStackTrace();
    } catch (AlreadyBoundException e) {
      e.printStackTrace();
    }
    OutputHandler.printWithTimestamp(String.format("Broker started on PORT: %d", PORT));
  }


}
