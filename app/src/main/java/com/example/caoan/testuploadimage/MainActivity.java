package com.example.caoan.testuploadimage;

import android.app.ProgressDialog;
import android.content.Intent;
import android.drm.DrmManagerClient;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private Button btnChoose, btnUpload,btngetlink;
    private ImageView image;
    private final int PICK_IMAGE_REQUEST = 71;
    private Uri filePath;
    private Bitmap bitmap;

    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private FirebaseDatabase firebaseDatabase;
    private String name;
    private String generatedFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnChoose = findViewById(R.id.btnChoose);
        btnUpload = findViewById(R.id.btnUpload);
        btngetlink = findViewById(R.id.btngetlink);
        image = findViewById(R.id.image);

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference("Image");
        firebaseDatabase = FirebaseDatabase.getInstance();

        btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                String key = reference.push().getKey();
//                if(bitmap != null ){
//                    reference.child(key).setValue(bitmap);
//                    Toast.makeText(getApplicationContext(),"success",Toast.LENGTH_SHORT).show();
//                }else {
//                    Toast.makeText(getApplicationContext(),"null",Toast.LENGTH_SHORT).show();
//                }
                uploadImage();
            }
        });

        btngetlink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storageReference.child("images/"+name).getDownloadUrl()
                        .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        // Got the download URL for 'users/me/profile.png'
                        //Uri downloadUri = taskSnapshot.getMetadata().getDownloadUrl();
                        generatedFilePath = uri.toString(); /// The string(file link) that you need
                        System.out.println(generatedFilePath);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                        Toast.makeText(getApplicationContext(),"Failed "+ exception.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void uploadImage() {
        if(filePath != null)
        {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            name = UUID.randomUUID().toString();
            StorageReference ref = storageReference.child("images/"+ name);
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(MainActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(MainActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded "+(int)progress+"%");
                        }
                    });
        }
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Picture"),
                PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){
            filePath = data.getData();
            try {
                Bitmap bm= MediaStore.Images.Media.getBitmap(getContentResolver(),filePath);
                image.setImageBitmap(bm);

//                System.out.println(bm);
//                bitmap = bm;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
