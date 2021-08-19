package protocol;

import java.io.Serializable;

public class PaxMessage implements Serializable {

  private Integer proposalID;
  private Replicable proposedValue;
  private PaxActions type;
  private Integer acceptedID;
  private Replicable acceptedValue;

  public PaxMessage(Integer proposalID, PaxActions type, Integer acceptedID,
      Replicable acceptedValue) {
    this.proposalID = proposalID;
    this.type = type;
    this.acceptedID = acceptedID;
    this.acceptedValue = acceptedValue;
  }

  public PaxMessage(Integer proposalID, Replicable proposedValue, PaxActions type) {
    this.proposalID = proposalID;
    this.proposedValue = proposedValue;
    this.type = type;
  }

  public Integer getProposalID() {
    return proposalID;
  }

  public PaxActions getType() {
    return type;
  }

  public Integer getAcceptedID() {
    return acceptedID;
  }

  public Replicable getAcceptedValue() {
    return acceptedValue;
  }

  public Replicable getProposedValue() {
    return proposedValue;
  }
}
