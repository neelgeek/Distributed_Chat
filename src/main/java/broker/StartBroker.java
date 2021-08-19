package broker;

import common.OutputHandler;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import protocol.GroupChat;
import protocol.GroupChatInfoPayload;

/**
 * Starts new Brokers
 */
public class StartBroker {

  /*
    Takes 3 CMD Arguments -
    1. HOST address of admin - pass 127.0.0.1 for local
    2. PORT of admin
    3. PORT on which this broker will run. Use unique PORT numbers for each broker.
    Use $Prompt$ if you want Intellij to pop up an input box for this argument each time you
    run the broker.
   */
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
