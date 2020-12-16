package ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.barberme.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.ArrayList;

import dialog.ForgotPasswordDialog;
import model.Consumer;
import service.UploadNewUserService;
import userData.User;

public class SignInUpActivity extends AppCompatActivity
    implements SignInFragment.SignInListener, SignUpFragment.SignUpListener{

    final String TAG = "SignInUpActivity";
    FirebaseAuth firebaseAuth;
    final String annonymousPicture = "https://firebasestorage.googleapis.com/v0/b/barberme-83e8b.appspot.com/o/profiles%2FprofilePicture.png?alt=media&token=996a8ccb-7f65-43da-86ad-4619a9580fa5";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signinup);
        firebaseAuth = FirebaseAuth.getInstance();
        getSupportFragmentManager().beginTransaction().add(R.id.container, new SignInFragment(), TAG).commit();
    }

    @Override
    public void onSignInFragmentLoginClick(String email, String password) {
        if(!email.isEmpty() || !password.isEmpty()) {
            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(SignInUpActivity.this, getResources().getString(R.string.signin_successful), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SignInUpActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else
                    Toast.makeText(SignInUpActivity.this, getResources().getString(R.string.signin_failed), Toast.LENGTH_SHORT).show();
            });
        }
        else
            Toast.makeText(SignInUpActivity.this, getResources().getString(R.string.empty_textboxes), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSignInFragmentRegisterClick() {
        getSupportFragmentManager().beginTransaction().replace(R.id.container, new SignUpFragment(), TAG).addToBackStack(null).commit();
    }

    @Override
    public void onForgotPasswordClick() {
        ForgotPasswordDialog dialog = new ForgotPasswordDialog(this);
        dialog.show();
    }

    @Override
    public void onGuestLoginClick() {
        firebaseAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            Intent intent = new Intent(SignInUpActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(SignInUpActivity.this, getResources().getString(R.string.signin_failed),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onSignUpFragmentLoginClick() {
        getSupportFragmentManager().beginTransaction().replace(R.id.container, new SignInFragment(), TAG).addToBackStack(null).commit();
    }

    @Override
    public void onSignUpFragmentRegisterClick(String firstName, String lastName, String email, String password, String repeatPassword,String gender,String birthday,String address) {
        if(!firstName.isEmpty() && !lastName.isEmpty() && !address.isEmpty() && !birthday.isEmpty() && !email.isEmpty() && !password.isEmpty() && !repeatPassword.isEmpty() && password.equals(repeatPassword)) {
            Uri profilePicture = Uri.parse(annonymousPicture);
            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    firebaseAuth.getCurrentUser().updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(firstName +" "+ lastName).setPhotoUri(profilePicture).build()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                User user = new User(firebaseAuth.getCurrentUser().getUid(),firstName,lastName,password,email,annonymousPicture,gender,birthday,address, true, 0.0, 0.0);
                                publishNewUser(user);
                                ProgressDialog pd = new ProgressDialog(SignInUpActivity.this);
                                pd.setMessage("loading");
                                pd.show();
                                try {
                                    Thread.sleep(5000);
                                    pd.dismiss();
                                } catch (InterruptedException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                                Toast.makeText(SignInUpActivity.this, getResources().getString(R.string.signup_successful), Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(SignInUpActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                            else
                                Toast.makeText(SignInUpActivity.this, getResources().getString(R.string.signup_failed), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else
                    Toast.makeText(SignInUpActivity.this, getResources().getString(R.string.signup_failed), Toast.LENGTH_SHORT).show();
            });
        }
        else
            Toast.makeText(SignInUpActivity.this, getResources().getString(R.string.equal_passwords_empty_boxes), Toast.LENGTH_SHORT).show();
    }

    private void publishNewUser(User user) {

        Intent intent = new Intent(this, UploadNewUserService.class)
                .putExtra("uID",user.getuID())
                .putExtra("firstName",user.getFirstName())
                .putExtra("lastName",user.getLastName())
                .putExtra( "password",user.getPassword())
                .putExtra("email",user.getEmail())
                .putExtra("profilePicture",user.getProfilePicture())
                .putExtra("gender",user.getGender())
                .putExtra("birthday",user.getBirthday())
                .putExtra("address",user.getAddress());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startForegroundService(intent);
        else
            startService(intent);

    }
}
