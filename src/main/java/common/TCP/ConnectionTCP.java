package common.TCP;

import protocol.UserInfoPayload;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class ConnectionTCP extends AbstractTCPNode {

    public String startConnection(UserInfoPayload uip) {
        String host = uip.getHOST();
        int portNo = uip.getSOCKET_PORT();

        System.out.println("Creating connection...");

        try (Socket s1 = new Socket(host, portNo)) {
            logMessage("Success: Connected to peer: " + uip.getUserName());
            s1.setSoTimeout(1000);
            try {
                s1In = s1.getInputStream();
                dis = new DataInputStream(s1In);
                s1out = s1.getOutputStream();
                dos = new DataOutputStream(s1out);

                Scanner in = new Scanner(System.in);
                String response;

                while (true) {
                    System.out.println("Please type a query in on of the following formats:");
                    System.out.println("PUT <string> <string>");
                    System.out.println("GET <string>");
                    System.out.println("DELETE <string>");

                    String query = in.nextLine();
                    query.trim();

                    sendMessageAndLog(query, "Request");
                    try {
                        response = readMessageAndLog("Client");
                        String error = CRUDHandler.processResponse(response, query);
                        if (!error.equals("")) {
                            logMessage(error);
                        }
                    } catch (IOException e) {
                        throw new IOException("Error: could not read input stream");
                    }
                }

                /*System.out.println("Please type a message for the server");
                String message = in.nextLine();
                dos.writeUTF(message);
                String serverMessage;
                try {
                    serverMessage = dis.readUTF();
                    System.out.println("Server Message: " + serverMessage);
                } catch (IOException e) {
                    System.out.println("Error: could not read input stream from server");
                }*/
            } catch (IOException e) {
                throw new IOException("Error: could not create I/O streams");
            }
        } catch (IOException e) {
            throw new IOException("Error: could not establish client socket connection. Client shutting down...");
        }
    }

}
