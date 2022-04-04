package com.example.notesapp;

import android.app.Application;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NameVH  extends RecyclerView.ViewHolder {

    TextView nametv,cattv,membertvname,membertvtime,membertvcat;
    CheckBox checkBox;
    CardView cardView ;
    ConstraintLayout cl;

    public NameVH(@NonNull View itemView) {
        super(itemView);
    }

    public void setname(Application application,String name,String category,String search){

        nametv = itemView.findViewById(R.id.add_usernametv);
        cattv = itemView.findViewById(R.id.add_usercattv);
        checkBox = itemView.findViewById(R.id.cb_adduser);

        nametv.setText(name);
        cattv.setText(category);

    }

    public  void setMembers(Application application, String name, String cat, String status, String date){

        membertvcat = itemView.findViewById(R.id.member_catitm);
        membertvname = itemView.findViewById(R.id.member_nameitm);
        membertvtime = itemView.findViewById(R.id.member_timeitm);


        membertvcat.setText(cat);
        membertvtime.setText("Joined: "+date);
        membertvname.setText(name);

    }



    public void checkuser(String postkey,String uid,String address){

        cardView = itemView.findViewById(R.id.cv_room);
        cl = itemView.findViewById(R.id.cl_room);
        DatabaseReference memberref;
        memberref = FirebaseDatabase.getInstance().getReference("members").child(address);

        memberref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.hasChild(uid)){
                    cl.setVisibility(View.GONE);
                    cardView.setVisibility(View.GONE);
                    nametv.setVisibility(View.GONE);
                    checkBox.setVisibility(View.GONE);
                    cattv.setVisibility(View.GONE);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}
