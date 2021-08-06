package protocol;

public abstract class AbstractNetworkEntity {

  private String entityID;
  private String HOST;
  private int PORT;
  private boolean isActive;

  public AbstractNetworkEntity(String entityID, String HOST, int PORT, boolean isActive) {
    this.entityID = entityID;
    this.HOST = HOST;
    this.PORT = PORT;
    this.isActive = isActive;
  }

  public AbstractNetworkEntity(String entityID, boolean isActive) {
    this.entityID = entityID;
    this.isActive = isActive;
  }

  public String getEntityID() {
    return entityID;
  }

  public String getHOST() {
    return HOST;
  }

  public int getPORT() {
    return PORT;
  }

  public boolean isActive() {
    return isActive;
  }

  public void setActive(boolean active) {
    isActive = active;
  }
}
