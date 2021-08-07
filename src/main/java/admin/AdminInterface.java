package admin;

public interface AdminInterface extends java.rmi.Remote
{
    /**
     * Method to register first time users to the service
     * @param UserInfoPayload containing the info about the new user
     * @return BrokerInfoPayload informing the user about one of the active brokers
     * @throws InterruptedException
     */
    BrokerInfoPayload registerNewUser(UserInfoPayload user) throws java.rmi.RemoteException, InterruptedException;

    /**
     * Method to register first time brokers to the service
     * @param BrokerInfoPayload containing the info about the new broker
     * @return boolean check informing the broker if it has been successfully registered
     */
    boolean registerNewBroker(BrokerInfoPayload broker) throws java.rmi.RemoteException, InterruptedException;

}