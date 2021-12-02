package commonclasses.warningmessages;

import commonclasses.messages.Message;
import commonclasses.messages.MessageType;

public class WarningMessage implements Message {
    private String text;

    public WarningMessage(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    @Override
    public MessageType getType() {
        return MessageType.WARNING_MESSAGE;
    }
}
