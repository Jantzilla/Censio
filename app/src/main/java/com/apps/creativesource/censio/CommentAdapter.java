package com.apps.creativesource.censio;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    private Context context;
    private ArrayList<Comment> commentArrayList = new ArrayList<>();
    private DatabaseReference realtimeRef = FirebaseDatabase.getInstance().getReference();
    private String profileUri;
    public Long lastTimestamp = Long.valueOf("0");

    public CommentAdapter(ArrayList<Comment> commentArrayList) {
        this.commentArrayList.addAll(commentArrayList);
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.comment_item, viewGroup, false);

        CommentViewHolder viewHolder = new CommentViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder commentViewHolder, int i) {

        if(commentArrayList.isEmpty())
            return;

        String comment = commentArrayList.get(i).comment;

        String userRef = commentArrayList.get(i).userRef;

        if(commentArrayList.get(i).timestamp > lastTimestamp)
            lastTimestamp = commentArrayList.get(i).timestamp;

        realtimeRef
                .child("users")
                .child(userRef)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.exists()) {
                            User user = dataSnapshot.getValue(User.class);
                            profileUri = user.profileUri;

                            if(profileUri != null)
                                Glide.with(context).load(profileUri).into(commentViewHolder.profileImageView);

                            if(user.name != null)
                                commentViewHolder.usernameTextView.setText(user.name);

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        commentViewHolder.commentTextView.setText(comment);

    }


    @Override
    public int getItemCount() {
        return commentArrayList.size();
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profileImageView;
        TextView usernameTextView;
        TextView commentTextView;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImageView = itemView.findViewById(R.id.iv_profile);
            commentTextView = itemView.findViewById(R.id.tv_comment);
            usernameTextView = itemView.findViewById(R.id.tv_username);
        }
    }

        public void swapList(ArrayList<Comment> comments) {

        this.commentArrayList.addAll(comments);

        if(!comments.isEmpty())
            this.notifyDataSetChanged();
    }

}
