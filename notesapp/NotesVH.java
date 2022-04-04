package com.example.notesapp;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

public class NotesVH extends RecyclerView.ViewHolder {

    TextView noteTv,titleTv;
    ImageView imageView;
    ImageButton moreoptionsbtn;

    public NotesVH(@NonNull View itemView) {
        super(itemView);
    }

    public void setnote(FragmentActivity activity,String title, String notes, String search, String url,
                        String delete, String type){

        noteTv = itemView.findViewById(R.id.tv_note_item);
        titleTv = itemView.findViewById(R.id.tv_title_item);
        imageView = itemView.findViewById(R.id.iv_notes_item);
        moreoptionsbtn = itemView.findViewById(R.id.moreoptions_btn);

        if (type.equals("TXT")){

            noteTv.setText(notes);
            titleTv.setText(title);
            imageView.setVisibility(View.GONE);

        }else if (type.equals("IMG")){

            imageView.setVisibility(View.VISIBLE);
            noteTv.setText(notes);
            titleTv.setText(title);
            Picasso.get().load(url).into(imageView);


        }

    }
}
