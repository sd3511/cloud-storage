package commonclasses.messages;

import java.io.Serializable;

public interface Message extends Serializable {
    public MessageType getType();
}
