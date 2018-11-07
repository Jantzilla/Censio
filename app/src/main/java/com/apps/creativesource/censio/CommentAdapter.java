package com.apps.creativesource.censio;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

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

        DocumentReference userRef = commentArrayList.get(i).userRef;

        if(commentArrayList.get(i).timestamp > lastTimestamp)
            lastTimestamp = commentArrayList.get(i).timestamp;

        userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                profileUri = documentSnapshot.getString("profileUri");
                Glide.with(context).load(profileUri).into(commentViewHolder.profileImageView);
                commentViewHolder.usernameTextView.setText(documentSnapshot.getString("name"));
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

        public void swapList(ArrayList<Comment> comments) {                                            //TODO: FIX SWAP METHOD
//        if(!this.commentArrayList.isEmpty())
//            commentArrayList.clear();

        this.commentArrayList.addAll(comments);

        if(!comments.isEmpty())
            this.notifyDataSetChanged();
    }

}
