package protocol;

import java.io.Serializable;

/**
 * Represents a payload with the information of a broker
 */
public class BrokerInfoPayload implements Serializable {

  private String brokerID;
  private String HOST;
  private String PORT;
  private boolean isActive;

  public BrokerInfoPayload(String brokerID, String HOST, String PORT, boolean isActive) {
    this.brokerID = brokerID;
    this.HOST = HOST;
    this.PORT = PORT;
    this.isActive = isActive;
  }

  public BrokerInfoPayload(String brokerID, boolean isActive) {
    this.brokerID = brokerID;
    this.isActive = isActive;
  }

  public String getBrokerID() {
    return brokerID;
  }

  public String getHOST() {
    return HOST;
  }

  public String getPORT() {
    return PORT;
  }

  public boolean isActive() {
    return isActive;
  }

  public void setActive(boolean active) {
    isActive = active;
  }
}
