package com.example.notesapp;

import android.app.Application;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MessageVH  extends RecyclerView.ViewHolder {

    TextView nametv,filenametv,texttv,timetv,typetv;
    ImageView downloadiv;
     LinearLayout ll_m;

    public MessageVH(@NonNull View itemView) {
        super(itemView);
    }

    public void setNotes(Application application, String sendername,String senderuid,
                          String note,String time,String type,String url){

        nametv = itemView.findViewById(R.id.uname_ml);
        texttv = itemView.findViewById(R.id.text_ml);
        filenametv = itemView.findViewById(R.id.mfile_name_ml);
        timetv = itemView.findViewById(R.id.time_ml);
        typetv = itemView.findViewById(R.id.type_ml);
        downloadiv = itemView.findViewById(R.id.download_ml);
        ll_m = itemView.findViewById(R.id.m_ll);

        switch (type){
            case "TXT":
                nametv.setText(sendername);
                texttv.setText(note);
                timetv.setText(time);
                typetv.setText(type);
                ll_m.setVisibility(View.GONE);
                break;

            case "PPT":
                ll_m.setVisibility(View.VISIBLE);
                texttv.setVisibility(View.GONE);
                nametv.setText(sendername);
                timetv.setText(time);
                typetv.setText(type);
                filenametv.setText(note);

                filenametv.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_baseline_feed_24, // left
                        0,0,0);

                break;

            case "PDF":

                ll_m.setVisibility(View.VISIBLE);
                texttv.setVisibility(View.GONE);
                nametv.setText(sendername);
                timetv.setText(time);
                typetv.setText(type);
                filenametv.setText(note);

                filenametv.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_baseline_picture_as_pdf_24, // left
                        0,0,0);

                break;

            case "DOCX":
                ll_m.setVisibility(View.VISIBLE);
                texttv.setVisibility(View.GONE);
                nametv.setText(sendername);
                timetv.setText(time);
                typetv.setText(type);
                filenametv.setText(note);

                filenametv.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_baseline_document_scanner_24, // left
                        0,0,0);

                break;

            case "IMG":
                ll_m.setVisibility(View.VISIBLE);
                texttv.setVisibility(View.GONE);
                nametv.setText(sendername);
                timetv.setText(time);
                typetv.setText(type);
                filenametv.setText(note);

                filenametv.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.imageblack, // left
                        0,0,0);

                break;
        }

    }
}
