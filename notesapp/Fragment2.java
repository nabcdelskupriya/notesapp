package com.example.notesapp;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class Fragment2  extends Fragment implements View.OnClickListener {

    EditText searchEt;
    TextView createRoomtv;
    ImageButton sortBtn;
    RecyclerView recyclerView;
    DatabaseReference roomref,nameref,memberRef;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    String currentuid,name,category,time;
    LinearLayoutManager linearLayoutManager;
    ModalRoom modalRoom;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment2,container,false);
        return view;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        currentuid = user.getUid();

        modalRoom = new ModalRoom();
        searchEt = getActivity().findViewById(R.id.searchEtf2);
        createRoomtv = getActivity().findViewById(R.id.create_roomf2);
        sortBtn = getActivity().findViewById(R.id.ib_sort_f2);
        recyclerView = getActivity().findViewById(R.id.rv_f2);

        roomref = database.getReference("rooms").child(currentuid);
        nameref = database.getReference("users");
        memberRef = database.getReference("members");



        linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);


        createRoomtv.setOnClickListener(this);
        sortBtn.setOnClickListener(this);
        createRoomtv.setClickable(false);


        nameref.child(currentuid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    name = (String) snapshot.child("name").getValue();
                    category = (String) snapshot.child("category").getValue();
                    createRoomtv.setClickable(true);
                }else {
                    createRoomtv.setClickable(false);
                    //Toast.makeText(getActivity(), "add username first", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

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
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.create_roomf2:
                createRoom();
                break;

                case R.id.ib_sort_f2:
                sortRooms();
                break;
        }
    }

    private void sortRooms() {

        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.f2_sort);

        TextView joinedtv = dialog.findViewById(R.id.show_joinedtv);
        TextView createdtv = dialog.findViewById(R.id.show_createdtv);


        joinedtv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String joined = "member:"+currentuid;
                sort(joined);

            }
        });

        createdtv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String created = currentuid;
                sort(created);


            }
        });



        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.bottomanim;
        dialog.getWindow().setGravity(Gravity.BOTTOM);



    }

    private void sort(String s) {

        Query query1 = roomref.orderByChild("adminid").startAt(s).endAt(s+"\uf0ff");

        FirebaseRecyclerOptions<ModalRoom> options =
                new FirebaseRecyclerOptions.Builder<ModalRoom>()
                        .setQuery(query1,ModalRoom.class)
                        .build();

        FirebaseRecyclerAdapter<ModalRoom,RoomVH> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<ModalRoom, RoomVH>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull RoomVH holder, int position, @NonNull ModalRoom model) {
                        holder.setroom(getActivity(),model.getRooname(),model.getAdminid(),model.getAddress()
                                ,model.getTime(),model.getMembers(),model.getCreated(),model.getSearch());

                        String roomname = getItem(position).getRooname();
                        String adminid = getItem(position).getAdminid();
                        String address = getItem(position).getAddress();



                        holder.opentv.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                Intent intent = new Intent(getActivity(),OpenRoom.class);
                                intent.putExtra("rn",roomname);
                                intent.putExtra("ai",adminid);
                                intent.putExtra("a",address);
                                startActivity(intent);

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

    }

    private void createRoom() {

        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.create_room_bs);

        EditText roomet = dialog.findViewById(R.id.roomName_etf2);
        Button createbtn = dialog.findViewById(R.id.btn_cr);

        Calendar date = Calendar.getInstance();
        SimpleDateFormat currentdate = new
                SimpleDateFormat("dd-MMMM-yyyy");
        final String savedate = currentdate.format(date.getTime());


        Calendar time1 = Calendar.getInstance();
        SimpleDateFormat currenttime = new
                SimpleDateFormat("HH:mm:ss a");
        final String savetime = currenttime.format(time1.getTime());

        time = savedate+savetime;

        createbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String roomname = roomet.getText().toString().trim();
                final String address = roomname+currentuid+System.currentTimeMillis();

                if (roomname!= null){
                    modalRoom.setAddress(address);
                    modalRoom.setAdminid(currentuid);
                    modalRoom.setCreated(name);
                    modalRoom.setSearch(roomname.toLowerCase());
                    modalRoom.setRooname(roomname);
                    modalRoom.setTime(time);
                    modalRoom.setMembers("0");

                    roomref.child(address).setValue(modalRoom);

                    MemberModal memberModal = new MemberModal();

                    memberModal.setCat(category);
                    memberModal.setDate(savedate + " Time:" +savetime);
                    memberModal.setName(name);
                    memberModal.setStatus("admin");

                    memberRef.child(address).child(currentuid).setValue(memberModal);

                    Toast.makeText(getActivity(), "Room created successfully", Toast.LENGTH_SHORT).show();



                }else {
                    Toast.makeText(getActivity(), "please enter a name", Toast.LENGTH_SHORT).show();
                }

            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.bottomanim;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

    }


    @Override
    public void onStart() {
        super.onStart();

        getnotes();
    }

    private void getnotes() {

        FirebaseRecyclerOptions<ModalRoom> options =
                new FirebaseRecyclerOptions.Builder<ModalRoom>()
                        .setQuery(roomref,ModalRoom.class)
                        .build();

        FirebaseRecyclerAdapter<ModalRoom,RoomVH> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<ModalRoom, RoomVH>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull RoomVH holder, int position, @NonNull ModalRoom model) {
                        holder.setroom(getActivity(),model.getRooname(),model.getAdminid(),model.getAddress()
                        ,model.getTime(),model.getMembers(),model.getCreated(),model.getSearch());

                        String roomname = getItem(position).getRooname();
                        String adminid = getItem(position).getAdminid();
                        String address = getItem(position).getAddress();



                        holder.opentv.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                Intent intent = new Intent(getActivity(),OpenRoom.class);
                                intent.putExtra("rn",roomname);
                                intent.putExtra("ai",adminid);
                                intent.putExtra("a",address);
                                startActivity(intent);

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
    }

    private void search (){

        String query = searchEt.getText().toString().toLowerCase().trim();

        Query query1 = roomref.orderByChild("search").startAt(query).endAt(query+"\uf0ff");


        FirebaseRecyclerOptions<ModalRoom> options =
                new FirebaseRecyclerOptions.Builder<ModalRoom>()
                        .setQuery(query1,ModalRoom.class)
                        .build();

        FirebaseRecyclerAdapter<ModalRoom,RoomVH> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<ModalRoom, RoomVH>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull RoomVH holder, int position, @NonNull ModalRoom model) {
                        holder.setroom(getActivity(),model.getRooname(),model.getAdminid(),model.getAddress()
                                ,model.getTime(),model.getMembers(),model.getCreated(),model.getSearch());

                        String roomname = getItem(position).getRooname();
                        String adminid = getItem(position).getAdminid();
                        String address = getItem(position).getAddress();



                        holder.opentv.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                Intent intent = new Intent(getActivity(),OpenRoom.class);
                                intent.putExtra("rn",roomname);
                                intent.putExtra("ai",adminid);
                                intent.putExtra("a",address);
                                startActivity(intent);

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

    }
}
