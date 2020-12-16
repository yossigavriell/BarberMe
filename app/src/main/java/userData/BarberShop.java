package userData;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@IgnoreExtraProperties  // For Firebase deserialization
public class BarberShop implements Serializable {

    @NonNull
    private String id; // Document id

    private String name;
    private String city;
    private String address;
    private String phoneNumber;
    private List<String> images;
    private String userId;
    private String userName;
    private String website;
    private List<Review> reviews;
    private float rate;
    private String type;
    private Double lat;
    private Double lng;

    @ServerTimestamp
    private Date updateDate;  // update (also created) date - from Firebase

    public BarberShop() {} //for firebase database

    public BarberShop(String name, String city, String address, String phoneNumber, List<String> images, String userId, String userName, String website, float rate, String type, Double lat, Double lng) {
        this.name = name;
        this.city = city;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.images = images;
        this.userId = userId;
        this.userName = userName;
        this.website = website;
        this.rate = rate;
        this.type = type;
        this.lat = lat;
        this.lng = lng;
    }

    public <T extends BarberShop> T withId(String id) {
        this.id = id;
        return (T)this;
    }

    @Exclude
    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public String getWebsite() { return website; }

    public void setWebsite(String website) { this.website = website; }

    public float getRate() {
        return rate;
    }

    public void setRate(float rate) {
        this.rate = rate;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }
}
