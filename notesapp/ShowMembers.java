package com.example.notesapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ShowMembers extends AppCompatActivity {


    DatabaseReference nameRef,roomref,memberRef;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    LinearLayoutManager linearLayoutManager;
    RecyclerView recyclerView;
    String address;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_members);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
//            roomname = bundle.getString("rn");
            address = bundle.getString("a");
//            adminid = bundle.getString("ai");
        }else {
            Toast.makeText(this, "error", Toast.LENGTH_SHORT).show();
        }

        recyclerView = findViewById(R.id.rv_members);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);

        memberRef = database.getReference("members").child(address);
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<MemberModal> options =
                new FirebaseRecyclerOptions.Builder<MemberModal>()
                        .setQuery(memberRef,MemberModal.class)
                        .build();

        FirebaseRecyclerAdapter<MemberModal,NameVH> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<MemberModal, NameVH>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull NameVH holder, int position, @NonNull MemberModal model) {

                        holder.setMembers(getApplication(),model.getName(),model.getCat(),model.getStatus(),model.getDate());


                        }

                    @NonNull
                    @Override
                    public NameVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.member_layout,parent,false);

                        return new NameVH(view);

                    }
                };


        recyclerView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();

    }
}