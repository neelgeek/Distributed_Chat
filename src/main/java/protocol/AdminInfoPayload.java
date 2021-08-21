package protocol;

public class AdminInfoPayload extends AbstractNetworkEntity {

  private Integer PRIORITY;

  public AdminInfoPayload(String entityID, String HOST, int PORT, boolean isActive,
      Integer PRIORITY) {
    super(entityID, HOST, PORT, isActive);
    this.PRIORITY = PRIORITY;
  }

  public Integer getPRIORITY() {
    return PRIORITY;
  }
}
