package com.abhay.chatapp;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> mMessagesList;
    private FirebaseAuth mAuth;

    public MessageAdapter(List<Messages> mMessagesList) {
        this.mMessagesList = mMessagesList;
    }


    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.message_single_layout,viewGroup,false);
        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder messageViewHolder, int i) {
        mAuth=FirebaseAuth.getInstance();
        String current_uid=mAuth.getUid();
        Messages c=mMessagesList.get(i);
        String from_user_id=c.getFrom();

        if(from_user_id.equals(current_uid)){

            //means we have sent the message
            messageViewHolder.messageText.setBackgroundResource(R.drawable.message_text_background_white);
            messageViewHolder.messageText.setTextColor(Color.BLACK);


        }
        else {
            //means other user have sent the message
            messageViewHolder.messageText.setBackgroundResource(R.drawable.message_text_background_voilet);
            messageViewHolder.messageText.setTextColor(Color.WHITE);
        }
        messageViewHolder.messageText.setText(c.getMessage());
    }

    @Override
    public int getItemCount() {
        return mMessagesList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{

        public TextView messageText;
        public CircleImageView profileImage;
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            messageText=itemView.findViewById(R.id.message_text_layout);
            profileImage=itemView.findViewById(R.id.message_profile_img_layout);
        }
    }
}
