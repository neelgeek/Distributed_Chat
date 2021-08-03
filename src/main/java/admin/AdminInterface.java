package admin;

public interface AdminInterface extends java.rmi.Remote
{
    /**
     * Method to register first time users to the service
     * @param entity containing the info about the new user
     * @return BrokerInfoPayload informing the user about one of the active broker
     * @throws InterruptedException
     */
    BrokerInfoPayload registerNewUser(AbstractNetworkEntity entity) throws java.rmi.RemoteException, InterruptedException;

    /**
     * Method to register first time brokers to the service
     * @param serverIp
     * @param portNumber
     * @return boolean check informing the broker if it has been successfully registered or notifies of error if any
     */
    boolean registerNewBroker(String serverIp, int portNumber) throws java.rmi.RemoteException, InterruptedException;

}
