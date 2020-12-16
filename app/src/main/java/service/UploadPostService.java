package service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.barberme.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import ui.SplashScreenActivity;
import userData.BarberShop;

public class UploadPostService extends Service
{
    private static final int ID = 1;
    private StorageReference imagesRef;
    private static boolean isRunning = false;
    private Double lat = 0.0;
    private Double lng = 0.0;
    private String userAddress;
    BarberShop data;
    final String geoApi = "http://open.mapquestapi.com/geocoding/v1/address?key=zdmIpWnC4qo7HygG9FDevXQUvQexxm3M&location=";

    @Override
    public void onCreate() {
        isRunning = true;
        super.onCreate();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        imagesRef = storage.getReference().child("images");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(ID, createNotification());

        ArrayList<Uri> images = intent.getParcelableArrayListExtra("images");
        String userId = intent.getStringExtra("userId");
        String title = intent.getStringExtra("title");
        String city = intent.getStringExtra("city");
        String address = intent.getStringExtra("address");
        String userName = intent.getStringExtra("userName");
        String website = intent.getStringExtra("website");
        String phoneNumber = intent.getStringExtra("phoneNumber");
        float rate = intent.getFloatExtra("rate", 0);
        String type = intent.getStringExtra("type");

        userAddress = address + "," + city;

        data = new BarberShop(title, city, address, phoneNumber, null, userId, userName, website, rate, type, lat, lng);

        if(images == null || images.size() == 0) {
            postAd();
            return Service.START_NOT_STICKY;
        }

        // Else upload photos first
        final List<UrlData> urlsList =  Collections.synchronizedList(new LinkedList<>());
        for(int i=0; i<images.size(); i++) {
            final StorageReference photoRef = imagesRef.child(String.valueOf(System.currentTimeMillis()));
            final UrlData url = new UrlData(i);
            UploadTask uploadTask = photoRef.putFile(images.get(i));
            //upload the img + perform a task of getting the download url from the cloud
            uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    task.getException().printStackTrace();
                    return null;
                }
                return photoRef.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String downloadUrl = task.getResult().toString();
                    url.url = downloadUrl;
                    urlsList.add(url);
                    if(urlsList.size() == images.size()) {
                        // all images were uploaded..

                        // sort the list by original elements' position
                        Collections.sort(urlsList, (e1, e2) -> {
                            return Integer.compare(e1.position, e2.position);
                        });

                        data.setImages(mappedURLs(urlsList));
                        postAd();
                    }
                } else {
                    showMessageAndFinish(getApplicationContext().getResources().getString(R.string.upload_data_error));
                }
            });
        }
        return Service.START_NOT_STICKY;
    }

    public static boolean isRunning() {
        return isRunning;
    }

    public List<String> mappedURLs(List<UrlData> urlsList) {
        List<String> result = new ArrayList<>();
        for(UrlData data : urlsList)
            result.add(data.url);

        return result;
    }

    private void postAd() {
        String withoutspaces = "";
        for (int i = 0; i < userAddress.length(); i++) {
            if (userAddress.charAt(i) != ' ')
                withoutspaces += userAddress.charAt(i);
            else
                withoutspaces += "+";
        }
        String getUrl = geoApi + withoutspaces + ",Israel";
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, getUrl, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("results");
                            JSONObject jsonObject = jsonArray.getJSONObject(0);
                            JSONArray jsonArray1 = jsonObject.getJSONArray("locations");
                            JSONObject jsonObject1 = jsonArray1.getJSONObject(0);
                            JSONObject jsonObject2 = jsonObject1.getJSONObject("latLng");
                            lat = jsonObject2.getDouble("lat");
                            lng = jsonObject2.getDouble("lng");
                            data.setLat(lat);
                            data.setLng(lng);
                            FirebaseFirestore.getInstance().collection("shops")
                                    .add(data)
                                    .addOnSuccessListener(docRef -> {
                                        stopSelf();
                                    })
                                    .addOnFailureListener(ex -> {
                                        ex.printStackTrace();
                                        showMessageAndFinish(getApplicationContext().getResources().getString(R.string.upload_data_error));
                                    });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        queue.add(request);
        queue.start();
    }

    private void showMessageAndFinish(String msg) {
        try {
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            stopSelf();
        } catch(Throwable t) {
            t.printStackTrace();
        }
    }

    private Notification createNotification() {
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = getPackageName() + "UploadNotifID";
        if(Build.VERSION.SDK_INT >= 26) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    "Important Notification", NotificationManager.IMPORTANCE_HIGH);

            // Configure the notification channel.
            notificationChannel.setDescription("Important Notification");
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this,
                NOTIFICATION_CHANNEL_ID);

        // Create notification action intent..
        Intent notificationIntent = new Intent(getApplicationContext(), SplashScreenActivity.class);
        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);

        notificationBuilder.setAutoCancel(false)
                .setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getApplicationContext().getResources().getString(R.string.upload_title))
                .setContentText(getApplicationContext().getResources().getString(R.string.upload_message))
                .setContentIntent(pi);

        return notificationBuilder.build();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    public static class UrlData {
        public String url;
        public int position;

        public UrlData(int pos) {
            position = pos;
        }
    }
}
