package commonclasses.authmessages;

import commonclasses.messages.Message;
import commonclasses.messages.MessageType;
import lombok.Data;

@Data
public class RegistrationMessage implements Message {
    private String login;
    private String password;
    public RegistrationMessage(String login, String password) {
        this.login = login;
        this.password = password;
    }

    @Override
    public MessageType getType() {

        return MessageType.REGISTRATION_REQUEST;
    }
}
