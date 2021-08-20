package client;

import client.ThreadingTCP.PeerListener;
import common.OutputHandler;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import protocol.ProtocolHandler;
import protocol.UserInfoPayload;

public class PeerRouter {

  private static Map<String, Socket> connectedPeers;


  public static void setConnectedPeers(Map<String, Socket> connectedPeers) {
    PeerRouter.connectedPeers = connectedPeers;
  }

  public static void sendMessageToPeer(UserInfoPayload sender, String targetUsername,
      String message, List<UserInfoPayload> allActiveUsers) {

    try {

      // first look for the user in
      for (String user : connectedPeers.keySet()) {

        if (user.equals(targetUsername)) {
          Socket targetSocket = connectedPeers.get(user);
          OutputStream os = targetSocket.getOutputStream();
          DataOutputStream dos = new DataOutputStream(os);
          dos.writeUTF(message);

          return;
        }
      }

      // if not a connected user, look in all active users and if found, send that user a message
      // containing the user payloaf info and then send the message
      for (UserInfoPayload user : allActiveUsers) {
        if (user.getUserName().equals(targetUsername)) {

          Socket newSocket = new Socket(user.getHOST(), user.getSOCKET_PORT());
          PeerListener listener = new PeerListener(newSocket, connectedPeers);
          listener.setTargetPeer(user);
          listener.start();
          connectedPeers.put(user.getUserName(), newSocket);
          OutputStream os = newSocket.getOutputStream();
          DataOutputStream dos = new DataOutputStream(os);
          dos.writeUTF(ProtocolHandler.encodeJSONUserInfo(sender));
          dos.writeUTF(message);
          return;
        }
      }

      OutputHandler.printWithTimestamp("Error: could not find username");

    } catch (IOException e) {
      OutputHandler.printWithTimestamp("Error: could not send message to peer");
      e.printStackTrace();
    }
  }
}

