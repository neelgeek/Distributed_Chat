package common;

import java.util.ArrayList;
import protocol.UserInfoPayload;

public interface GroupChat {

  /**
   * Returns the List of active participants in the group
   *
   * @return
   */
  ArrayList<UserInfoPayload> getGroupParticipants();

//  /**
//   * Returns the ID for the group chat
//   *
//   * @return
//   */
//  String getGroupChatID();
}