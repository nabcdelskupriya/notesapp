package com.example.notesapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class ImageActivity extends AppCompatActivity {


    ImageButton backbtn;
    EditText titleEt,notesEt;
    Button savebtn;
    Notemember notemember;
    DatabaseReference notesref;
    ImageView imageView;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    Uri imageuri,imageurl;
    String uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);


        backbtn = findViewById(R.id.back_ib_iv);
        titleEt = findViewById(R.id.title_et_iv);
        notesEt = findViewById(R.id.notes_et_iv);
        savebtn = findViewById(R.id.btn_saveiv);
        imageView = findViewById(R.id.iv_notes);


        notemember = new Notemember();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String currentuid = user.getUid();

        notesref = database.getReference("Notes").child(currentuid);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            uri = bundle.getString("u");
        }else {
            Toast.makeText(this, "unable to fetch url", Toast.LENGTH_SHORT).show();
        }


        imageurl = Uri.parse(uri);

        Picasso.get().load(imageurl).into(imageView);

        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent( ImageActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        });


        savebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                uploadImage();


            }
        });

    }

    private void uploadImage() {

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference ;
        storageReference = storage.getReference("images");

        String note = notesEt.getText().toString().trim();
        String title  =titleEt.getText().toString().trim();

        final StorageReference reference1 = storageReference.child(System.currentTimeMillis()+"."+"jpg");
        UploadTask uploadTask = reference1.putFile(imageurl);


        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return reference1.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();


                    notemember.setDelete(String.valueOf(System.currentTimeMillis()));
                    notemember.setNotes(note);
                    notemember.setSearch(title.toLowerCase());
                    notemember.setTitle(title);
                    notemember.setUrl(downloadUri.toString());
                    notemember.setType("IMG");

                    String key = notesref.push().getKey();
                    notesref.child(key).setValue(notemember);

                    Toast.makeText(ImageActivity.this, "File uploaded", Toast.LENGTH_SHORT).show();

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            Intent intent = new Intent(ImageActivity.this,MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    },1000);
                } else {
                    // Handle failures
                    // ...
                }

    }
});
    }

}