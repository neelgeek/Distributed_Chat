package client;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface used by one client to call methods of another client
 * @param <T> Interface of Messages
 * @param <V> Interface of Responses
 */
public interface ClientToPeerInterface<T, V> extends Remote {

    /**
     * sends a message to the client
     * @param message message to be sent
     * @return response of the client
     * @throws RemoteException
     */
    V sendMessage(T message) throws RemoteException;

    /**
     * gets the user profile of the client
     * @return response of the clients
     * @throws RemoteException
     */
    V getUserProfile() throws RemoteException;
}
