package commonclasses.warningmessages;

import commonclasses.messages.Message;
import commonclasses.messages.MessageType;

public class WarningMessageAuth implements Message {
    private String warningMessage;

    public WarningMessageAuth(String warningMessage) {
        this.warningMessage = warningMessage;
    }

    public String getWarningMessage() {
        return warningMessage;
    }

    @Override
    public MessageType getType() {
        return MessageType.WARNING_AUTH;
    }
}
