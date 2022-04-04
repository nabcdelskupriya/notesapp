package com.example.notesapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class TextActivity extends AppCompatActivity {


    ImageButton backbtn;
    EditText titleEt,notesEt;
    Button savebtn;
    Notemember notemember;
    DatabaseReference notesref;
    FirebaseDatabase database = FirebaseDatabase.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text);


        backbtn = findViewById(R.id.back_ib);
        titleEt = findViewById(R.id.title_et);
        notesEt = findViewById(R.id.notes_et);
        savebtn = findViewById(R.id.btn_savetext);

        notemember = new Notemember();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String currentuid = user.getUid();

        notesref = database.getReference("Notes").child(currentuid);


        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent( TextActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        savebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String note = notesEt.getText().toString().trim();
                String title  =titleEt.getText().toString().trim();

                if (note!= null || title!= null){

                    notemember.setType("TXT");
                    notemember.setTitle(title);
                    notemember.setNotes(note);
                    notemember.setDelete(String.valueOf(System.currentTimeMillis()));
                    notemember.setSearch(title.toLowerCase());

                    String key = notesref.push().getKey();
                    notesref.child(key).setValue(notemember);

                    Toast.makeText(TextActivity.this, "Saved", Toast.LENGTH_SHORT).show();

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(TextActivity.this,MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    },1000);

                }else {
                    Toast.makeText(TextActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                }



            }
        });


    }

}