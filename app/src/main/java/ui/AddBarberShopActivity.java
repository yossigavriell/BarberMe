package ui;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.barberme.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.util.ArrayList;

import adapter.PictureAdapter;
import service.UploadPostService;

public class AddBarberShopActivity extends AppCompatActivity {
    private FirebaseUser currentUser;
    private FirebaseAuth auth;;
    private RecyclerView picturesList;
    private PictureAdapter pictureAdapter;
    private ArrayList<Uri> pictures = new ArrayList<>();
    private ImageButton uploadPicture;
    private Button finishBT;
    private TextView picturesCountTv;
    private EditText nameET;
    private EditText cityET;
    private EditText addressET;
    private EditText phoneNumberET;
    private EditText websiteET;
    private EditText typeET;
    private File file;
    private int numOfPictures = 0;
    private final int SELECT_IMAGE = 1;
    private final int CAMERA_REQUEST = 2;
    private final int WRITE_PERMISSION_REQUEST = 3;
    private Uri imageUri;

    @Override
    protected void onStart() {
        super.onStart();
        currentUser = auth.getCurrentUser();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_add_barbershop);
        auth = FirebaseAuth.getInstance();
        uploadPicture = findViewById(R.id.upload_button);
        picturesList = findViewById(R.id.recyclerview_pics);
        picturesCountTv = findViewById(R.id.pictures_count_tv);
        picturesCountTv = findViewById(R.id.pictures_count_tv);
        nameET = findViewById(R.id.name_et);
        cityET = findViewById(R.id.city_et);
        addressET = findViewById(R.id.address_et);
        phoneNumberET = findViewById(R.id.phone_et);
        websiteET = findViewById(R.id.website_et);
        typeET = findViewById(R.id.type_et);
        finishBT = findViewById(R.id.finish_button);
        picturesCountTv.setText(this.getResources().getString(R.string.pictures_count) + " " + numOfPictures);
        picturesList.setLayoutManager(new GridLayoutManager(this, 3));
        pictureAdapter = new PictureAdapter(pictures);
        picturesList.setAdapter(pictureAdapter);
        finishBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(pictures.size() != 0)
                    publishNewShop();
                else
                    Toast.makeText(AddBarberShopActivity.this, AddBarberShopActivity.this.getResources().getString(R.string.atleast_one_picture), Toast.LENGTH_SHORT).show();

            }
        });

        uploadPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final CharSequence[] options = { AddBarberShopActivity.this.getResources().getString(R.string.take_picture), AddBarberShopActivity.this.getResources().getString(R.string.choose_picture),AddBarberShopActivity.this.getResources().getString(R.string.cancel) };
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(AddBarberShopActivity.this,R.style.AlertDialog_Builder);
                builder.setTitle(AddBarberShopActivity.this.getResources().getString(R.string.upload_pictures_title));
                builder.setItems(options, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        if (options[item].equals(AddBarberShopActivity.this.getResources().getString(R.string.take_picture))) {
                            //Request permissions
                            if(Build.VERSION.SDK_INT>=23) {
                                int hasWritePermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
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
                        } else if (options[item].equals(AddBarberShopActivity.this.getResources().getString(R.string.choose_picture))) {
                            uploadPicture();

                        } else if (options[item].equals(AddBarberShopActivity.this.getResources().getString(R.string.cancel))) {
                            dialog.dismiss();
                        }
                    }
                });
                builder.show();
            }
        });
    }

    //Publish new barber shop
    private void publishNewShop() {
        if(UploadPostService.isRunning()) {
            showMsg(this.getResources().getString(R.string.double_post));
            return;
        }
        Intent intent = new Intent(this, UploadPostService.class)
                .putParcelableArrayListExtra("images", pictures)
                .putExtra("userId", currentUser.getUid())
                .putExtra("userName", currentUser.getDisplayName())
                .putExtra("phoneNumber", phoneNumberET.getText().toString())
                .putExtra("title", nameET.getText().toString())
                .putExtra("city", cityET.getText().toString())
                .putExtra("website", websiteET.getText().toString())
                .putExtra("rate", 0)
                .putExtra("type", typeET.getText().toString())
                .putExtra("address", addressET.getText().toString());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startForegroundService(intent);
        else
            startService(intent);
        Intent mainIntent = new Intent(this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }

    private void showMsg(String msg) {
        new AlertDialog.Builder(this)
                .setTitle(this.getResources().getString(R.string.note))
                .setMessage(msg)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setIcon(R.mipmap.ic_launcher)
                .setPositiveButton(this.getResources().getString(R.string.ok), null)
                .create()
                .show();
    }

    //Upload picture from gallery
    private void uploadPicture() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, this.getResources().getString(R.string.select_picture)), SELECT_IMAGE);
    }

    //Take picture from camera
    private void takePicture(){
        String pictureName = String.valueOf(System.currentTimeMillis());
        file = new File(this.getExternalFilesDir(null), pictureName + ".jpg");
        imageUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, CAMERA_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    addPictureFromGallery(data);
                }
            }
        }
        else if (requestCode == CAMERA_REQUEST) {
            if (resultCode == RESULT_OK) {
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
        numOfPictures++;
        pictures.add(imageUri);
        picturesCountTv.setText(this.getResources().getString(R.string.pictures_count) + " " + numOfPictures);
        pictureAdapter.notifyDataSetChanged();
    }

    private void addPictureFromCamera() {
        numOfPictures++;
        pictures.add(imageUri);
        picturesCountTv.setText(this.getResources().getString(R.string.pictures_count) + " " + numOfPictures);
        pictureAdapter.notifyDataSetChanged();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==WRITE_PERMISSION_REQUEST){
            if(grantResults[0]!=PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, this.getResources().getString(R.string.permission), Toast.LENGTH_SHORT).show();
            }
            else{
                //Has permissions
                takePicture();
            }
        }
    }
}
