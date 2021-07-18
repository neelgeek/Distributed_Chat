package admin;

public interface AdminInterface extends java.rmi.Remote
{
    /**
     * Method to register first time users to the service
     * @param username
     * @return message informing the user if it has been successfully registered or notifies of error if any
     * @throws InterruptedException
     */
    String registerNewUser(String username) throws java.rmi.RemoteException, InterruptedException;

    /**
     * Method to register first time brokers to the service
     * @param serverIp
     * @param portNumber
     * @return message informing the broker if it has been successfully registered or notifies of error if any
     */
    String registerNewBroker(String serverIp, int portNumber) throws java.rmi.RemoteException, InterruptedException;

    /**
     * Sends a given message to all the available brokers connected with the AdminServer
     * @param message
     */
    void notifyAllBrokers(String message) throws java.rmi.RemoteException, InterruptedException;

    /**
     * Deletes the inactive broker from its records
     * @param brokerID
     */
    void deleteInactiveBroker(String brokerID) throws java.rmi.RemoteException, InterruptedException;
}
