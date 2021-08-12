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
   * Creates a new Group chat with the given name set to the group name
   *
   * @param groupName The name to be set for the new Group
   * @param creator
   * @return
   * @throws RemoteException
   */
  GroupChat createGroupChat(String groupName, UserInfoPayload creator) throws RemoteException;

  /**
   * Adds a new user to a group chat
   *
   * @param joiningUser User that wants to join the group chat
   * @param groupChat   Group chat info
   * @return Returns the {@link GroupChat} object with the user in its participants. Returns Null if
   * the group does not exist
   * @throws RemoteException
   */
  GroupChat joinGroupChat(UserInfoPayload joiningUser, GroupChat groupChat) throws RemoteException;

  /**
   * Removes the user from the group if they are a participant of that group.
   *
   * @param leavingUser User that has to be removed from the group
   * @param groupChat   Group from which the user wants to leave
   * @return True if the user has left successfully, else False
   */
  boolean leaveGroupChat(UserInfoPayload leavingUser, GroupChat groupChat);


}
