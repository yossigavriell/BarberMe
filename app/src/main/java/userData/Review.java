package userData;

import java.io.Serializable;
import java.util.Date;

public class Review implements Serializable {
    private User user;
    private String reviewText;
    private String date;
    private int rate;

    public Review(User user, String reviewText, String date, int rate) {
        this.user = user;
        this.reviewText = reviewText;
        this.date = date;
        this.rate = rate;
    }

    public Review() {} //for firebase database

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getReviewText() {
        return reviewText;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }
}
