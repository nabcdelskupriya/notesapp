package com.example.notesapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bnv);
        bottomNavigationView.setOnNavigationItemSelectedListener(onnav);

        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout,
                new Fragment1()).commit() ;


        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (task.isSuccessful()){
                            String token = task.getResult();
                            FirebaseDatabase.getInstance().getReference("Token").child(uid).child("token").setValue(token);
                        }else {
                            Toast.makeText(MainActivity.this, "Token missing", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    private BottomNavigationView.OnNavigationItemSelectedListener onnav =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                    Fragment selected = null;
                    switch (item.getItemId()){
                        case R.id.notesid:
                            selected = new Fragment1();
                            break;

                            case R.id.roomid:
                            selected = new Fragment2();
                            break;

                            case R.id.profileid:
                            selected = new Fragment3();
                            break;

                    }
                    getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout,selected).commit();
                    return true;
                }
            };


}