package client.ThreadingTCP;

import common.OutputHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PeerHarbor extends Thread {

    ServerSocket s;
    Map<String, Socket> connectedPeers;

    public PeerHarbor(int portNo, Map<String, Socket> connectedPeers) {
        try {
            s = new ServerSocket(portNo);
            OutputHandler.printWithTimestamp("Success: created peer harbor on port " + s.getLocalPort());
            this.connectedPeers = connectedPeers;
        } catch (IOException e) {
            OutputHandler.printWithTimestamp("Error: Could not create server socket for harbor");
            e.printStackTrace();
        }
    }

    public int getHarborPort() {
        return s.getLocalPort();
    }

    public void run() {

        while(true) {
            try {
                Socket s_client = s.accept();
                boolean seenBefore = false;
                for (Socket soc : connectedPeers.values()) {
                    if (soc.getLocalAddress().equals(s_client.getLocalAddress()) &&
                            soc.getLocalPort() == s_client.getLocalPort()) {
                        seenBefore = true;
                        break;
                    }
                }

                if (!seenBefore) {
                    PeerListener listener = new PeerListener(s_client, connectedPeers);
                    listener.start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
