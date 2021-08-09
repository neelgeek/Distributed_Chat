package common;

import java.util.Map;

/**
 * A Runnable class that is intended to check the last heartbeat timestamp from a timeout map of
 * type {@link java.util.Map}<String,Long> and to determine if a client has become inActive based on
 * the timeout period specified.
 */
public class ClientStatusChecker implements Runnable {

  private Map<String, Long> timeoutMap;
  private StatusMaintainer maintainer;
  private Long timeoutSeconds;
  private Long currentTime;

  public ClientStatusChecker(Map<String, Long> timeoutMap, StatusMaintainer maintainer,
      Long timeoutSeconds) {
    this.timeoutMap = timeoutMap;
    this.maintainer = maintainer;
    this.timeoutSeconds = timeoutSeconds;
  }

  @Override
  public void run() {
    this.currentTime = System.currentTimeMillis();
    for (String clientID : timeoutMap.keySet()) {
      Long lastHB = timeoutMap.get(clientID);
      Long timeDifference = (currentTime - lastHB) / 1000;
      if (timeDifference > timeoutSeconds) {
        maintainer.setClientInactive(clientID);
      }
    }
  }
}
