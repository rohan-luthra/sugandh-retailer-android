package com.sbw.auder.Models;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;

public class OrderModel implements Parcelable {

    public String id;
    public String ret_id;

    public String getDistName() {
        return distName;
    }

    public void setDistName(String distName) {
        this.distName = distName;
    }

    public String distUser_id;
    public String timestamp;
    public String audioRec;
    public String videoIntroReceipt;
    public String recText;
    public String distName;

    public String getSkippedImage() {
        return skippedImage;
    }

    public void setSkippedImage(String skippedImage) {
        this.skippedImage = skippedImage;
    }

    public String skippedImage;

    public String getUserImageUrl() {
        return userImageUrl;
    }

    public void setUserImageUrl(String userImageUrl) {
        this.userImageUrl = userImageUrl;
    }

    public String userImageUrl;

    public String getRecText() {
        return recText;
    }

    public void setRecText(String recText) {
        this.recText = recText;
    }

    public String getSchTimestamp() {
        return schTimestamp;
    }

    public void setSchTimestamp(String schTimestamp) {
        this.schTimestamp = schTimestamp;
    }

    public String schTimestamp;

    public String getVideoIntroReceipt() {
        return videoIntroReceipt;
    }

    public void setVideoIntroReceipt(String videoIntroReceipt) {
        this.videoIntroReceipt = videoIntroReceipt;
    }

    public OrderModel() {
    }

    @Override
    public String toString() {
        return "OrderModel{" +
                "id='" + id + '\'' +
                ", ret_id='" + ret_id + '\'' +
                ", distUser_id='" + distUser_id + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", audioRec='" + audioRec + '\'' +
                ", videoIntroReceipt='" + videoIntroReceipt + '\'' +
                ", recText='" + recText + '\'' +
                ", schTimestamp='" + schTimestamp + '\'' +
                ", statusJsonStr='" + statusJsonStr + '\'' +
                ", imageRec='" + imageRec + '\'' +
                ", dist_id='" + dist_id + '\'' +
                ", status=" + status +
                '}';
    }

    public String getStatusJsonStr() {

        return statusJsonStr;
    }

    public void setStatusJsonStr(String statusJsonStr) {
        this.statusJsonStr = statusJsonStr;
    }

    public String statusJsonStr;

    protected OrderModel(Parcel in) {
        id = in.readString();
        ret_id = in.readString();
        distUser_id = in.readString();
        timestamp = in.readString();
        audioRec = in.readString();
        videoIntroReceipt = in.readString();
        imageRec = in.readString();
        dist_id = in.readString();
        statusJsonStr = in.readString();
        distName = in.readString();
        skippedImage = in.readString();
    }

    public static final Creator<OrderModel> CREATOR = new Creator<OrderModel>() {
        @Override
        public OrderModel createFromParcel(Parcel in) {
            return new OrderModel(in);
        }

        @Override
        public OrderModel[] newArray(int size) {
            return new OrderModel[size];
        }
    };

    public String getAudioRec() {
        return audioRec;
    }

    public void setAudioRec(String audioRec) {
        this.audioRec = audioRec;
    }

    public String getImageRec() {
        return imageRec;
    }

    public void setImageRec(String imageRec) {
        this.imageRec = imageRec;
    }

    String imageRec;

    public String getDistUser_id() {
        return distUser_id;
    }

    public void setDistUser_id(String distUser_id) {
        this.distUser_id = distUser_id;
    }

    String dist_id;
    JSONArray status;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRet_id() {
        return ret_id;
    }

    public void setRet_id(String ret_id) {
        this.ret_id = ret_id;
    }

    public String getDist_id() {
        return dist_id;
    }

    public void setDist_id(String dist_id) {
        this.dist_id = dist_id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public JSONArray getStatus() {
        return status;
    }

    public void setStatus(JSONArray status) {
        this.status = status;
    }


    /**
     * Describe the kinds of special objects contained in this Parcelable
     * instance's marshaled representation. For example, if the object will
     * include a file descriptor in the output of {@link #writeToParcel(Parcel, int)},
     * the return value of this method must include the
     * {@link #CONTENTS_FILE_DESCRIPTOR} bit.
     *
     * @return a bitmask indicating the set of special object types marshaled
     * by this Parcelable object instance.
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Flatten this object in to a Parcel.
     *
     * @param dest  The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written.
     *              May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(ret_id);
        dest.writeString(distUser_id);
        dest.writeString(timestamp);
        dest.writeString(audioRec);
        dest.writeString(videoIntroReceipt);
        dest.writeString(imageRec);
        dest.writeString(dist_id);
        dest.writeString(statusJsonStr);
        dest.writeString(distName);
        dest.writeString(skippedImage);
    }
}
