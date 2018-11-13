package com.apps.creativesource.censio;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    private Context context;
    private ArrayList<Comment> commentArrayList = new ArrayList<>();
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

        DatabaseReference userRef = commentArrayList.get(i).userRef;

        if(commentArrayList.get(i).timestamp > lastTimestamp)
            lastTimestamp = commentArrayList.get(i).timestamp;

        userRef
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.exists()) {
                            User user = dataSnapshot.getValue(User.class);

                            profileUri = user.profileUri;
                            Glide.with(context).load(profileUri).into(commentViewHolder.profileImageView);
                            commentViewHolder.usernameTextView.setText(user.name);

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

//                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() { TODO: Remove Comment
//            @Override
//            public void onSuccess(DocumentSnapshot documentSnapshot) {
//                profileUri = documentSnapshot.getString("profileUri");
//                Glide.with(context).load(profileUri).into(commentViewHolder.profileImageView);
//                commentViewHolder.usernameTextView.setText(documentSnapshot.getString("name"));
//            }
//        });

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
