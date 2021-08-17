package protocol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GroupChatInfoPayload implements GroupChat {

  private String groupID = UUID.randomUUID().toString();
  private String groupName;
  private Map<String, UserInfoPayload> participants = new HashMap<>();

  public GroupChatInfoPayload() {
  }

  public GroupChatInfoPayload(String groupName) {
    this.groupName = groupName;
  }

  public String getGroupID() {
    return groupID;
  }

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
