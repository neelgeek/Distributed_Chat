package common.TCP;

import java.io.*;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.sql.Timestamp;

/**
 * abstract class to represent either client or server over TCP
 */
public abstract class AbstractTCPNode  {
    protected InputStream s1In;
    protected DataInputStream dis;
    protected OutputStream s1out;
    protected DataOutputStream dos;

    protected InetAddress IP;
    protected int destPortNo;
    protected InetAddress destIP;

    // inherited method
    protected String readMessageAndLog(String nodeType) throws IOException {
        String message = null;
        try {
            message = dis.readUTF();
            if (nodeType.equals("Server")) {
                logMessage(nodeType + " Received from " + destIP + ":" + destPortNo + ":\n" + message);
            } else {
                logMessage(nodeType + " Received:\n" + message);
            }
        } catch (SocketTimeoutException e) {
            logMessage("Socket Timeout: Server took too long to respond. Sending next request.");
        } catch (IOException e) {
            String error = "Error: Could not read from " + nodeType.toLowerCase();
            throw new IOException(error);
        }

        return message;
    }

    // inherited method
    protected void sendMessageAndLog(String message, String messageType) throws IOException{
        try {
            dos.writeUTF(message);
            logMessage(messageType + " Sent:\n" + message);
        } catch (IOException e) {
            String nodeType;
            switch (messageType) {
                case "Request":
                    nodeType = "server";
                    break;
                case "Response":
                    nodeType = "client";
                    break;
                default:
                    nodeType = "node";
            }

            String error = "Error: Could not send the following " + messageType.toLowerCase() + "to" + nodeType + "\n" +
                    message;
            throw new IOException(error);
        }
    }

    /**
     * prints a log containing the timestamp and message on to std console
     * @param message message to be logged onto the screen
     */
    protected void logMessage(String message) {
        System.out.println("----------------------------------------------");
        System.out.println("Log TimeStamp: " + new Timestamp(System.currentTimeMillis()).toString());
        System.out.println(message);
        System.out.println("------------------------------------------------");
    }
}
