package client;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Interface used by Broker to call Client methods
 * @param <T> interfaces for Message
 * @param <V> interfaces for Response
 */
public interface ClientToBrokerInterface<T, V> extends Remote {

    /**
     * Periodically called by Broker to make sure client is online
     * @param userUpdateInfo information of all the users and their active status
     * @throws RemoteException
     */
    void sendUserUpdateInfo(List<T> userUpdateInfo) throws RemoteException;
}
