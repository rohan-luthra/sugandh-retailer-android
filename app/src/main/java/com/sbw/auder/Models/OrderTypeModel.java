package com.sbw.auder.Models;

public class OrderTypeModel {

    public String id, name, fees, audioTimeSec;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFees() {
        return fees;
    }

    public void setFees(String fees) {
        this.fees = fees;
    }

    public String getAudioTimeSec() {
        return audioTimeSec;
    }

    public void setAudioTimeSec(String audioTimeSec) {
        this.audioTimeSec = audioTimeSec;
    }

    @Override
    public String toString() {
        return "OrderTypeModel{" + "id='" + id + '\'' + ", name='" + name + '\'' + ", fees='" + fees + '\'' + ", audioTimeSec='" + audioTimeSec + '\'' + '}';
    }
}
