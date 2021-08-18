package protocol;

import java.io.Serializable;

/**
 * Any object that can be replicated using the Paxos protocol in the system, should implement this.
 */
public interface Replicable extends Serializable {

}
