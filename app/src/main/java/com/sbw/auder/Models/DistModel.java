package com.sbw.auder.Models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class DistModel implements Parcelable {

    public String id;
    public String name;
    String timestamp;
    String backgroundUrl;
    String intoVideoUrl;

    public String getSkippedImage() {
        return skippedImage;
    }

    public void setSkippedImage(String skippedImage) {
        this.skippedImage = skippedImage;
    }

    String skippedImage;
    int priority;


    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public OrderModel getLatestOrder() {
        return latestOrder;
    }

    public void setLatestOrder(OrderModel latestOrder) {
        this.latestOrder = latestOrder;
    }

    OrderModel latestOrder;

    public String getDistInfo() {
        return distInfo;
    }

    public void setDistInfo(String distInfo) {
        this.distInfo = distInfo;
    }

    String distInfo;

    public DistModel() {
    }

    protected DistModel(Parcel in) {
        id = in.readString();
        name = in.readString();
        timestamp = in.readString();
        backgroundUrl = in.readString();
        intoVideoUrl = in.readString();
        profileUrl = in.readString();
        imageUrls = in.createStringArrayList();
        location = in.readString();
        distInfo = in.readString();
        skippedImage = in.readString();
    }

    public static final Creator<DistModel> CREATOR = new Creator<DistModel>() {
        @Override
        public DistModel createFromParcel(Parcel in) {
            return new DistModel(in);
        }

        @Override
        public DistModel[] newArray(int size) {
            return new DistModel[size];
        }
    };

    public String getBackgroundUrl() {
        return backgroundUrl;
    }

    public void setBackgroundUrl(String backgroundUrl) {
        this.backgroundUrl = backgroundUrl;
    }

    public String getIntoVideoUrl() {
        return intoVideoUrl;
    }

    public void setIntoVideoUrl(String intoVideoUrl) {
        this.intoVideoUrl = intoVideoUrl;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    String profileUrl;
    ArrayList<String> imageUrls = new ArrayList<>();

    public ArrayList<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(ArrayList<String> imageUrls) {
        this.imageUrls = imageUrls;
    }



    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String location ;

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

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
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
        dest.writeString(name);
        dest.writeString(timestamp);
        dest.writeString(backgroundUrl);
        dest.writeString(intoVideoUrl);
        dest.writeString(profileUrl);
        dest.writeStringList(imageUrls);
        dest.writeString(location);
        dest.writeString(distInfo);
        dest.writeString(skippedImage);
    }

    public void addImageUrl(String url){
        this.imageUrls.add(url);

    }

    /**
     * Returns a string representation of the object. In general, the
     * {@code toString} method returns a string that
     * "textually represents" this object. The result should
     * be a concise but informative representation that is easy for a
     * person to read.
     * It is recommended that all subclasses override this method.
     * <p>
     * The {@code toString} method for class {@code Object}
     * returns a string consisting of the name of the class of which the
     * object is an instance, the at-sign character `{@code @}', and
     * the unsigned hexadecimal representation of the hash code of the
     * object. In other words, this method returns a string equal to the
     * value of:
     * <blockquote>
     * <pre>
     * getClass().getName() + '@' + Integer.toHexString(hashCode())
     * </pre></blockquote>
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return "DistModel{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", backgroundUrl='" + backgroundUrl + '\'' +
                ", intoVideoUrl='" + intoVideoUrl + '\'' +
                ", priority=" + priority +
                ", latestOrder=" + latestOrder +
                ", distInfo='" + distInfo + '\'' +
                ", profileUrl='" + profileUrl + '\'' +
                ", imageUrls=" + imageUrls +
                ", location='" + location + '\'' +
                '}';
    }
}
