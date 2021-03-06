package com.apps.creativesource.censio;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class UserPollsAdapter extends RecyclerView.Adapter<UserPollsAdapter.PollsViewHolder> {
    private boolean first;
    private ArrayList<Post> postArrayList = new ArrayList<>();
    private boolean twoPane;
    private Context context;
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private DatabaseReference realtimeRef = FirebaseDatabase.getInstance().getReference();
    private ListItemClickListener clickListener;
    private String userRef;

    public interface ListItemClickListener{
        void onListItemClicked(int clickedItemIndex, String profileUri, String username, String statement,
                               String interactionCount, int likes, int dislikes, int postTypeId, String id, String postFireUserId, ImageView profileImageView);
    }


    public UserPollsAdapter(Context context, ArrayList<Post> postArrayList,boolean first, boolean twoPane, ListItemClickListener clickListener) {
        this.postArrayList.addAll(postArrayList);
        this.clickListener = clickListener;
        this.twoPane = twoPane;
        this.context = context;
        this.first = first;
    }

    @NonNull
    @Override
    public PollsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());

        View view = inflater.inflate(R.layout.post_item, viewGroup, false);

        PollsViewHolder viewHolder = new PollsViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull UserPollsAdapter.PollsViewHolder pollsViewHolder, int i) {

            if(postArrayList.isEmpty())
                return;

        pollsViewHolder.postTitle = postArrayList.get(i).statement;
        pollsViewHolder.postFireId = postArrayList.get(i).firestoreId;
        pollsViewHolder.likes = postArrayList.get(i).likes;
        pollsViewHolder.dislikes = postArrayList.get(i).dislikes;
        pollsViewHolder.interactionCount = postArrayList.get(i).interactionCount;
        pollsViewHolder.postTypeId = postArrayList.get(i).postTypeId;
        userRef = postArrayList.get(i).userRef;

        DatabaseReference documentReference = realtimeRef.child("users").child(userRef);

        documentReference.
                addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.exists()) {
                            User user = dataSnapshot.getValue(User.class);

                            pollsViewHolder.postFireUserId = user.id;
                            pollsViewHolder.profileUri = user.profileUri;

                            if(pollsViewHolder.profileUri != null)
                                Glide.with(context).load(pollsViewHolder.profileUri).into(pollsViewHolder.profileImageView);

                            if(user.name != null)
                                pollsViewHolder.profileTextView.setText(user.name);

                            if(first && twoPane && i == 0 && !(postArrayList.get(i).author.equals(auth.getUid()))) {
                                pollsViewHolder.itemView.performClick();
                            }

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        pollsViewHolder.statementTextView.setText(pollsViewHolder.postTitle);
        pollsViewHolder.interactionTextView.setText(String.valueOf(pollsViewHolder.interactionCount));
        pollsViewHolder.itemView.setTag(pollsViewHolder.postFireId);
        pollsViewHolder.interactionImageView.setImageResource(pollsViewHolder.postTypeId);

    }

    @Override
    public int getItemCount() {
        return postArrayList.size();
    }


    class PollsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public String profileUri;
        private TextView statementTextView;
        private TextView profileTextView;
        private TextView interactionTextView;
        private ImageView profileImageView;
        private ImageView interactionImageView;
        private String postTitle;
        private String postFireId;
        private String postFireUserId;
        private int postTypeId;
        private int interactionCount;
        private int likes;
        private int dislikes;

        public PollsViewHolder(@NonNull View itemView) {
            super(itemView);
            statementTextView = itemView.findViewById(R.id.tv_statement);
            profileTextView = itemView.findViewById(R.id.tv_profile);
            interactionTextView = itemView.findViewById(R.id.tv_interaction_count);
            profileImageView = itemView.findViewById(R.id.iv_profile);
            interactionImageView = itemView.findViewById(R.id.iv_interaction);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int clickedPosition = getAdapterPosition();
            clickListener.onListItemClicked(clickedPosition, profileUri, profileTextView.getText().toString()
                    , postTitle, String.valueOf(interactionCount), likes, dislikes, postTypeId, postFireId, postFireUserId, profileImageView);
        }
    }
}
