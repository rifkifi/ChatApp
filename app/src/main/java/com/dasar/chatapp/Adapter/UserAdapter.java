package com.dasar.chatapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.dasar.chatapp.MessageActivity;
import com.dasar.chatapp.Model.Chat;
import com.dasar.chatapp.Model.User;
import com.dasar.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private Context mContext;
    private List<User> mUser;
    private boolean isChat;
    String theLastMsg;

    public UserAdapter(Context mContext, List<User> mUser, boolean isChat){
        this.mContext = mContext;
        this.mUser = mUser;
        this.isChat = isChat;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item, parent,false);
        return new UserAdapter.ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final User user = mUser.get(position);

        holder.username.setText(user.getUsername());
        if (user.getImageURL().equals("default")){
            holder.profile_image.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(mContext).load(user.getImageURL()).into(holder.profile_image);
        }

        if (isChat){
            lastMessage(user.getId(),holder.last_msg);
        } else {
            holder.last_msg.setVisibility(View.GONE);
        }

        if (isChat){
            if (user.getStatus().equals("Online")){
                holder.im_on.setVisibility(View.VISIBLE);
                holder.im_off.setVisibility(View.GONE);
            } else {
                holder.im_off.setVisibility(View.VISIBLE);
                holder.im_on.setVisibility(View.GONE);
            }
        } else {
            holder.im_off.setVisibility(View.GONE);
            holder.im_on.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, MessageActivity.class);
                intent.putExtra("userid", user.getId());
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mUser.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView username, last_msg;
        ImageView profile_image, im_on, im_off;


        ViewHolder(View itemView){
            super(itemView);
            username = itemView.findViewById(R.id.username);
            profile_image = itemView.findViewById(R.id.profile_image);
            im_on = itemView.findViewById(R.id.im_on);
            im_off = itemView.findViewById(R.id.im_off);
            last_msg = itemView.findViewById(R.id.last_msg);

        }
    }

    private void lastMessage(final String userid, final TextView last_msg){
        theLastMsg = "default";
        final FirebaseUser loginUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(loginUser.getUid()) && chat.getSender().equals(userid)
                            || chat.getReceiver().equals(userid) && chat.getSender().equals(loginUser.getUid()) ){
                        theLastMsg = chat.getMessage();
                    }
                }
                switch (theLastMsg){
                    case "default":
                        last_msg.setText("No Message");
                        break;
                    default:
                        last_msg.setText(theLastMsg);
                        break;
                }
                theLastMsg = "default";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
