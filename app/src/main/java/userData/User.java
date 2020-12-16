package userData;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class User implements Serializable {

    @NonNull
    private String id; // Document id

    String uID;
    String firstName;
    String lastName;
    String password;
    String email;
    String profilePicture;
    String gender;
    String birthday;
    String address;
    Double lat;
    Double lng;
    boolean isGettingNotifications;

    @ServerTimestamp
    private Date registerDate;  // update (also created) date - from Firebase

    public User() {} //for firebase database

    public User(String uID,String firstName,String lastName, String password, String email, String profilePicture, String gender, String birthday, String address, boolean isGettingNotifications, Double lat, Double lng) {

        this.uID=uID;
        this.firstName=firstName;
        this.lastName=lastName;
        this.password = password;
        this.email = email;
        this.profilePicture = profilePicture;
        this.gender = gender;
        this.birthday = birthday;
        this.address = address;
        this.isGettingNotifications = isGettingNotifications;
        this.lat = lat;
        this.lng = lng;
    }

    public <T extends User> T withId(String id) {
        this.id = id;
        return (T)this;
    }

    @Exclude
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) { this.id = id; }

    public Date getRegisterDate() {
        return registerDate;
    }

    public void setRegisterDate(Date registerDate) {
        this.registerDate = registerDate;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getuID() {
        return uID;
    }

    public void setuID(String uID) {
        this.uID = uID;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isGettingNotifications() {
        return isGettingNotifications;
    }

    public void setGettingNotifications(boolean gettingNotifications) {
        isGettingNotifications = gettingNotifications;
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
