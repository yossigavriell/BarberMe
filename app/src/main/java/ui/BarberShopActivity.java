package ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.barberme.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import adapter.BarberShopAdapter;
import adapter.PictureAdapter;
import adapter.ReviewAdapter;
import model.Consumer;
import model.DatabaseFetch;
import userData.BarberShop;
import userData.Review;
import userData.User;

public class BarberShopActivity extends AppCompatActivity {

    final int REQUEST_CALL_PERMISSION = 1;
    BarberShop barberShop;
    ImageView barberPicture;
    Button addReview;
    LinearLayout newReviewLayout;
    EditText newReviewText;
    Button submitNewReview;
    List<Review> reviews;
    ArrayList<Uri> pictures;
    RecyclerView picturesRecycler;
    RecyclerView reviewRecycler;
    TextView title;
    TextView type;
    RatingBar ratingBar;
    RatingBar barbershopRatingBar;
    FloatingActionButton phoneBtn, navigateBtn, messageBtn, websiteBtn;
    ImageView imageViewPic;
    StringBuilder barberAddress = new StringBuilder();
    ReviewAdapter reviewAdapter;
    DatabaseFetch databaseFetch = new DatabaseFetch();
    boolean showAddReviewLayout = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barber_shop);
        barberPicture = findViewById(R.id.barbershop_activity_image_view);
        picturesRecycler = findViewById(R.id.recycler_show_barber_pictures);
        reviewRecycler = findViewById(R.id.recycler_barber_reviews);
        addReview = findViewById(R.id.add_review_bt);
        newReviewLayout = findViewById(R.id.new_review_layout);
        newReviewText = findViewById(R.id.review_text_et);
        submitNewReview = findViewById(R.id.submit_review_bt);
        barbershopRatingBar = findViewById(R.id.rating_barber_shop);
        title = findViewById(R.id.barbershop_name_activity_barber_shop);
        type = findViewById(R.id.type_barbershop_activity_barber_shop);
        ratingBar = findViewById(R.id.rating);
        newReviewLayout.setVisibility(View.GONE);
        barberShop = (BarberShop) getIntent().getSerializableExtra("Barbershop");
        title.setText(barberShop.getName());
        barbershopRatingBar.setRating(barberShop.getRate());
        type.setText(barberShop.getType());
        Glide.with(this).load(barberShop.getImages().get(0)).into(barberPicture);
        pictures = new ArrayList<>();
        String uID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String barberUID = barberShop.getUserId();
        if(uID.equals(barberUID))
            addReview.setVisibility(View.GONE);
        else
            addReview.setVisibility(View.VISIBLE);
        for (String url : barberShop.getImages())
            pictures.add(Uri.parse(url));
        picturesRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        PictureAdapter pictureAdapter = new PictureAdapter(pictures);
        picturesRecycler.setAdapter(pictureAdapter);
        reviewRecycler.setLayoutManager(new LinearLayoutManager(this));
        reviews = barberShop.getReviews();
        if(reviews == null)
            reviews = new ArrayList<>();
        else
            Collections.reverse(reviews);
        reviewAdapter = new ReviewAdapter(reviews);
        reviewRecycler.setAdapter(reviewAdapter);

        phoneBtn = findViewById(R.id.image_btn_phone);
        navigateBtn = findViewById(R.id.image_btn_navigation);
        messageBtn = findViewById(R.id.image_btn_message);
        websiteBtn = findViewById(R.id.image_btn_internet);

        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                if(v<1.0f)
                    ratingBar.setRating(1.0f);
                else
                    ratingBar.setRating(v);
            }
        });

        addReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(FirebaseAuth.getInstance().getCurrentUser().getDisplayName() == null || FirebaseAuth.getInstance().getCurrentUser().getDisplayName().length() == 0) {
                    Toast.makeText(BarberShopActivity.this, getResources().getString(R.string.guest_error), Toast.LENGTH_LONG).show();
                }
                else {
                    showAddReviewLayout = !showAddReviewLayout;
                    newReviewLayout.setVisibility(showAddReviewLayout? View.VISIBLE : View.GONE);
                    ratingBar.setRating(5);
                }
            }
        });

        submitNewReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(newReviewText.getText().toString().isEmpty())
                {
                    Toast.makeText(BarberShopActivity.this, getResources().getString(R.string.empty_textboxes), Toast.LENGTH_SHORT).show();
                }
                else
                {
                    showAddReviewLayout = false;
                    uploadNewReview();
                }
            }
        });

        phoneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT >= 23) {
                    int hasCallPermission = checkSelfPermission(Manifest.permission.CALL_PHONE);
                    if (hasCallPermission == PackageManager.PERMISSION_GRANTED) {
                        makePhoneCall();
                    } else {
                        requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PERMISSION);
                    }
                } else {
                    makePhoneCall();
                }

            }
        });

        buildAddress();
        navigateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try
                {
                    // Launch Waze to look for Hawaii:
                    String url = "https://waze.com/ul?q=" + barberAddress.toString();
                    Intent intent = new Intent( Intent.ACTION_VIEW, Uri.parse( url ) );
                    startActivity( intent );
                }
                catch ( ActivityNotFoundException ex  )
                {
                    // If Waze is not installed, open it in Google Play:
                    Intent intent = new Intent( Intent.ACTION_VIEW, Uri.parse( "market://details?id=com.waze" ) );
                    startActivity(intent);
                }
            }
        });

        messageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
                smsIntent.addCategory(Intent.CATEGORY_DEFAULT);
                smsIntent.setType("vnd.android-dir/mms-sms");
                smsIntent.setData(Uri.parse("sms:" + barberShop.getPhoneNumber()));
                startActivity(smsIntent);
            }
        });

        websiteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("http://"+barberShop.getWebsite()); // missing 'http://' will cause crashed
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        pictureAdapter.setListener(new PictureAdapter.PictureListener() {
            @Override
            public void onClickPicture(int position, View view) {

                final AlertDialog.Builder builderDialog = new AlertDialog.Builder(BarberShopActivity.this);
                final View dialogView = getLayoutInflater().inflate(R.layout.pic_layout, null);

                imageViewPic=dialogView.findViewById(R.id.pic_dialog);
                Glide.with(BarberShopActivity.this).load(pictures.get(position)).into(imageViewPic);

                builderDialog.setView(dialogView);
                AlertDialog alertDialog = builderDialog.create();
                alertDialog.show();

            }
        });
    }

    private void uploadNewReview() {
        Consumer<User> consumer = new Consumer<User>() {
            @Override
            public void apply(User param) {
                User user = param;
                SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd HH:mm");
                Date date = new Date(System.currentTimeMillis());
                Review newReview = new Review(user, newReviewText.getText().toString(), formatter.format(date), (int)ratingBar.getRating());
                reviews.add(0, newReview);
                reviewAdapter.notifyDataSetChanged();
                updateBarberReviews();
                newReviewLayout.setVisibility(View.GONE);
                newReviewText.setText("");
            }
        };
        databaseFetch.findUserData(consumer, FirebaseAuth.getInstance().getCurrentUser().getUid());
    }

    private void updateBarberReviews() {
        List<Review> tempReviews = new ArrayList<>(reviews);
        Collections.reverse(tempReviews);
        barberShop.setReviews(tempReviews);
        FirebaseFirestore.getInstance().collection("shops").document(barberShop.getId())
                .set(barberShop, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(BarberShopActivity.this, BarberShopActivity.this.getResources().getString(R.string.new_review), Toast.LENGTH_SHORT).show();
                updateRating();
                sendPushNotification();
                barbershopRatingBar.setRating(barberShop.getRate());
            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });
    }

    private void updateRating() {
        //((Overall Rating * Total Rating) + new Rating) / (Total Rating + 1)
        if(barberShop.getRate() != 0) {
            float totalRating = barberShop.getReviews().size();
            float rating = ((barberShop.getRate() * totalRating) + ratingBar.getRating()) / (totalRating + 1);
            barberShop.setRate(rating);
        }
        else
            barberShop.setRate(ratingBar.getRating());
        FirebaseFirestore.getInstance().collection("shops").document(barberShop.getId())
                .set(barberShop, SetOptions.merge());
    }

    private void sendPushNotification() {
        JSONObject rootObject = new JSONObject();
        try {
            rootObject.put("to", "/topics/" + barberShop.getUserId());
            rootObject.put("data", new JSONObject().put("message", barberShop.getName() + ": " + getResources().getString(R.string.push_new_review)));
            String url = "https://fcm.googleapis.com/fcm/send";
            RequestQueue queue = Volley.newRequestQueue(this);
            StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    String API_TOKEN_KEY = "AAAAoF0h4pE:APA91bE62Z9KGOf-3mskaHcVldCJdQEDXVL53v2FsneoeC0impMaLQpT2cj3zT_SSo46uUiwWeSo2648CZfkZMwnzltuo51cceaZx1taERc3emWHxQnH1Gov5SsXytN5cOayvw1L5ZuI";
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type","application/json");
                    headers.put("Authorization","key="+API_TOKEN_KEY);
                    return headers;
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    return rootObject.toString().getBytes();
                }
            };
            queue.add(request);
            queue.start();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void buildAddress() {
        String city = barberShop.getCity();
        city.replaceAll(" ", "%20");
        String address = barberShop.getAddress();
        address.replaceAll(" ", "%20");
        barberAddress.append(city);
        barberAddress.append("%20");
        barberAddress.append(address);
    }

    private void makePhoneCall() {

        if (ContextCompat.checkSelfPermission(BarberShopActivity.this,Manifest.permission.CALL_PHONE)!=PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(BarberShopActivity.this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PERMISSION);
        }
        else {

            String dial = "tel:" +  barberShop.getPhoneNumber();
            startActivity(new Intent(Intent.ACTION_CALL,Uri.parse(dial)));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode== REQUEST_CALL_PERMISSION)
        {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                makePhoneCall();
            }
            else {
                Toast.makeText(this, this.getResources().getString(R.string.permission), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}