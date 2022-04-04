package com.example.notesapp;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class Fragment1 extends Fragment implements View.OnClickListener {

    EditText searchEt;
    ImageButton sortbtn;
    RecyclerView recyclerView;
    FloatingActionButton fone,ftwo,fthree,ffour;
    Float transtlationYaxis = 100f;
    Boolean menuOpen = false;
    OvershootInterpolator interpolator = new OvershootInterpolator();
    Uri imageuri;
    DatabaseReference notesref;
    String currentuid,name,category;
    DatabaseReference roomRef,nameRef,roomlist;
    FirebaseDatabase database = FirebaseDatabase.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment1,container,false);
        return view;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        searchEt = getActivity().findViewById(R.id.et_search_f1);
        sortbtn = getActivity().findViewById(R.id.ib_sort_f1);
        recyclerView  = getActivity().findViewById(R.id.rv_f1);
        fone = getActivity().findViewById(R.id.fab1);
        ftwo = getActivity().findViewById(R.id.fab2);
         fthree= getActivity().findViewById(R.id.fab3);
        ffour = getActivity().findViewById(R.id.fab4);


        LinearLayoutManager manager = new LinearLayoutManager(getActivity());

        recyclerView.setLayoutManager(manager);
        recyclerView.setHasFixedSize(true);


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
       currentuid = user.getUid();

        notesref = FirebaseDatabase.getInstance().getReference("Notes").child(currentuid);

        showmenu();


        sortbtn.setOnClickListener((View.OnClickListener) this);

        searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                search();
            }
        });


        nameRef = database.getReference("users");
        nameRef.child(currentuid).addValueEventListener(new ValueEventListener() {
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


    }

    private void showmenu() {

        fone.setAlpha(0f);
        ftwo.setAlpha(0f);
        fthree.setAlpha(0f);

        fone.setTranslationY(transtlationYaxis);
        ftwo.setTranslationY(transtlationYaxis);
        fthree.setTranslationY(transtlationYaxis);

        ffour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (menuOpen){
                    closemenu();
                }else {
                    openmenu();
                }

            }
        });

        fone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getActivity(),TextActivity.class);
                startActivity(intent);
                closemenu();

            }
        });

        ftwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getActivity(),DrawActivity.class);
                startActivity(intent);
                closemenu();

            }
        });

        fthree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent,1);
                closemenu();

            }
        });

    }

    private void openmenu() {

        menuOpen = ! menuOpen;
        ffour.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_24);
        fone.animate().translationY(0f).alpha(1f).setInterpolator(interpolator).setDuration(300).start();
        ftwo.animate().translationY(0f).alpha(1f).setInterpolator(interpolator).setDuration(300).start();
        fthree.animate().translationY(0f).alpha(1f).setInterpolator(interpolator).setDuration(300).start();

    }

    private void closemenu() {

        menuOpen = ! menuOpen;
        ffour.setImageResource(R.drawable.ic_baseline_keyboard_arrow_up_24);
        fone.animate().translationY(transtlationYaxis).alpha(0f).setInterpolator(interpolator).setDuration(300).start();
        ftwo.animate().translationY(transtlationYaxis).alpha(0f).setInterpolator(interpolator).setDuration(300).start();
        fthree.animate().translationY(transtlationYaxis).alpha(0f).setInterpolator(interpolator).setDuration(300).start();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {

            if (requestCode == 1 || resultCode == RESULT_OK ||
            data != null || data.getData() != null){

                imageuri = data.getData();

                if (requestCode == 1){
                    String url = imageuri.toString();
                    Intent intent = new Intent(getActivity(),ImageActivity.class);
                    intent.putExtra("u",url);
                    startActivity(intent);
                }

            }

        }catch (Exception e){

            Toast.makeText(getActivity(), ""+ e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    private  void getnotes(){

        FirebaseRecyclerOptions<Notemember> options =
                new FirebaseRecyclerOptions.Builder<Notemember>()
                .setQuery(notesref,Notemember.class)
                .build();

        FirebaseRecyclerAdapter<Notemember,NotesVH> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Notemember, NotesVH>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull NotesVH holder, int position, @NonNull Notemember model) {

                        holder.setnote(getActivity(),model.getTitle(),model.getNotes(),model.getSearch(),model.getUrl()
                        ,model.getDelete(),model.getType());

                        String postkey = getRef(position).getKey();
                        String title = getItem(position).getTitle();
                        String notes = getItem(position).getNotes();
                        String url = getItem(position).getUrl();
                        String delete = getItem(position).getDelete();
                        String type = getItem(position).getType();


                        holder.moreoptionsbtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                moreoptions(postkey,title,notes,url,delete,type);

                            }
                        });


                    }

                    @NonNull
                    @Override
                    public NotesVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.notes_layout,parent,false);

                        return new NotesVH(view);

                    }
                };


        recyclerView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();

    }

    private void moreoptions(String postkey, String title, String notes, String url, String delete, String type) {

        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.more_options);


        TextView edittv = dialog.findViewById(R.id.tv_edit);
        TextView forwardtv = dialog.findViewById(R.id.tv_forward);
        TextView downloadtv = dialog.findViewById(R.id.tv_download);
        TextView deletetv = dialog.findViewById(R.id.tv_delete);


        if (type.equals("TXT")){
            downloadtv.setVisibility(View.GONE);
        }else if (type.equals("IMG")){

            downloadtv.setVisibility(View.VISIBLE);
        }


        edittv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                editDialog(postkey,title,notes);
                dialog.dismiss();



            }
        });


        forwardtv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                forwardRooom(delete,url,notes,title,type);


            }
        });


        downloadtv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PermissionListener permissionListener = new PermissionListener(){

                    @Override
                    public void onPermissionGranted() {

                        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
                        request.setTitle("Download "+ notes);
                        request.setDescription("Downloading file " + title);
                        request.allowScanningByMediaScanner();
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,title + System.currentTimeMillis() + ".jpg");

                        DownloadManager manager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
                        manager.enqueue(request);
                        Toast.makeText(getActivity(), "Downloading", Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onPermissionDenied(List<String> deniedPermissions) {

                        Toast.makeText(getActivity(), "Please allow for downloading", Toast.LENGTH_SHORT).show();
                    }
                };

                TedPermission.with(getActivity())
                        .setPermissionListener(permissionListener)
                        .setPermissions(Manifest.permission.INTERNET,Manifest.permission.READ_EXTERNAL_STORAGE)
                        .check();

                dialog.dismiss();

            }
        });


        deletetv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Delete");
                builder.setMessage("Are you sure to delete");
                builder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        if (type.equals("IMG")){

                            Query query = notesref.orderByChild("delete").equalTo(delete);
                            query.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {

                                    for (DataSnapshot dataSnapshot :snapshot.getChildren()){
                                        dataSnapshot.getRef().removeValue();
                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                            StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(url);
                            reference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    Toast.makeText(getActivity(), "deleted", Toast.LENGTH_SHORT).show();
                                }
                            });
                            dialog.dismiss();
                        }else if (type.equals("TXT")){

                            Query query2 = notesref.orderByChild("delete").equalTo(delete);
                            query2.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {

                                    for (DataSnapshot dataSnapshot :snapshot.getChildren()){
                                        dataSnapshot.getRef().removeValue();
                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                            Toast.makeText(getActivity(), "deleted", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }

                    }
                });

                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        dialog.dismiss();
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.bottomanim;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

    }

    private void forwardRooom(String delete, String url, String notes, String title, String type) {

         roomRef =database.getReference("rooms").child(currentuid);

        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.forward_bs);

        RecyclerView recyclerView = dialog.findViewById(R.id.rv_forward);

        LinearLayoutManager manager = new LinearLayoutManager(getActivity());

        recyclerView.setLayoutManager(manager);
        recyclerView.setHasFixedSize(true);

        FirebaseRecyclerOptions<ModalRoom> options =
                new FirebaseRecyclerOptions.Builder<ModalRoom>()
                        .setQuery(roomRef,ModalRoom.class)
                        .build();

        FirebaseRecyclerAdapter<ModalRoom,RoomVH> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<ModalRoom, RoomVH>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull RoomVH holder, int position, @NonNull ModalRoom model) {
                        holder.setroomForward(getActivity(),model.getRooname(),model.getAdminid(),model.getAddress()
                                ,model.getTime(),model.getMembers(),model.getCreated(),model.getSearch());

                        String roomname = getItem(position).getRooname();
                        String adminid = getItem(position).getAdminid();
                        String address = getItem(position).getAddress();

                        holder.opentv.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                holder.opentv.setText("Sent");
                                holder.opentv.setTextColor(Color.BLACK);

                                roomlist = database.getReference("notelist").child(address);
                                RoomnoteMember member = new RoomnoteMember();

                                Calendar time1 = Calendar.getInstance();
                                SimpleDateFormat currenttime = new
                                        SimpleDateFormat("HH:mm:ss a");
                                final String savetime = currenttime.format(time1.getTime());

                                member.setSendername(name);
                                member.setSenderuid(currentuid);
                                member.setNote(notes);
                                member.setTime(savetime);
                                member.setUrl(url);
                                member.setType(type);

                                String key = roomlist.push().getKey();
                                roomlist.child(key).setValue(member);

                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        String topic = "/topics/"+address;


                                        FcmNotificationsSender notificationsSender =
                                                new FcmNotificationsSender(topic,"Notes app","[" + roomname+ "] " + name +":"+ title,
                                                       getActivity(),getActivity());

                                        notificationsSender.SendNotifications();


                                    }
                                },1000);


                            }
                        });

                        holder.showmembers(address);



                    }

                    @NonNull
                    @Override
                    public RoomVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.room_layout,parent,false);

                        return new RoomVH(view);

                    }
                };


        recyclerView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();



        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.bottomanim;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

    }

    private void editDialog(String postkey, String title, String notes) {

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.edit_layput,null);


        EditText titleet = view.findViewById(R.id.edit_et_title);
        EditText noteet = view.findViewById(R.id.edit_et_note);
        Button button = view.findViewById(R.id.btn_update);


        titleet.setText(title);
        noteet.setText(notes);

        AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setView(view)
                .create();

        alertDialog.show();


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Map<String,Object> map = new HashMap<>();
                map.put("title",titleet.getText().toString());
                map.put("notes",noteet.getText().toString());
                map.put("search",titleet.getText().toString().toLowerCase());

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String currentuid = user.getUid();

                FirebaseDatabase.getInstance().getReference()
                        .child("Notes").child(currentuid)
                        .child(postkey)
                        .updateChildren(map)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                Toast.makeText(getActivity(), "Updates", Toast.LENGTH_SHORT).show();
                                alertDialog.dismiss();
                            }
                        });




            }
        });

    }


    private void search(){

        String query = searchEt.getText().toString().toLowerCase().trim();

        Query query1 = notesref.orderByChild("search").startAt(query).endAt(query+"\uf0ff");


        FirebaseRecyclerOptions<Notemember> options =
                new FirebaseRecyclerOptions.Builder<Notemember>()
                        .setQuery(query1,Notemember.class)
                        .build();

        FirebaseRecyclerAdapter<Notemember,NotesVH> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Notemember, NotesVH>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull NotesVH holder, int position, @NonNull Notemember model) {

                        holder.setnote(getActivity(),model.getTitle(),model.getNotes(),model.getSearch(),model.getUrl()
                                ,model.getDelete(),model.getType());


                    }

                    @NonNull
                    @Override
                    public NotesVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.notes_layout,parent,false);

                        return new NotesVH(view);

                    }
                };


        recyclerView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();

    }

    private void sorting(String type){


        Query query1 = notesref.orderByChild("type").startAt(type).endAt(type+"\uf0ff");


        FirebaseRecyclerOptions<Notemember> options =
                new FirebaseRecyclerOptions.Builder<Notemember>()
                        .setQuery(query1,Notemember.class)
                        .build();

        FirebaseRecyclerAdapter<Notemember,NotesVH> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Notemember, NotesVH>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull NotesVH holder, int position, @NonNull Notemember model) {

                        holder.setnote(getActivity(),model.getTitle(),model.getNotes(),model.getSearch(),model.getUrl()
                                ,model.getDelete(),model.getType());


                    }

                    @NonNull
                    @Override
                    public NotesVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.notes_layout,parent,false);

                        return new NotesVH(view);

                    }
                };


        recyclerView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();

    }

    @Override
    public void onStart() {
        super.onStart();

        getnotes();
    }


    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.ib_sort_f1:
                sortBottomsheet();
        }
    }

    private void sortBottomsheet() {

        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.sort_bottomsheet);

        TextView onlytext = dialog.findViewById(R.id.only_text);
        TextView onlyimages = dialog.findViewById(R.id.only_image);


        onlytext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String type = "TXT";
                sorting(type);
            }
        });


        onlyimages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String type = "IMG";
                sorting(type);


            }
        });


        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.bottomanim;
        dialog.getWindow().setGravity(Gravity.BOTTOM);




    }
}
