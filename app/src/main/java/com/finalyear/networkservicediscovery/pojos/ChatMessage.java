package com.finalyear.networkservicediscovery.pojos;

import java.io.Serializable;

/**
 * Created by KayO on 23/11/2016.
 */
public class ChatMessage implements Serializable {
    private boolean received;//used to determine whether messageContent should be on the left or the right in a chat
    private String messageContent;
    //newer fields
    private String sender = null, recipient = null, timeSent = null, timeDelivered = null;
    private boolean sent = false;
    private long messageID;

    public ChatMessage(boolean received, String messageContent) {
        this.received = received;
        this.messageContent = messageContent;
    }

    public ChatMessage(long messageID, String messageContent, String sender, String recipient, String timeSent, String timeDelivered, boolean sent, boolean received) {
        this.received = received;
        this.messageContent = messageContent;
        this.sender = sender;
        this.recipient = recipient;
        this.timeSent = timeSent;
        this.timeDelivered = timeDelivered;
        this.sent = sent;
        this.messageID = messageID;
    }


    public boolean isReceived() {

        return received;
    }

    public void setReceived(boolean received) {
        this.received = received;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public boolean isSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getTimeSent() {
        return timeSent;
    }

    public void setTimeSent(String timeSent) {
        this.timeSent = timeSent;
    }

    public String getTimeDelivered() {
        return timeDelivered;
    }

    public void setTimeDelivered(String timeDelivered) {
        this.timeDelivered = timeDelivered;
    }

    public long getMessageID() {
        return messageID;
    }

    public void setMessageID(long messageID) {
        this.messageID = messageID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChatMessage that = (ChatMessage) o;

        if (received != that.received) return false;
        if (sent != that.sent) return false;
        if (messageID != that.messageID) return false;
        if (messageContent != null ? !messageContent.equals(that.messageContent) : that.messageContent != null)
            return false;
        if (sender != null ? !sender.equals(that.sender) : that.sender != null) return false;
        if (recipient != null ? !recipient.equals(that.recipient) : that.recipient != null)
            return false;
        if (timeSent != null ? !timeSent.equals(that.timeSent) : that.timeSent != null)
            return false;
        return timeDelivered != null ? timeDelivered.equals(that.timeDelivered) : that.timeDelivered == null;

    }

    @Override
    public int hashCode() {
        int result = (received ? 1 : 0);
        result = 31 * result + (messageContent != null ? messageContent.hashCode() : 0);
        result = 31 * result + (sender != null ? sender.hashCode() : 0);
        result = 31 * result + (recipient != null ? recipient.hashCode() : 0);
        result = 31 * result + (timeSent != null ? timeSent.hashCode() : 0);
        result = 31 * result + (timeDelivered != null ? timeDelivered.hashCode() : 0);
        result = 31 * result + (sent ? 1 : 0);
        result = 31 * result + (int) (messageID ^ (messageID >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "ChatMessage{" +
                "received=" + received +
                ", messageContent='" + messageContent + '\'' +
                ", sender='" + sender + '\'' +
                ", recipient='" + recipient + '\'' +
                ", timeSent='" + timeSent + '\'' +
                ", timeDelivered='" + timeDelivered + '\'' +
                ", sent=" + sent +
                ", messageID=" + messageID +
                '}';
    }
}
