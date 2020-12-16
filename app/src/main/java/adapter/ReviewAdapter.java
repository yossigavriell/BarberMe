package adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.barberme.R;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.List;

import userData.Review;
import userData.User;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    List<Review> reviews;
    Context context;

    public ReviewAdapter(List<Review> reviews) {
        this.reviews = reviews;
    }

    class ReviewViewHolder extends RecyclerView.ViewHolder {

        ImageView picture;
        TextView name;
        TextView reviewText;
        TextView date;
        RatingBar ratingBar;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            picture = itemView.findViewById(R.id.user_review_pic);
            name = itemView.findViewById(R.id.user_name_reviews);
            reviewText = itemView.findViewById(R.id.user_text_reviews);
            date = itemView.findViewById(R.id.date_reviews);
            ratingBar = itemView.findViewById(R.id.rating_cardview);
        }
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.review_cardview, parent, false);
        ReviewViewHolder reviewViewHolder = new ReviewViewHolder(view);
        context = parent.getContext();
        return reviewViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        User user = reviews.get(position).getUser();
        holder.name.setText(user.getFirstName() + " " + user.getLastName());
        holder.reviewText.setText(reviews.get(position).getReviewText());
        holder.date.setText(reviews.get(position).getDate());
        holder.ratingBar.setRating(reviews.get(position).getRate());
        Glide.with(context).load(user.getProfilePicture()).into(holder.picture);
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

}
