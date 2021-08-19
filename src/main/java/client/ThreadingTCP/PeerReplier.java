package client.ThreadingTCP;

import common.OutputHandler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;

public class PeerReplier extends Thread {

    private ClientToThreadInterface client;

    public PeerReplier(ClientToThreadInterface client) {
        this.client = client;
    }

    public void run() {

        while (true) {
            OutputHandler.printWithTimestamp("Type message for peer:");
            Scanner in = new Scanner(System.in);
            String message = in.nextLine();
            List<DataOutputStream> streams = client.getAllOutputStreams();
            for (DataOutputStream output : streams) {
                try {
                    output.writeUTF(message);
                } catch (IOException e) {
                    OutputHandler.printWithTimestamp("Error: could not send message to peer");
                    e.printStackTrace();
                }
            }
        }
    }
}
