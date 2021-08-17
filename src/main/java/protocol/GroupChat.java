package protocol;

import java.io.Serializable;
import java.util.ArrayList;

public interface GroupChat extends Serializable {

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
