package broker;

import common.GroupChat;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import protocol.BrokerInfoPayload;
import protocol.UserInfoPayload;

/**
 * Defines the interface for implementation of a Broker in the cluster.
 */
public interface Broker extends Remote {

  /**
   * Sends an update about another broker that has become active or inactive. This method is used by
   * the admin server to sends broker updates to active brokers.
   *
   * @param brokerInfo Payload holding broker info
   */
  void sendBrokerUpdate(BrokerInfoPayload brokerInfo) throws RemoteException;

  /**
   * Registers a User into the Broker's user record
   *
   * @param userInfoPayload payload containing the user's information
   * @return True if user is successfully registered, else False
   * @throws RemoteException
   */
  boolean registerUser(UserInfoPayload userInfoPayload) throws RemoteException;

  /**
   * Fetches the list of active users in the system.
   *
   * @return List of {@link UserInfoPayload}, each representing an active user.
   * @throws RemoteException
   */
  List<UserInfoPayload> getActiveUsers() throws RemoteException;

  /**
   * Fetched the list of all group chats in the system.
   *
   * @return
   * @throws RemoteException
   */
  List<GroupChat> getGroupChats() throws RemoteException;

  /**
   * Adds a new user to a group chat
   *
   * @param userInfo  User that wants to join the group chat
   * @param groupChat Group chat info
   * @return
   * @throws RemoteException
   */
  boolean joinGroupChat(UserInfoPayload userInfo, GroupChat groupChat) throws RemoteException;
}
