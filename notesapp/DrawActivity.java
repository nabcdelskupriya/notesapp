package com.example.notesapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
import android.net.Network;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import static com.example.notesapp.Display.colorlist;
import static com.example.notesapp.Display.current_brush;
import static com.example.notesapp.Display.pathlist;

public class DrawActivity extends AppCompatActivity {

    public  static Path path = new Path();
    public static Paint paint_brush = new Paint();


    RelativeLayout relativeLayout;
    Display display;

    ImageButton pencil,eraser;
    TextView tvUpload;
    String currentuid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);

        pencil = findViewById(R.id.ib_pencil_draw);
        eraser = findViewById(R.id.ib_eraser_draw);
        tvUpload = findViewById(R.id.tv_upload_draw);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        currentuid = user.getUid();

        display = findViewById(R.id.draw);
        relativeLayout = findViewById(R.id.rl_paint);


        pencil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                paint_brush.setColor(Color.BLACK);
                currentColor(paint_brush.getColor());
            }
        });

        eraser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                pathlist.clear();;
                colorlist.clear();
                path.reset();
            }
        });

        tvUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Bitmap bitmap = Bitmap.createBitmap(relativeLayout.getWidth(),relativeLayout.getHeight(),
                        Bitmap.Config.ARGB_8888);

                Canvas canvas = new Canvas(bitmap);
                relativeLayout.draw(canvas);


                LayoutInflater inflater = LayoutInflater.from(DrawActivity.this);
                view = inflater.inflate(R.layout.draw_preview,null);

                ImageView imageView = view.findViewById(R.id.iv_draw);
                EditText etdraw = view.findViewById(R.id.draw_title);
                Button cancelbtn = view.findViewById(R.id.cancel_draw);
                Button uploadbtn = view.findViewById(R.id.upload_draw);
                ProgressBar pb = view.findViewById(R.id.pb_draw);


                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageReference ;
                storageReference = storage.getReference("images");


                FirebaseDatabase database = FirebaseDatabase.getInstance();

                DatabaseReference reference = database.getReference("Notes").child(currentuid);


                AlertDialog alertDialog = new AlertDialog.Builder(DrawActivity.this)
                        .setView(view)
                        .create();


                alertDialog.show();

                imageView.setImageBitmap(bitmap);

                cancelbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        alertDialog.dismiss();
                    }
                });



                uploadbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {


                        pb.setVisibility(View.VISIBLE);
                        String title = etdraw.getText().toString().trim();
                        imageView.setDrawingCacheEnabled(true);
                        imageView.buildDrawingCache();


                        Bitmap bitmap1 = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap1.compress(Bitmap.CompressFormat.JPEG,100,baos);
                        byte[] data = baos.toByteArray();

                        Notemember notemember = new Notemember();

                        final StorageReference reference1 = storageReference.child(System.currentTimeMillis()+"."+"jpg");
                        UploadTask uploadTask = reference1.putBytes(data);


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

                                    pb.setVisibility(View.GONE);
                                    notemember.setDelete(String.valueOf(System.currentTimeMillis()));
                                    notemember.setNotes("");
                                    notemember.setSearch(title.toLowerCase());
                                    notemember.setTitle(title);
                                    notemember.setUrl(downloadUri.toString());
                                    notemember.setType("IMG");

                                    String key = reference.push().getKey();
                                    reference.child(key).setValue(notemember);

                                    Toast.makeText(DrawActivity.this, "File uploaded", Toast.LENGTH_SHORT).show();

                                    Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {

                                            Intent intent = new Intent(DrawActivity.this,MainActivity.class);
                                            startActivity(intent);
                                            alertDialog.dismiss();
                                            finish();
                                        }
                                    },1000);
                                } else {
                                    // Handle failures
                                    // ...
                                }
                            }
                        });




                    }
                });







            }
        });

    }

    private void currentColor(int color) {

        current_brush = color;
        path = new Path();

    }

    public void yellowclicked(View view) {

        paint_brush.setColor(Color.YELLOW);
        currentColor(paint_brush.getColor());

    }

    public void whiteclicked(View view) {

        paint_brush.setColor(Color.LTGRAY);
        currentColor(paint_brush.getColor());

    }

    public void redclicked(View view) {

        paint_brush.setColor(Color.RED);
        currentColor(paint_brush.getColor());

    }

    public void blackclicked(View view) {

        paint_brush.setColor(Color.BLACK);
        currentColor(paint_brush.getColor());
    }

    public void blueclicked(View view) {
        paint_brush.setColor(Color.BLUE);
        currentColor(paint_brush.getColor());
    }
}