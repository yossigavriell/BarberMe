package ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.example.barberme.R;


public class SplashScreenActivity extends AppCompatActivity
{
    private FirebaseAuth auth;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        auth = FirebaseAuth.getInstance();
        handler = new Handler();
    }

    @Override
    protected void onStart() {
        super.onStart();
        handler.postDelayed(() -> {
            if(auth.getCurrentUser() != null)
                startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
            else
                startActivity(new Intent(SplashScreenActivity.this, SignInUpActivity.class));
            finish();
        }, 3000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}