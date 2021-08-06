package protocol;

import java.io.Serializable;

/**
 * Represents a payload with the information of a broker
 */
public class BrokerInfoPayload extends AbstractNetworkEntity implements Serializable {

  public BrokerInfoPayload(String entityID, String HOST, int PORT, boolean isActive) {
    super(entityID, HOST, PORT, isActive);
  }

  public BrokerInfoPayload(String entityID, int PORT, boolean isActive) {
    super(entityID, "localhost", PORT, isActive);
  }
}
