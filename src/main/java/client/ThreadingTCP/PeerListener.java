package client.ThreadingTCP;

import common.OutputHandler;
import protocol.ProtocolHandler;
import protocol.UserInfoPayload;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Map;

public class PeerListener extends Thread {

    protected InputStream s1In;
    protected DataInputStream dis;
    protected OutputStream s1out;
    protected DataOutputStream dos;

    protected InetAddress IP;
    protected int destPortNo;
    protected InetAddress destIP;

    private UserInfoPayload targetPeer;



    private Socket s_client;
    ClientToThreadInterface myClient;
    private Map<String, Socket> connectedPeers;


    // portNo == 0 means it's automatically allocated
    public PeerListener(Socket s_client, Map<String, Socket> connectedPeers) {
        this.s_client = s_client;
        this.connectedPeers = connectedPeers;
    }

    public void setTargetPeer(UserInfoPayload targetPeer) {
        this.targetPeer = targetPeer;
    }

    public Socket getS_client() {
        return s_client;
    }

    public void run() {

        // DEBUG
        System.out.println("Running peer listener on" + s_client.getLocalPort() + " to target "+ s_client.getPort());

        try {
            s1In = s_client.getInputStream();
            dis = new DataInputStream(s1In);
            s1out = s_client.getOutputStream();
            dos = new DataOutputStream(s1out);


            boolean userInfoSent = false;

            // get the first message from the user that contains sender info
            while (true) {
                if (dis.available() > 0) {
                    String uip_str = dis.readUTF();
                    if (ProtocolHandler.decodeJSONUserInfo(uip_str) != null) {
                        targetPeer = ProtocolHandler.decodeJSONUserInfo(uip_str);
                        userInfoSent = true;
                    }
                    OutputHandler.printWithTimestamp("Connected with user: \n" + targetPeer.getUserName());

                    if (!userInfoSent) {
                        String message = uip_str;
                        OutputHandler.printWithTimestamp(targetPeer.getUserName() + ": " + message);
                    }
                    break;
                }
            }



            connectedPeers.put(targetPeer.getUserName(), s_client);


            // loop for listening to messages
            while(true) {
                if (dis.available() > 0) {
                    String message = dis.readUTF();
                    OutputHandler.printWithTimestamp(targetPeer.getUserName() + ": " + message);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
