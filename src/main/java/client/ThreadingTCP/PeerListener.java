package client.ThreadingTCP;

import common.OutputHandler;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

public class PeerListener extends Thread {

    protected InputStream s1In;
    protected DataInputStream dis;
    protected OutputStream s1out;
    protected DataOutputStream dos;

    protected InetAddress IP;
    protected int destPortNo;
    protected InetAddress destIP;

    private ServerSocket s;
    ClientToThreadInterface myClient;


    // portNo == 0 means it's automatically allocated
    public PeerListener(ClientToThreadInterface myClient, int portNo) {
        try {
            s = new ServerSocket(portNo);
            myClient.setListenerPort(s.getLocalPort());
            OutputHandler.printWithTimestamp("Success: created peer listener on port " + s.getLocalPort());
            this.myClient = myClient;
        } catch (IOException e) {
            OutputHandler.printWithTimestamp("Error: Could not create server socket");
            e.printStackTrace();
        }
    }

    public void run() {
            try (Socket s1 = s.accept()) {



                myClient.startPeerListener(0);


                s1In = s1.getInputStream();
                dis = new DataInputStream(s1In);
                s1out = s1.getOutputStream();
                dos = new DataOutputStream(s1out);

                // TODO: as of now, only one group exists
                myClient.addPeerToOutputStream(dos);

                // sendMessageAndLog("Success: Client connection established", "Response");
                // destIP = s1.getInetAddress();
                // destPortNo = s1.getPort();

                // TODO: change to accept uip!!
                while (true) {
                    if (dis.available() > 0) {
                        String input = dis.readUTF();
                        OutputHandler.printWithTimestamp("Message from peer: \n" + input);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
    }
}
