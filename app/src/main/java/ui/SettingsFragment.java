package ui;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.bumptech.glide.Glide;
import com.example.barberme.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.firestore.v1.WriteResult;

import java.io.File;
import java.net.PasswordAuthentication;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import model.Consumer;
import model.DatabaseFetch;
import service.UploadPostService;
import userData.BarberShop;
import userData.Review;
import userData.User;

public class SettingsFragment extends PreferenceFragmentCompat {

    Button saveProfilePicChangesBtn,editProfilePicBtn;
    ImageView profilePicIv;
    File file;
    private final int SELECT_IMAGE = 1;
    private final int CAMERA_REQUEST = 2;
    private final int WRITE_PERMISSION_REQUEST = 3;
    private Uri imageUri;
    DatabaseFetch databaseFetch = new DatabaseFetch();
    String imageUrl;
    User currentUser;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public boolean onPreferenceTreeClick(androidx.preference.Preference preference) {
        switch (preference.getKey()) {
            case "ChangeProfilePicture":
            {
                changeProfilePicture();
                break;
            }
            case "ChangeBasicInfo":
            {
                changeBasicInfo();
                break;
            }
            case "DeleteAccount":
            {
                deleteAccount();
                break;
            }
        }
        return super.onPreferenceTreeClick(preference);
    }

    private void deleteAccount() {
        final AlertDialog.Builder builderDialog = new AlertDialog.Builder(SettingsFragment.this.getContext());
        final View dialogView = getLayoutInflater().inflate(R.layout.delete_account_dialog, null);
        Button deleteAccount = dialogView.findViewById(R.id.delete_account_bt);
        builderDialog.setView(dialogView);
        AlertDialog alertDialog = builderDialog.create();
        alertDialog.show();
        Consumer<User> uid = new Consumer<User>() {
            @Override
            public void apply(User param) {
                currentUser = param;
            }
        };
        databaseFetch.findUserData(uid, FirebaseAuth.getInstance().getCurrentUser().getUid());
        deleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AuthCredential credential = EmailAuthProvider
                        .getCredential(currentUser.getEmail(), currentUser.getPassword());
                FirebaseAuth.getInstance().getCurrentUser().reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        FirebaseAuth.getInstance().getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful())
                                {
                                    deleteAccountFromDatabase();
                                }
                                else
                                    Toast.makeText(SettingsFragment.this.getContext(), SettingsFragment.this.getContext().getResources().getString(R.string.error_message), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        });
    }

    private void deleteAccountFromDatabase() {
        Consumer<List<BarberShop>> consumer = new Consumer<List<BarberShop>>() {
            @Override
            public void apply(List<BarberShop> param) {
                for(BarberShop barberShop : param)
                    FirebaseFirestore.getInstance().collection("shops").document(barberShop.getId()).delete();
                FirebaseFirestore.getInstance().collection("users").document(currentUser.getuID()).delete();
                Toast.makeText(SettingsFragment.this.getContext(), SettingsFragment.this.getContext().getResources().getString(R.string.account_deleted), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(SettingsFragment.this.getContext(), SignInUpActivity.class);
                startActivity(intent);
            }
        };
        databaseFetch.fetchUserBarberShops(consumer, currentUser.getuID());
    }

    private void changeBasicInfo() {
        Intent intent = new Intent(this.getContext(), ChangeBasicInfoActivity.class);
        startActivity(intent);
    }

    private void changeProfilePicture() {
        final AlertDialog.Builder builderDialog = new AlertDialog.Builder(SettingsFragment.this.getContext());
        final View dialogView = getLayoutInflater().inflate(R.layout.change_profile_pic_dialog, null);

        saveProfilePicChangesBtn = dialogView.findViewById(R.id.save_profile_pic_changes_btn);
        editProfilePicBtn = dialogView.findViewById(R.id.edit_profile_pic_btn);
        profilePicIv=dialogView.findViewById(R.id.profile_pic_image_view);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        Glide.with(this.getContext()).load(user.getPhotoUrl()).into(profilePicIv);
        builderDialog.setView(dialogView);
        AlertDialog alertDialog = builderDialog.create();
        editProfilePicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });
        saveProfilePicChangesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveProfilePicture();
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }

    private void saveProfilePicture() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        user.updateProfile(new UserProfileChangeRequest.Builder().setPhotoUri(imageUri).build()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    Toast.makeText(SettingsFragment.this.getContext(), SettingsFragment.this.getContext().getResources().getString(R.string.profile_picture_chaneged), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent("profilePictureChanged");
                    LocalBroadcastManager.getInstance(SettingsFragment.this.getContext()).sendBroadcast(intent);
                    updateFirestore();
                }
                else
                    Toast.makeText(SettingsFragment.this.getContext(), SettingsFragment.this.getContext().getResources().getString(R.string.error_message), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void selectImage() {
        final CharSequence[] options = { SettingsFragment.this.getContext().getResources().getString(R.string.take_picture), SettingsFragment.this.getContext().getResources().getString(R.string.choose_picture) };
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext(),R.style.AlertDialog_Builder);
        builder.setTitle(SettingsFragment.this.getContext().getResources().getString(R.string.choose_picture));
        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals(SettingsFragment.this.getContext().getResources().getString(R.string.take_picture))) {
                    if(Build.VERSION.SDK_INT>=23) {
                        int hasWritePermission = getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                        if(hasWritePermission!= PackageManager.PERMISSION_GRANTED){
                            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_PERMISSION_REQUEST);
                        }
                        else{
                            //has permission
                            takePicture();
                        }
                    }
                    else {
                        //has permission
                        takePicture();
                    }

                } else if (options[item].equals(SettingsFragment.this.getContext().getResources().getString(R.string.choose_picture))) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, SettingsFragment.this.getContext().getResources().getString(R.string.choose_picture)), SELECT_IMAGE);

                } else if (options[item].equals(SettingsFragment.this.getContext().getResources().getString(R.string.cancel))) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void takePicture() {
        String pictureName = String.valueOf(System.currentTimeMillis());
        file = new File(getActivity().getExternalFilesDir(null), pictureName + ".jpg");
        imageUri = FileProvider.getUriForFile(SettingsFragment.this.getContext(), getActivity().getPackageName() + ".provider", file);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, CAMERA_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    addPictureFromGallery(data);
                }
            }
        }
        else if (requestCode == CAMERA_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                addPictureFromCamera();
            }
            else {
                try {
                    file.delete();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                file = null;
            }
        }
    }

    private void addPictureFromGallery(Intent data) {
        imageUri = data.getData();
        Glide.with(this.getContext()).load(imageUri).into(profilePicIv);
    }

    private void addPictureFromCamera() {
        Glide.with(this.getContext()).load(imageUri).into(profilePicIv);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==WRITE_PERMISSION_REQUEST){
            if(grantResults[0]!=PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this.getContext(), SettingsFragment.this.getContext().getResources().getString(R.string.permission), Toast.LENGTH_SHORT).show();
            }
            else{
                //Has permissions
                takePicture();
            }
        }
    }

    private void updateFirestore() {
        Consumer<User> consumer = new Consumer<User>() {
            @Override
            public void apply(User param) {
                Consumer<String> updateImage = new Consumer<String>() {
                    @Override
                    public void apply(String param1) {
                        param.setProfilePicture(param1);
                        FirebaseFirestore.getInstance().collection("users").document(param.getId())
                                .set(param, SetOptions.merge());
                    }
                };
                uploadPhotoToPhotos(updateImage);
            }
        };
        databaseFetch.findUserData(consumer, FirebaseAuth.getInstance().getCurrentUser().getUid());
    }

    private void uploadPhotoToPhotos(Consumer<String> consumer) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference imagesRef = storage.getReference().child("profiles");;
        final StorageReference photoRef = imagesRef.child(String.valueOf(System.currentTimeMillis()));
        UploadTask uploadTask = photoRef.putFile(imageUri);
        //upload the img + perform a task of getting the download url from the cloud
        uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                task.getException().printStackTrace();
                return null;
            }
            return photoRef.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                imageUrl = task.getResult().toString();
                consumer.apply(imageUrl);
            }
        });
    }
}
