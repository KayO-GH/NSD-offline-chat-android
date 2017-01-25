package com.finalyear.networkservicediscovery.pojos;

import java.io.Serializable;

/**
 * Created by KayO on 29/12/2016.
 */
public class LocalInfo implements Serializable{
    private boolean firstTime;
    private String identity;
    private int infoID;

    public LocalInfo() {
    }

    public LocalInfo(boolean firstTime, String identity) {
        this.firstTime = firstTime;
        this.identity = identity;
    }

    public LocalInfo(boolean firstTime, String identity, int infoID) {
        this.firstTime = firstTime;
        this.identity = identity;
        this.infoID = infoID;
    }

    public boolean isFirstTime() {
        return firstTime;
    }

    public void setFirstTime(boolean firstTime) {
        this.firstTime = firstTime;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public int getInfoID() {
        return infoID;
    }

    public void setInfoID(int infoID) {
        this.infoID = infoID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LocalInfo localInfo = (LocalInfo) o;

        if (firstTime != localInfo.firstTime) return false;
        if (infoID != localInfo.infoID) return false;
        return identity != null ? identity.equals(localInfo.identity) : localInfo.identity == null;

    }

    @Override
    public int hashCode() {
        int result = (firstTime ? 1 : 0);
        result = 31 * result + (identity != null ? identity.hashCode() : 0);
        result = 31 * result + infoID;
        return result;
    }

    @Override
    public String toString() {
        return "LocalInfo{" +
                "firstTime=" + firstTime +
                ", identity='" + identity + '\'' +
                ", infoID=" + infoID +
                '}';
    }
}
