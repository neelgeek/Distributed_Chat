package common;

import java.io.Serializable;

/**
 * interface representing messages passed from one node to the other
 * as a parameter
 */
public class Message implements Serializable {
    private String senderUsername;
    private String subject;
    private String message;

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
