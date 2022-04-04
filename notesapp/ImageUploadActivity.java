package com.example.notesapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class ImageUploadActivity extends AppCompatActivity {

    ArrayList<Uri> imagelist = new ArrayList<Uri>();
    private  int upload_count = 0;
    DatabaseReference roomlist;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    String currentuid,address,sendername,roomname;
    RoomnoteMember member;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageReference;
    Uri imageURL,imageuri;
    Button choosebtn,uploadbtn;
    TextView notv;
    ProgressBar progressBar;
    EditText editText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_upload);

        storageReference = storage.getReference("files");

        notv = findViewById(R.id.tv_no_iv);
        editText = findViewById(R.id.et_iv_filename);
        progressBar = findViewById(R.id.pb_multiple_iv);
        choosebtn = findViewById(R.id.choose_image);
        uploadbtn = findViewById(R.id.upload_image);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        currentuid = user.getUid();
        member = new RoomnoteMember();

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            roomname = extras.getString("rn");
            address = extras.getString("a");
            sendername = extras.getString("n");
        } else {
            Toast.makeText(this, "value missing", Toast.LENGTH_SHORT).show();
        }

        roomlist = database.getReference("notelist").child(address);


        choosebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(intent, 1);
            }
        });


        uploadbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                uploadimage();
            }
        });


        }



    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {

            if (requestCode == 1 || resultCode == RESULT_OK ||
                    data != null || data.getData() != null){

                int countclipdata = data.getClipData().getItemCount();

                int currentImage = 0;

                while (currentImage < countclipdata){
                    imageuri = data.getClipData().getItemAt(currentImage).getUri();
                    imagelist.add(imageuri);
                    currentImage = currentImage+1;

                }
                notv.setVisibility(View.VISIBLE);
                notv.setText("You have selected "+imagelist.size()+" images");


            }

        }catch (Exception e){

            Toast.makeText(this, "please select", Toast.LENGTH_SHORT).show();
        }

    }

    private void uploadimage() {

        progressBar.setVisibility(View.VISIBLE);

        for (upload_count = 0; upload_count < imagelist.size(); upload_count++) {

            final StorageReference reference1 = storageReference.child(System.currentTimeMillis() + "." + "jpg");

            Uri selecteduri = imagelist.get(upload_count);

            UploadTask uploadTask = reference1.putFile(selecteduri);


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

                        Calendar time1 = Calendar.getInstance();
                        SimpleDateFormat currenttime = new
                                SimpleDateFormat("HH:mm:ss a");
                        final String savetime = currenttime.format(time1.getTime());

                        member.setSendername(sendername);
                        member.setSenderuid(currentuid);
                        member.setNote(editText.getText().toString().trim());
                        member.setTime(savetime);
                        member.setUrl(downloadUri.toString());
                        member.setType("IMG");

                        String key = roomlist.push().getKey();
                        roomlist.child(key).setValue(member);

                        editText.setText("");

                        progressBar.setVisibility(View.GONE);

                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                String topic = "/topics/"+address;
                                String data = editText.getText().toString().trim();

                                FcmNotificationsSender notificationsSender =
                                        new FcmNotificationsSender(topic,"Notes app","[" + roomname+ "] " + sendername +":"+ data,
                                                getApplicationContext(),ImageUploadActivity.this);

                                notificationsSender.SendNotifications();

                            }
                        },1000);


                    }
                }
            });
        }

    }}