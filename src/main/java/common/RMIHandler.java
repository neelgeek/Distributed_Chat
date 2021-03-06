package common;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Class with static methods to register or fetch remote objects
 */
public class RMIHandler {

  /**
   * Register a given object with the RMI registry
   *
   * @param RMIName the name of the object in the registry
   * @param obj     the object to register
   * @param PORT    the port to register on
   * @param message the message displayed after successful registration
   */
  public static void registerRemoteObject(String RMIName, Remote obj, Integer PORT,
      String message) {
    try {
      Registry registry = LocateRegistry.createRegistry(PORT);

      registry.bind(RMIName, obj);
      OutputHandler.printWithTimestamp(message);
    } catch (RemoteException | AlreadyBoundException e) {
      e.printStackTrace();
    }
  }

  /**
   * Returns a (typed) remote object given the object's RMI name and a host (if null, then is
   * localhost)
   *
   * @param RMIname name of the remote object in the registry
   * @param host    host name of the RMI registry server
   * @param <T>     generic type of object
   * @return object with the given RMI name
   */
  public static <T> T fetchRemoteObject(String RMIname, String host, Integer PORT) {

    String errorMessage = "Error: could not find remote object";

    if (PORT == null) {
      PORT = 1099;
    }

    try {
      String name = RMIname;
      Registry registry = LocateRegistry.getRegistry(host, PORT);
      T obj = (T) registry.lookup(name);

      return obj;

    } catch (RemoteException | NotBoundException e) {
      OutputHandler.printWithTimestamp(
          errorMessage + String.format(" HOST: %s PORT: %d", host, PORT));
      return null;
    }
  }
}

