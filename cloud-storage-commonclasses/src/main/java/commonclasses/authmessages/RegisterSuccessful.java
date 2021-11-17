package commonclasses.authmessages;

import commonclasses.messages.Message;
import commonclasses.messages.MessageType;

public class RegisterSuccessful implements Message {
    @Override
    public MessageType getType() {
        return MessageType.REGISTRATION_SUCCESSFUL;
    }
}
