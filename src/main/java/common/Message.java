package common;

import java.io.Serializable;

/**
 * interface representing messages passed from one node to the other
 * as a parameter
 */
public interface Message extends Serializable {
    String getSenderRMIName();

    String getSenderUsername();

    String getSubject();

    String getTimeStamp();

    String getMessage();
}
