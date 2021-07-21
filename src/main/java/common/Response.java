package common;

import java.io.Serializable;

/**
 * Interface representing the response sent from one node to another
 * as a return value
 */
public interface Response extends Serializable {
    String getHeader();

    String getResponse();
}
