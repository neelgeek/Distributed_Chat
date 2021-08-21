package common;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import protocol.AbstractNetworkEntity;
import protocol.PaxActions;
import protocol.PaxMessage;
import protocol.Replicable;

public class PaxosReplication implements Runnable {

  private Integer current_proposalID;
  private AbstractNetworkEntity[] PAX_SERVERS;
  private Replicable toReplicate;
  private String paxServerRMIName;

  public PaxosReplication(Integer current_proposalID, AbstractNetworkEntity[] PAX_SERVERS,
      Replicable toReplicate, String paxServerRMIName) {
    this.current_proposalID = current_proposalID;
    this.PAX_SERVERS = PAX_SERVERS;
    this.toReplicate = toReplicate;
    this.paxServerRMIName = paxServerRMIName;
  }

  @Override
  public void run() {
    OutputHandler.printWithTimestamp(
        String.format("Replication Request submitted for %s with id: %d", toReplicate.toString(),
            current_proposalID));

    ArrayList<PaxosServer> ACCEPTORS = new ArrayList<>();
    for (AbstractNetworkEntity paxServer : PAX_SERVERS) {
      PaxosServer server = RMIHandler.fetchRemoteObject(paxServerRMIName, paxServer.getHOST(),
          paxServer.getPORT());
      if (server != null) {
        ACCEPTORS.add(server);
      }
    }

    ArrayList<PaxMessage> promises = new ArrayList<>();
    for (PaxosServer acceptor : ACCEPTORS) {
      try {
        PaxMessage promise = acceptor.prepare(this.current_proposalID);
        promises.add(promise);
      } catch (RemoteException | NullPointerException e) {
        OutputHandler.printWithTimestamp(
            String.format("No response for PREPARE for id: %d", current_proposalID));
      }
    }

    ArrayList<PaxMessage> positivePromises = promises.stream()
        .filter(paxMessage -> paxMessage.getType().equals(
            PaxActions.PROMISE)).collect(Collectors.toCollection(ArrayList::new));

    if (positivePromises.size() >= (ACCEPTORS.size() / 2) + 1) {
      OutputHandler.printWithTimestamp(
          String.format("Majority achieved for PROMISE for id: %d ", current_proposalID));

      PaxMessage highestPromise = positivePromises.stream()
          .max(Comparator.comparing(PaxMessage::getAcceptedID))
          .orElseThrow(NoSuchElementException::new);

      PaxMessage acceptMessage;
      if (highestPromise.getAcceptedID() == -1) {
        acceptMessage = new PaxMessage(current_proposalID, toReplicate, PaxActions.ACCEPT);
      } else {
        acceptMessage = new PaxMessage(current_proposalID, highestPromise.getAcceptedValue(),
            PaxActions.ACCEPT);
      }

      ArrayList<PaxMessage> acceptedMessages = new ArrayList<>();
      for (PaxosServer acceptor : ACCEPTORS) {
        try {
          PaxMessage accepted = acceptor.accept(acceptMessage);
          acceptedMessages.add(accepted);
        } catch (RemoteException e) {
          OutputHandler.printWithTimestamp(
              String.format("No response for ACCEPT for id: %d", current_proposalID));
        }
      }

      ArrayList<PaxMessage> positiveAccepts = acceptedMessages.stream()
          .filter(paxMessage -> paxMessage.getType().equals(PaxActions.ACCEPTED))
          .collect(Collectors.toCollection(ArrayList::new));

      if (positiveAccepts.size() >= ACCEPTORS.size() / 2 + 1) {
        for (PaxosServer peer : ACCEPTORS) {
          try {
            peer.announce(acceptMessage);
          } catch (RemoteException e) {
            e.printStackTrace();
          }
        }
        OutputHandler.printWithTimestamp(
            String.format("Replication successful for ID: %d", current_proposalID));
      } else {
//        finalResponse.setSuccess(false);
//        finalResponse.setMessage("Failed to Replicate the value, due to failure in Acceptors");
        OutputHandler.printWithTimestamp(
            String.format(
                "Failed to Replicate the value for proposalID: %d, due to failure in Acceptors",
                current_proposalID));

      }
    } else {
//      finalResponse.setSuccess(false);
//      finalResponse.setMessage("Failed to Replicate the value, due to failure in Acceptors");
      OutputHandler.printWithTimestamp(
          String.format(
              "Failed to Replicate the value for proposalID: %d, due to failure in Acceptors",
              current_proposalID));
    }

  }
}
