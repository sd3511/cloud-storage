package commonclasses.messages;

import lombok.Data;

@Data
public class AuthOkMessage implements Message {


    private String login;

    public AuthOkMessage(String login) {
        this.login = login;
    }

    @Override
    public MessageType getType() {
        return MessageType.AUTH_OK;
    }
}
