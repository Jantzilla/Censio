package com.apps.creativesource.censio;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class UserPollsAdapter extends RecyclerView.Adapter<UserPollsAdapter.PollsViewHolder> {
    private ArrayList<Post> postArrayList = new ArrayList<>();
    private boolean twoPane;
    private Context context;
    private String profileUri;
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private ListItemClickListener clickListener;
    private String userRef;

    public interface ListItemClickListener{
        void onListItemClicked(int clickedItemIndex, String profileUri, String username, String statement,
                               String interactionCount, int likes, int dislikes, int postTypeId, String id, String postFireUserId);
    }


    public UserPollsAdapter(Context context, ArrayList<Post> postArrayList, boolean twoPane, ListItemClickListener clickListener) {
        this.postArrayList.addAll(postArrayList);
        this.clickListener = clickListener;
        this.twoPane = twoPane;
        this.context = context;
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

        DocumentReference documentReference = firestore.collection("users").document(userRef);

        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                pollsViewHolder.postFireUserId = documentSnapshot.getId();
                profileUri = documentSnapshot.getString("profileUri");
                Glide.with(context).load(profileUri).into(pollsViewHolder.profileImageView);
                pollsViewHolder.profileTextView.setText(documentSnapshot.getString("name"));

                if(twoPane && i == 0 && !(postArrayList.get(i).author.equals(auth.getUid()))) {
                    pollsViewHolder.itemView.performClick();
                }

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
                    , postTitle, String.valueOf(interactionCount), likes, dislikes, postTypeId, postFireId, postFireUserId);
        }
    }
}
