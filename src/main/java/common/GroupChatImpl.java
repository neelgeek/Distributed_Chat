package common;

import java.util.ArrayList;
import java.util.Map;
import protocol.UserInfoPayload;

public class GroupChatImpl implements GroupChat {

  private String chatID;
  private Map<String, UserInfoPayload> participants;

  @Override
  public ArrayList<UserInfoPayload> getGroupParticipants() {
    return new ArrayList<>(participants.values());
  }

  /**
   * Adds a new participant user to the participants of group chat
   *
   * @param newUser
   */
  public void addParticipant(UserInfoPayload newUser) {
    participants.put(newUser.getEntityID(), newUser);
  }

  /**
   * Removes an existing user from the participants of group chat
   *
   * @param existingUser
   */
  public void removeParticipant(UserInfoPayload existingUser) {
    if (this.participants.containsKey(existingUser.getEntityID())) {
      this.participants.remove(existingUser.getEntityID());
    }
  }
}
