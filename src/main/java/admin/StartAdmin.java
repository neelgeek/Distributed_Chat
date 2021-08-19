package admin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import common.OutputHandler;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class StartAdmin {

  /*
    Takes only 1 CMD Argument, i.e. the port on which the admin rmi will run.
   */


  public static void main(String[] args) throws FileNotFoundException {
    JsonParser parser = new JsonParser();
    JsonElement element = parser.parse(new FileReader("/Users/neelbhave/dev/mscs/Summer21/cs6650/Final Project/Distributed_Chat/src/main/resources/admin-config.json"));
    JsonObject object = element.getAsJsonObject();
    System.out.println(object);

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
