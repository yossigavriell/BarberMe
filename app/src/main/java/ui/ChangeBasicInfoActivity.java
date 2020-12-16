package ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.barberme.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import model.Consumer;
import model.DatabaseFetch;
import userData.User;

public class ChangeBasicInfoActivity extends AppCompatActivity {

    EditText firstName;
    EditText lastName;
    EditText email;
    EditText newPassword;
    EditText repeatPassword;
    Button finishEditInfo;
    User userInfo;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    String oldPasswordAuth;
    String oldEmailAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_basic_info_layout_activity);
        firstName = findViewById(R.id.first_name_et);
        lastName = findViewById(R.id.last_name_et);
        email = findViewById(R.id.email_et);
        newPassword = findViewById(R.id.preference_new_password_et);
        repeatPassword = findViewById(R.id.preference_confirm_password_et);
        finishEditInfo = findViewById(R.id.save_change_edit_personal_info);
        Consumer<User> consumer = new Consumer<User>() {
            @Override
            public void apply(User param) {
                userInfo = param;
                firstName.setText(userInfo.getFirstName());
                lastName.setText(userInfo.getLastName());
                email.setText(userInfo.getEmail());
            }
        };
        DatabaseFetch databaseFetch = new DatabaseFetch();
        databaseFetch.findUserData(consumer, auth.getCurrentUser().getUid());
        finishEditInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!firstName.getText().toString().isEmpty() || !lastName.getText().toString().isEmpty() || !email.getText().toString().isEmpty())
                {
                    oldPasswordAuth = userInfo.getPassword();
                    oldEmailAuth = userInfo.getEmail();
                    userInfo.setFirstName(firstName.getText().toString());
                    userInfo.setLastName(lastName.getText().toString());
                    userInfo.setEmail(email.getText().toString());
                    if(newPassword.getText().toString().isEmpty())
                    {
                        updateUserInfo(false);
                    }
                    else if(!newPassword.getText().toString().isEmpty() && !repeatPassword.getText().toString().isEmpty() && newPassword.getText().toString().equals(repeatPassword.getText().toString()))
                    {
                        userInfo.setPassword(newPassword.getText().toString());
                        updateUserInfo(true);
                    }
                    else
                        Toast.makeText(ChangeBasicInfoActivity.this, getResources().getString(R.string.equal_passwords_empty_boxes), Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(ChangeBasicInfoActivity.this, getResources().getString(R.string.empty_textboxes), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUserInfo(boolean updatePassword) {
        AuthCredential credential = EmailAuthProvider
                .getCredential(oldEmailAuth, oldPasswordAuth);
        auth.getCurrentUser().reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    if(updatePassword)
                        updatePassword();
                    else
                        updateEmail();
                }
                else
                    Toast.makeText(ChangeBasicInfoActivity.this, getResources().getString(R.string.error_message), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updatePassword() {
        auth.getCurrentUser().updatePassword(userInfo.getPassword()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    updateEmail();
                }
                else
                    Toast.makeText(ChangeBasicInfoActivity.this, getResources().getString(R.string.error_message), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateEmail() {
        auth.getCurrentUser().updateEmail(userInfo.getEmail()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    String username = userInfo.getFirstName() + " " + userInfo.getLastName();
                    auth.getCurrentUser().updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(username).build()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Intent intent = new Intent("usernameChange");
                            LocalBroadcastManager.getInstance(ChangeBasicInfoActivity.this).sendBroadcast(intent);
                            updateFirestore();
                        }
                    });
                }
            }
        });
    }

    private void updateFirestore() {
        FirebaseFirestore.getInstance().collection("users").document(userInfo.getId())
                .set(userInfo, SetOptions.merge());
        Toast.makeText(this, getResources().getString(R.string.update_successful), Toast.LENGTH_SHORT).show();
        finish();
    }
}
