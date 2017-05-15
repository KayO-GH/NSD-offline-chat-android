package com.finalyear.networkservicediscovery.pojos;

import java.io.Serializable;
import java.net.Inet4Address;
import java.net.Inet4Address;
import java.util.Arrays;

/**
 * Created by KayO on 28/12/2016.
 */
public class Contact implements Serializable {
    private String name = null;
    private byte[] image;
    private boolean online = false;
    private String lastMessage;
    private boolean isSenderOfLastMessage;//will be needed later
    private String phoneNumber = null;
    private Inet4Address ipAddress;
    private int port;

    public Contact() {
    }

    public Contact(String name, byte[] image, String lastMessage, String phoneNumber) {
        this.name = name;
        this.image = image;
        //this.online = online;
        this.lastMessage = lastMessage;
        //this.isSenderOfLastMessage = isSenderOfLastMessage;
        this.phoneNumber = phoneNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public boolean isSenderOfLastMessage() {
        return isSenderOfLastMessage;
    }

    public void setSenderOfLastMessage(boolean senderOfLastMessage) {
        isSenderOfLastMessage = senderOfLastMessage;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Inet4Address getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(Inet4Address ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Contact contact = (Contact) o;

        if (online != contact.online) return false;
        if (isSenderOfLastMessage != contact.isSenderOfLastMessage) return false;
        if (name != null ? !name.equals(contact.name) : contact.name != null) return false;
        if (!Arrays.equals(image, contact.image)) return false;
        if (lastMessage != null ? !lastMessage.equals(contact.lastMessage) : contact.lastMessage != null)
            return false;
        return phoneNumber != null ? phoneNumber.equals(contact.phoneNumber) : contact.phoneNumber == null;

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + Arrays.hashCode(image);
        result = 31 * result + (online ? 1 : 0);
        result = 31 * result + (lastMessage != null ? lastMessage.hashCode() : 0);
        result = 31 * result + (isSenderOfLastMessage ? 1 : 0);
        result = 31 * result + (phoneNumber != null ? phoneNumber.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Contact{" +
                "name='" + name + '\'' +
                ", image=" + Arrays.toString(image) +
                ", online=" + online +
                ", lastMessage='" + lastMessage + '\'' +
                ", isSenderOfLastMessage=" + isSenderOfLastMessage +
                ", phoneNumber='" + phoneNumber + '\'' +
                '}';
    }
}
