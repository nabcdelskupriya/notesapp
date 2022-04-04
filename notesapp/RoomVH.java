package com.example.notesapp;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RoomVH extends RecyclerView.ViewHolder {

    TextView nametv,createdtv,memberstv,opentv;
    DatabaseReference memberRef;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    int count;


    public RoomVH(@NonNull View itemView) {
        super(itemView);
    }

    public void setroom(FragmentActivity activity , String rooname, String adminid, String address,
                       String  time, String members, String created,String search){

        nametv = itemView.findViewById(R.id.nametv_item);
        createdtv = itemView.findViewById(R.id.createdtv_item);
        memberstv = itemView.findViewById(R.id.memberstv_item);
        opentv = itemView.findViewById(R.id.opentv_item);


        nametv.setText(rooname);
        createdtv.setText("Created By " +created );


    }


    public void setroomForward(FragmentActivity activity , String rooname, String adminid, String address,
                       String  time, String members, String created,String search){

        nametv = itemView.findViewById(R.id.nametv_item);
        createdtv = itemView.findViewById(R.id.createdtv_item);
        memberstv = itemView.findViewById(R.id.memberstv_item);
        opentv = itemView.findViewById(R.id.opentv_item);


        nametv.setText(rooname);
        createdtv.setText("Created By " +created );
        opentv.setText("Send");


    }

    public void showmembers(String address){
        memberRef = database.getReference("members");

        memberRef.child(address).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()){
                    count = (int) snapshot.getChildrenCount();
                    memberstv.setText("Total members: " +count);

                }else {

                    count = (int) snapshot.getChildrenCount();
                    memberstv.setText("Total members: " +count);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}
