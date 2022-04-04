package com.example.notesapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class OpenRoom extends AppCompatActivity {

    String roomname,address,adminid,name,category,currentuid,status,data;
    Button adduserbtn;
    TextView roomnametv;
    ImageButton sendbtn,addbtn;
    EditText noteEt;
    DatabaseReference nameref,roomlist,roomref,memberRef;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageReference;
    RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager;
    Uri selecteduri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_room);


        storageReference = storage.getReference("files");
        roomnametv = findViewById(R.id.rname_or);
        adduserbtn = findViewById(R.id.adduserbtn_or);
        addbtn = findViewById(R.id.ib_add_or);
        sendbtn = findViewById(R.id.ib_send_or);
        noteEt = findViewById(R.id.et_note_or);
        recyclerView = findViewById(R.id.rv_or);


        linearLayoutManager = new LinearLayoutManager(OpenRoom.this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        currentuid= user.getUid();

        nameref = database.getReference("users");

        Bundle extras = getIntent().getExtras();

        if (extras!= null){
            roomname = extras.getString("rn");
            address =  extras.getString("a");
            adminid = extras.getString("ai");
        }else {
            Toast.makeText(this, "value missing", Toast.LENGTH_SHORT).show();
        }

        FirebaseMessaging.getInstance().subscribeToTopic(address);


        roomnametv.setText(roomname);
        if (adminid.equals(currentuid)){
            adduserbtn.setText("Add users");
            status = "add";

        }else {
            adduserbtn.setText("Leave");
            status = "leave";
        }

        roomlist = database.getReference("notelist").child(address);


        nameref.child(currentuid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    name = (String) snapshot.child("name").getValue();
                    category = (String) snapshot.child("category").getValue();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        adduserbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (status.equals("add")){

                    Intent intent = new Intent(OpenRoom.this,RoomActivity.class);
                    intent.putExtra("rn",roomname);
                    intent.putExtra("ai",adminid);
                    intent.putExtra("a",address);
                    startActivity(intent);

                }else if (status.equals("leave")){

                    roomref = database.getReference("rooms").child(currentuid);
                    memberRef = database.getReference("members").child(address);

                    roomref.child(address).removeValue();
                    memberRef.child(currentuid).removeValue();

                    Intent intent = new Intent(OpenRoom.this,MainActivity.class);
                    startActivity(intent);
                    finish();
                }


            }
        });

        roomnametv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(OpenRoom.this,ShowMembers.class);
                intent.putExtra("a",address);
                startActivity(intent);

            }
        });

        addbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                openBs();

            }
        });


        sendbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                RoomnoteMember member = new RoomnoteMember();

                Calendar time1 = Calendar.getInstance();
                SimpleDateFormat currenttime = new
                        SimpleDateFormat("HH:mm:ss a");
                final String savetime = currenttime.format(time1.getTime());

                member.setSendername(name);
                member.setSenderuid(currentuid);
                member.setNote(noteEt.getText().toString().trim());
                member.setTime(savetime);
                member.setUrl("");
                member.setType("TXT");

                String key = roomlist.push().getKey();
                roomlist.child(key).setValue(member);

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        String topic = "/topics/"+address;
                         data = noteEt.getText().toString().trim();

                        FcmNotificationsSender notificationsSender =
                                new FcmNotificationsSender(topic,"Notes app","[" + roomname+ "] " + name +":"+ data,
                                        getApplicationContext(),OpenRoom.this);

                        notificationsSender.SendNotifications();
                        noteEt.setText("");

                    }
                },1000);



            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            if (requestCode == 2 || resultCode == RESULT_OK ||
            data != null || data.getData() != null){

                selecteduri = data.getData();

                switch (requestCode){
                    case 2:
                        previewPDF(selecteduri);
                        break;
                        case 3:
                        previewDoc(selecteduri);
                        break;
                        case 4:
                        previewPPT(selecteduri);
                        break;

                }


            }else {
                Toast.makeText(this, "pick a file", Toast.LENGTH_SHORT).show();
            }

        }catch (Exception e){
            Toast.makeText(this, "error"+ e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    private void previewPDF(Uri selecteduri) {


        LayoutInflater inflater = LayoutInflater.from(OpenRoom.this);
        View view = inflater.inflate(R.layout.m_preview,null);


        AlertDialog alertDialog = new AlertDialog.Builder(OpenRoom.this)
                .setView(view)
                .create();


        alertDialog.show();

        EditText filenameEt = view.findViewById(R.id.filenameEt);
        Button sendBtn1 = view.findViewById(R.id.send_notesBtn);


        filenameEt.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(getApplication(),
                R.drawable.ic_baseline_feed_24),
                null,null,null);
        filenameEt.setHint("Enter PDF filename");

        sendBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                sendBtn1.setText("Uploading");

                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageReference ;
                storageReference = storage.getReference("notesimages");

                final StorageReference reference1 = storageReference.child(System.currentTimeMillis()+"."+"pdf");
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


                            RoomnoteMember member = new RoomnoteMember();

                            Calendar time1 = Calendar.getInstance();
                            SimpleDateFormat currenttime = new
                                    SimpleDateFormat("HH:mm:ss a");
                            final String savetime = currenttime.format(time1.getTime());

                            member.setSendername(name);
                            member.setSenderuid(currentuid);
                            member.setNote(filenameEt.getText().toString().trim());
                            member.setTime(savetime);
                            member.setUrl(downloadUri.toString());
                            member.setType("PDF");

                            String key = roomlist.push().getKey();
                            roomlist.child(key).setValue(member);



                            filenameEt.setText("");


                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    String topic = "/topics/"+address;
                                    String data = filenameEt.getText().toString().trim();

                                    FcmNotificationsSender notificationsSender =
                                            new FcmNotificationsSender(topic,"Notes app","[" + roomname+ "] " + name +":"+ data,
                                                    getApplicationContext(),OpenRoom.this);

                                    notificationsSender.SendNotifications();

                                }
                            },1000);

                            alertDialog.dismiss();


                        } else {
                            // Handle failures
                            // ...
                        }
                    }
                });


            }
        });




    }

    private void previewDoc(Uri selecteduri) {

        LayoutInflater inflater = LayoutInflater.from(OpenRoom.this);
        View view = inflater.inflate(R.layout.m_preview,null);


        AlertDialog alertDialog = new AlertDialog.Builder(OpenRoom.this)
                .setView(view)
                .create();


        alertDialog.show();

        EditText filenameEt = view.findViewById(R.id.filenameEt);
        Button sendBtn1 = view.findViewById(R.id.send_notesBtn);


        filenameEt.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(getApplication(),
                R.drawable.ic_baseline_document_scanner_24),
                null,null,null);
        filenameEt.setHint("Enter Document filename");

        sendBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                sendBtn1.setText("Uploading");

                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageReference ;
                storageReference = storage.getReference("notesimages");

                final StorageReference reference1 = storageReference.child(System.currentTimeMillis()+"."+"docx");
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


                            RoomnoteMember member = new RoomnoteMember();

                            Calendar time1 = Calendar.getInstance();
                            SimpleDateFormat currenttime = new
                                    SimpleDateFormat("HH:mm:ss a");
                            final String savetime = currenttime.format(time1.getTime());

                            member.setSendername(name);
                            member.setSenderuid(currentuid);
                            member.setNote(filenameEt.getText().toString().trim());
                            member.setTime(savetime);
                            member.setUrl(downloadUri.toString());
                            member.setType("DOCX");

                            String key = roomlist.push().getKey();
                            roomlist.child(key).setValue(member);

                            filenameEt.setText("");

                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    String topic = "/topics/"+address;
                                    String data = filenameEt.getText().toString().trim();

                                    FcmNotificationsSender notificationsSender =
                                            new FcmNotificationsSender(topic,"Notes app","[" + roomname+ "] " + name +":"+ data,
                                                    getApplicationContext(),OpenRoom.this);

                                    notificationsSender.SendNotifications();

                                }
                            },1000);

                            alertDialog.dismiss();


                        } else {
                            // Handle failures
                            // ...
                        }
                    }
                });


            }
        });
    }


    private void previewPPT(Uri selecteduri) {

        LayoutInflater inflater = LayoutInflater.from(OpenRoom.this);
         View view = inflater.inflate(R.layout.m_preview,null);


        AlertDialog alertDialog = new AlertDialog.Builder(OpenRoom.this)
                .setView(view)
                .create();


        alertDialog.show();

        EditText filenameEt = view.findViewById(R.id.filenameEt);
        Button sendBtn1 = view.findViewById(R.id.send_notesBtn);


        filenameEt.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(getApplication(),
                R.drawable.ic_baseline_feed_24),
                null,null,null);
        filenameEt.setHint("Enter PPT filename");

        sendBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                sendBtn1.setText("Uploading");

                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageReference ;
                storageReference = storage.getReference("notesimages");

                final StorageReference reference1 = storageReference.child(System.currentTimeMillis()+"."+"pptx");
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


                            RoomnoteMember member = new RoomnoteMember();

                            Calendar time1 = Calendar.getInstance();
                            SimpleDateFormat currenttime = new
                                    SimpleDateFormat("HH:mm:ss a");
                            final String savetime = currenttime.format(time1.getTime());

                            member.setSendername(name);
                            member.setSenderuid(currentuid);
                            member.setNote(filenameEt.getText().toString().trim());
                            member.setTime(savetime);
                            member.setUrl(downloadUri.toString());
                            member.setType("PPT");

                            String key = roomlist.push().getKey();
                            roomlist.child(key).setValue(member);

                            filenameEt.setText("");

                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    String topic = "/topics/"+address;
                                    String data = filenameEt.getText().toString().trim();

                                    FcmNotificationsSender notificationsSender =
                                            new FcmNotificationsSender(topic,"Notes app","[" + roomname+ "] " + name +":"+ data,
                                                    getApplicationContext(),OpenRoom.this);

                                    notificationsSender.SendNotifications();

                                }
                            },1000);

                            alertDialog.dismiss();


                        } else {
                            // Handle failures
                            // ...
                        }
                    }
                });


            }
        });

    }

    private void openBs() {

        final Dialog dialog = new Dialog(OpenRoom.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.add_file_bs);

        TextView imagetv = dialog.findViewById(R.id.pickImage);
        TextView docxtv = dialog.findViewById(R.id.pickdocx);
        TextView pdftv = dialog.findViewById(R.id.pickpdf);
        TextView pptv = dialog.findViewById(R.id.pickppt);


        imagetv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(OpenRoom.this,ImageUploadActivity.class);
                intent.putExtra("a",address);
                intent.putExtra("n",name);
                intent.putExtra("rn",roomname);
                startActivity(intent);

                dialog.dismiss();

            }
        });


        pdftv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent();
                intent.setType("application/pdf");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent,2);
                dialog.dismiss();


            }
        });


        docxtv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent();
                intent.setType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent,3);
                dialog.dismiss();


            }
        });


        pptv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent();
                intent.setType("application/vnd.openxmlformats-officedocument.presentationml.presentation");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent,4);
                dialog.dismiss();


            }
        });






        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.bottomanim;
        dialog.getWindow().setGravity(Gravity.BOTTOM);


    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<RoomnoteMember> options =
                new FirebaseRecyclerOptions.Builder<RoomnoteMember>()
                        .setQuery(roomlist,RoomnoteMember.class)
                        .build();

        FirebaseRecyclerAdapter<RoomnoteMember,MessageVH> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<RoomnoteMember, MessageVH>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull MessageVH holder, int position, @NonNull RoomnoteMember model) {

                        holder.setNotes(getApplication(),model.getSendername(),model.getSenderuid(),model.getNote()
                        ,model.getTime(),model.getType(),model.getUrl());

                        String type = getItem(position).getType();
                        String url = getItem(position).getUrl();
                        String filename = getItem(position).getNote();

                        holder.downloadiv.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                PermissionListener permissionListener = new PermissionListener(){

                                    @Override
                                    public void onPermissionGranted() {
                                        downlaodfile(type,url,filename);

                                    }

                                    @Override
                                    public void onPermissionDenied(List<String> deniedPermissions) {

                                        Toast.makeText(OpenRoom.this, "Please allow for downloading", Toast.LENGTH_SHORT).show();
                                    }
                                };

                                TedPermission.with(OpenRoom.this)
                                        .setPermissionListener(permissionListener)
                                        .setPermissions(Manifest.permission.INTERNET,Manifest.permission.READ_EXTERNAL_STORAGE)
                                        .check();

                            }
                        });

                        holder.texttv.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {


                                try {

                                    Uri uri = Uri.parse(filename);
                                    Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                                    startActivity(intent);

                                }catch (Exception e){
                                    Toast.makeText(OpenRoom.this, "only for valid links", Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public MessageVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.message_layout,parent,false);

                        return new MessageVH(view);

                    }
                };


        recyclerView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();

    }

    private void downlaodfile(String type, String url, String filename) {

        switch (type){
            case "TXT":
                Toast.makeText(this, "cannot download text", Toast.LENGTH_SHORT).show();
                break;

            case "PPT":


                        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
                        request.setTitle("Download "+ filename);
                        request.setDescription("Downloading file " + filename);
                        request.allowScanningByMediaScanner();
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,filename + System.currentTimeMillis() + ".pptx");

                        DownloadManager manager = (DownloadManager) OpenRoom.this.getSystemService(Context.DOWNLOAD_SERVICE);
                        manager.enqueue(request);

                        Toast.makeText(OpenRoom.this, "downloading", Toast.LENGTH_SHORT).show();



                break;

            case "PDF":


                DownloadManager.Request request2 = new DownloadManager.Request(Uri.parse(url));
                request2.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
                request2.setTitle("Download "+ filename);
                request2.setDescription("Downloading file " + filename);
                request2.allowScanningByMediaScanner();
                request2.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request2.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,filename + System.currentTimeMillis() + ".pdf");

                DownloadManager manager2 = (DownloadManager) OpenRoom.this.getSystemService(Context.DOWNLOAD_SERVICE);
                manager2.enqueue(request2);

                Toast.makeText(OpenRoom.this, "downloading", Toast.LENGTH_SHORT).show();

                break;

                case "DOCX":


                DownloadManager.Request request3 = new DownloadManager.Request(Uri.parse(url));
                request3.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
                request3.setTitle("Download "+ filename);
                request3.setDescription("Downloading file " + filename);
                request3.allowScanningByMediaScanner();
                request3.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request3.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,filename + System.currentTimeMillis() + ".docx");

                DownloadManager manager3 = (DownloadManager) OpenRoom.this.getSystemService(Context.DOWNLOAD_SERVICE);
                manager3.enqueue(request3);

                Toast.makeText(OpenRoom.this, "downloading", Toast.LENGTH_SHORT).show();

                break;

                case "IMG":


                DownloadManager.Request request4 = new DownloadManager.Request(Uri.parse(url));
                request4.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
                request4.setTitle("Download "+ filename);
                request4.setDescription("Downloading file " + filename);
                request4.allowScanningByMediaScanner();
                request4.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request4.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,filename + System.currentTimeMillis() + ".jpg");

                DownloadManager manager4 = (DownloadManager) OpenRoom.this.getSystemService(Context.DOWNLOAD_SERVICE);
                manager4.enqueue(request4);

                Toast.makeText(OpenRoom.this, "downloading", Toast.LENGTH_SHORT).show();

                break;
        }

    }
}
