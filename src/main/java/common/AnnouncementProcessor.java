package common;

import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentHashMap;
import protocol.AbstractNetworkEntity;
import protocol.BrokerInfoPayload;
import protocol.Replicable;
import protocol.UserInfoPayload;

/**
 * The Announcements from the Acceptors are added to a {@link ConcurrentHashMap} named
 * announcementMap so that duplicate transactions don't take place in the store. This thread
 * periodically checks the operations accumulated in the map and then starts new threads to carry
 * them out on the key value store.
 */
public class AnnouncementProcessor implements Runnable {

  private ConcurrentHashMap<Integer, Replicable> announcementMap;
  private PaxosServer server;

  public AnnouncementProcessor(
      ConcurrentHashMap<Integer, Replicable> announcementMap, PaxosServer server) {
    this.announcementMap = announcementMap;
    this.server = server;
  }

  @Override
  public void run() {
    String entityType = "";
    for (Integer proposalID : this.announcementMap.keySet()) {
      Replicable requestPayload = this.announcementMap.get(proposalID);

      if (requestPayload instanceof UserInfoPayload) {
        entityType = "User";
      } else if (requestPayload instanceof BrokerInfoPayload) {
        entityType = "Broker";
      }
      try {
        if (((AbstractNetworkEntity) requestPayload).isActive()) {
          server.put(((AbstractNetworkEntity) requestPayload).getEntityID(),
              requestPayload);
          OutputHandler.printWithTimestamp(
              String.format("Data added for new %s with ID: %s at HOST: %s",
                  entityType,
                  ((AbstractNetworkEntity) requestPayload).getEntityID(),
                  ((AbstractNetworkEntity) requestPayload).getHOST()));
        } else {
          server.remove(((AbstractNetworkEntity) requestPayload).getEntityID());
          OutputHandler.printWithTimestamp(
              String.format("Data deleted for %s with ID: %s at HOST: %s",
                  entityType,
                  ((AbstractNetworkEntity) requestPayload).getEntityID(),
                  ((AbstractNetworkEntity) requestPayload).getHOST()));
        }
      } catch (RemoteException e) {
        OutputHandler.printWithTimestamp("Error: " + e.getMessage());
      }
      this.announcementMap.remove(proposalID);
    }
  }
}
