package com.example.notesapp;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
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

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Fragment3 extends Fragment implements View.OnClickListener {

    ImageButton logoutbtn,editbtn;
    TextView nametv,categorytv,tvtv;
    FirebaseAuth mAuth;
    NameModal modal;
    DatabaseReference nameRef;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    String currentuid,name,category;
    private GoogleSignInClient mGoogleSignInClient;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment3,container,false);
        return view;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        nametv = getActivity().findViewById(R.id.tv_namef3);
        categorytv = getActivity().findViewById(R.id.tv_category);
        tvtv = getActivity().findViewById(R.id.tvtv);
        logoutbtn = getActivity().findViewById(R.id.logout_f3);
        editbtn = getActivity().findViewById(R.id.edit_f3);



        mAuth = FirebaseAuth.getInstance();
        modal = new NameModal();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        currentuid= user.getUid();

        nameRef = database.getReference("users");

        logoutbtn.setOnClickListener(this);
        editbtn.setOnClickListener(this);

        nameRef.child(currentuid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    name = (String) snapshot.child("name").getValue();
                    category = (String) snapshot.child("category").getValue();
                    tvtv.setVisibility(View.GONE);

                    nametv.setText(name);
                    categorytv.setText(category);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    @Override
    public void onStart() {
        super.onStart();

        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(getActivity());
        if (signInAccount != null){
            name = signInAccount.getDisplayName();
            nametv.setText(name);
        }else {

        }

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.logout_f3:
                mAuth.signOut();

                Intent intent = new Intent(getActivity(),Login.class);
                startActivity(intent);
                break;

            case R.id.edit_f3:
                showbottomsheet();
                break;

        }
    }

    private void showbottomsheet() {

        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.name_bottomsheet);

        EditText nameEt =  dialog.findViewById(R.id.uname_et_f3);
        EditText catet =  dialog.findViewById(R.id.ucategory_et_f3);
        Button savebtn = dialog.findViewById(R.id.btn_f3);

        nameEt.setText(name);

        nameRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.hasChild(currentuid)){
                    savebtn.setText("Update");
                }else {
                    savebtn.setText("Add name");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        nameRef.child(currentuid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                nameEt.setText(name);
                catet.setText(category);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        savebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String name = nameEt.getText().toString().trim();
                String cat = catet.getText().toString().trim();


                modal.setName(name);
                modal.setCategory(cat);
                modal.setSearch(name.toLowerCase());

                nameRef.child(currentuid).setValue(modal).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Toast.makeText(getActivity(), "name added successfully", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });

            }
        });


        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.bottomanim;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

    }
}
