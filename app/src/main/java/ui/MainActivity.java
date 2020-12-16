package ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceScreen;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import com.bumptech.glide.Glide;
import com.example.barberme.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import dialog.LogoutDialog;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    final String TAG = "MainActivity";
    final String ANNONYMOUS_PROFILE = "https://firebasestorage.googleapis.com/v0/b/barberme-83e8b.appspot.com/o/profiles%2FprofilePicture.png?alt=media&token=996a8ccb-7f65-43da-86ad-4619a9580fa5";
    final int SETTING_REQUEST=1;
    Toolbar toolbar;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseAuth.AuthStateListener firebaseListener;
    static Boolean isGuest = false;
    ImageView home;
    ImageView search;
    boolean searchMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        drawerLayout = findViewById(R.id.drawer_layout);
        toolbar=findViewById(R.id.toolbar);
        navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
        setSupportActionBar(toolbar);
        this.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.menu.search_menu);
        getSupportActionBar().setElevation(0);
        View actionBar = getSupportActionBar().getCustomView();
        home = actionBar.findViewById(R.id.home_button);
        search = actionBar.findViewById(R.id.search_button);
        home.setOnClickListener(view -> drawerLayout.openDrawer(GravityCompat.START));
        search.setOnClickListener(view -> {
            AllBarberShopsFragment myFragment = (AllBarberShopsFragment)getSupportFragmentManager().findFragmentByTag(TAG);
            searchMode = !searchMode;
            myFragment.showHideSearch(searchMode);
        });
        View headerView = navigationView.getHeaderView(0);
        TextView welcomeTV = headerView.findViewById(R.id.navigation_header_tv);
        ImageView profileIV = headerView.findViewById(R.id.profile_picture_header);
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        String name;
        if(currentUser.getDisplayName() == null || currentUser.getDisplayName().length() == 0) {
            name = getResources().getString(R.string.guest);
            isGuest = true;
            Glide.with(this).load(ANNONYMOUS_PROFILE).into(profileIV);
        }
        else {
            name = currentUser.getDisplayName();
            isGuest = false;
            Uri profilePicture = currentUser.getPhotoUrl();
            Glide.with(this).load(profilePicture).into(profileIV);
        }
        welcomeTV.setText(name);
        welcomeTV.setMovementMethod(LinkMovementMethod.getInstance());
        getSupportFragmentManager().beginTransaction().add(R.id.container, new AllBarberShopsFragment(), TAG).commit();

        firebaseListener = firebaseAuth -> {
            if(firebaseAuth.getCurrentUser() == null) {
                Intent intent = new Intent(MainActivity.this, SignInUpActivity.class);
                startActivity(intent);
                finish();
            }
        };
        BroadcastReceiver updateProfileReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Uri profilePicture = currentUser.getPhotoUrl();
                Glide.with(MainActivity.this).load(profilePicture).into(profileIV);
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(
                updateProfileReceiver, new IntentFilter("profilePictureChanged"));

        BroadcastReceiver usernameReceiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String username=currentUser.getDisplayName();
                welcomeTV.setText(username);

            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(
                usernameReceiver,new IntentFilter("usernameChange"));
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        unCheckAllMenuItems(navigationView.getMenu());
        item.setChecked(true);
        drawerLayout.closeDrawers();
        switch (item.getItemId())
        {
            case R.id.all_barbershops:
                getSupportFragmentManager().beginTransaction().replace(R.id.container, new AllBarberShopsFragment(), TAG).addToBackStack(null).commit();
                search.setVisibility(View.VISIBLE);
                searchMode = false;
                break;
            case R.id.my_barbershops:
                if(isGuest != true) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, new MyBarberShopsFragment(), TAG).addToBackStack(null).commit();
                    search.setVisibility(View.GONE);
                }
                else
                    Toast.makeText(this, getResources().getString(R.string.guest_error), Toast.LENGTH_LONG).show();
                break;
            case R.id.settings:
                if(isGuest != true) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, new SettingsFragment(), TAG).addToBackStack(null).commit();
                    search.setVisibility(View.GONE);
                }
                else
                    Toast.makeText(this, getResources().getString(R.string.guest_error), Toast.LENGTH_LONG).show();
                break;
            case R.id.Logout:
                logout();
                break;
        }
        return false;
    }


    private void unCheckAllMenuItems(@NonNull final Menu menu) {
        int size = menu.size();
        for (int i = 0; i < size; i++) {
            final MenuItem item = menu.getItem(i);
            if(item.hasSubMenu()) {
                // Un check sub menu items
                unCheckAllMenuItems(item.getSubMenu());
            } else {
                item.setChecked(false);
            }
        }
    }

    private void logout() {
        LogoutDialog dialog = new LogoutDialog(this);
        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        firebaseAuth.addAuthStateListener(firebaseListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(firebaseListener != null)
            firebaseAuth.removeAuthStateListener(firebaseListener);
    }
}