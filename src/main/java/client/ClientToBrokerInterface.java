package client;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface used by Broker to call Client methods
 * @param <T> interfaces for Message
 * @param <V> interfaces for Response
 */
public interface ClientToBrokerInterface<T, V> extends ClientToPeerInterface<T, V>, Remote {

    /**
     * Periodically called by Broker to make sure client is online
     * @param pingMessage the specific message to send to the client
     * @return the client's response to the ping
     * @throws RemoteException
     */
    V sendPing(T pingMessage) throws RemoteException;
}
