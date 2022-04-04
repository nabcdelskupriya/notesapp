package com.example.notesapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class RoomActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    DatabaseReference nameRef,roomref,memberRef;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    String currentuid,name,category,roomname,address,adminid;
    LinearLayoutManager linearLayoutManager;
    EditText searchEt;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);



        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            roomname = bundle.getString("rn");
            address = bundle.getString("a");
            adminid = bundle.getString("ai");
        }else {
            Toast.makeText(this, "error", Toast.LENGTH_SHORT).show();
        }
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
         currentuid = user.getUid();

        nameRef = database.getReference("users");
        recyclerView = findViewById(R.id.rv_rooms);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        searchEt  = findViewById(R.id.searchEtrooms);


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



    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<NameModal> options =
                new FirebaseRecyclerOptions.Builder<NameModal>()
                        .setQuery(nameRef,NameModal.class)
                        .build();

        FirebaseRecyclerAdapter<NameModal,NameVH> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<NameModal, NameVH>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull NameVH holder, int position, @NonNull NameModal model) {

                        holder.setname(getApplication(),model.getName(),model.getCategory(),model.getSearch());

                        String uid = getRef(position).getKey();
                        String nameadd = getItem(position).getName();
                        String catadd = getItem(position).getCategory();
                        String postkey = getRef(position).getKey();

                        holder.checkuser(postkey,uid,address);

                        if (adminid.equals(uid)){
                            holder.checkBox.setVisibility(View.GONE);
                        }else {

                            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                                    if (b){
                                        createmembers(nameadd,catadd,uid);
                                    }else {
                                        roomref = database.getReference("rooms").child(uid);
                                        roomref.child(address).removeValue();
                                        //nameref = database.getReference("users");
                                        memberRef = database.getReference("members").child(uid);
                                        memberRef.removeValue();
                                    }


                                }
                            });


                    }}

                    @NonNull
                    @Override
                    public NameVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.add_user_item,parent,false);

                        return new NameVH(view);

                    }
                };


        recyclerView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();

    }

    private void search(){

        String query = searchEt.getText().toString().toLowerCase().trim();

        Query query1 = nameRef.orderByChild("search").startAt(query).endAt(query+"\uf0ff");


        FirebaseRecyclerOptions<NameModal> options =
                new FirebaseRecyclerOptions.Builder<NameModal>()
                        .setQuery(query1,NameModal.class)
                        .build();

        FirebaseRecyclerAdapter<NameModal,NameVH> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<NameModal, NameVH>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull NameVH holder, int position, @NonNull NameModal model) {

                        holder.setname(getApplication(),model.getName(),model.getCategory(),model.getSearch());


                        String uid = getRef(position).getKey();
                        String nameadd = getItem(position).getName();
                        String catadd = getItem(position).getCategory();
                        String postkey = getRef(position).getKey();



                        holder.checkuser(postkey,uid,address);

                        if (adminid.equals(uid)){
                            holder.checkBox.setVisibility(View.GONE);
                        }else {

                            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                                    if (b){
                                        createmembers(nameadd,catadd,uid);
                                    }else {
                                        roomref = database.getReference("rooms").child(uid);
                                        roomref.child(address).removeValue();
                                        //nameref = database.getReference("users");
                                        memberRef = database.getReference("members").child(uid);
                                        memberRef.removeValue();
                                    }


                                }
                            });
                        }
                    }

                    @NonNull
                    @Override
                    public NameVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.add_user_item,parent,false);

                        return new NameVH(view);

                    }
                };


        recyclerView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();

    }

    private void createmembers(String nameadd, String catadd, String uid) {


        try {
            nameRef.child(uid).addValueEventListener(new ValueEventListener() {
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


            Calendar date = Calendar.getInstance();
            SimpleDateFormat currentdate = new
                    SimpleDateFormat("dd-MMMM-yyyy");
            final String savedate = currentdate.format(date.getTime());


            Calendar time1 = Calendar.getInstance();
            SimpleDateFormat currenttime = new
                    SimpleDateFormat("HH:mm:ss a");
            final String savetime = currenttime.format(time1.getTime());

            MemberModal memberModal = new MemberModal();

            memberModal.setCat(catadd);
            memberModal.setStatus("Member");
            memberModal.setName(nameadd);
            memberModal.setDate("Date: " + savedate + "Time:" + savetime);

            memberRef = database.getReference("members").child(address);
            memberRef.child(uid).setValue(memberModal);


            ModalRoom modalRoom = new ModalRoom();

            modalRoom.setAdminid("member:" + uid);
            modalRoom.setCreated(name);
            modalRoom.setMembers("0");
            modalRoom.setSearch(roomname.toLowerCase());
            modalRoom.setRooname(roomname);
            modalRoom.setTime(savetime);
            modalRoom.setAddress(address);

            roomref = database.getReference("rooms").child(uid);
            roomref.child(address).setValue(modalRoom);

        }catch (Exception e){


            Toast.makeText(this, "no username exiest", Toast.LENGTH_SHORT).show();
        }



    }
}










