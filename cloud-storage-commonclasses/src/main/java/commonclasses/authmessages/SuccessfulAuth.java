package commonclasses.authmessages;

import commonclasses.messages.Message;
import commonclasses.messages.MessageType;

public class SuccessfulAuth implements Message {
    @Override
    public MessageType getType() {
        return MessageType.SUCCESSFUL_AUTH;
    }
}
