package com.apps.creativesource.censio;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentDetailFragment extends Fragment {

    private ArrayList<Comment> commentArrayList = new ArrayList<>();
    private String commentEntry;
    private EditText commentEditText;
    private TextView statementTextView;
    private TextView usernameTextView;
    private TextView interactionCountTextView;
    private TextView likesCountTextView;
    private TextView dislikesCountTextView;
    private RecyclerView commentRecyclerView;
    private CircleImageView circleImageView;
    private ImageView interactionImageView;
    private ImageView likesImageView;
    private ImageView dislikesImageView;
    private FloatingActionButton fab;
    private CommentAdapter adapter;
    private SharedPreferences sharedPreferences;
    private DatabaseReference realtimeRef;
    private String postId;
    private int likeCode = 0;
    private String postUserId;
    private boolean userPost;

    private InterstitialAd interstitialAd;

    public CommentDetailFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comment_detail, container, false);


        MobileAds.initialize(getContext(), getString(R.string.app_id_test_mob));

        interstitialAd = new InterstitialAd(getContext());
        interstitialAd.setAdUnitId(getString(R.string.ad_id_test_mob));
        interstitialAd.loadAd(new AdRequest.Builder().build());

        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                interstitialAd.loadAd(new AdRequest.Builder().build());
            }

        });

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        realtimeRef = FirebaseDatabase.getInstance().getReference();

        circleImageView = view.findViewById(R.id.iv_profile);
        interactionImageView = view.findViewById(R.id.iv_comments);
        likesImageView = view.findViewById(R.id.iv_likes);
        dislikesImageView = view.findViewById(R.id.iv_dislikes);
        statementTextView = view.findViewById(R.id.tv_statement);
        usernameTextView = view.findViewById(R.id.tv_username);
        interactionCountTextView = view.findViewById(R.id.tv_comments_count);
        likesCountTextView = view.findViewById(R.id.tv_likes_count);
        dislikesCountTextView = view.findViewById(R.id.tv_dislikes_count);
        fab = view.findViewById(R.id.fab_delete);

        commentEditText = view.findViewById(R.id.et_comment_post);
        commentRecyclerView = view.findViewById(R.id.rv_comment_list);

        Intent initialIntent = getActivity().getIntent();

        if(initialIntent.hasExtra("statement")) {
            Glide.with(this).load(initialIntent.getStringExtra("profileUri")).into(circleImageView);
            statementTextView.setText(initialIntent.getStringExtra("statement"));
            usernameTextView.setText(initialIntent.getStringExtra("username"));
            interactionCountTextView.setText(initialIntent.getStringExtra("interactionCount"));
            likesCountTextView.setText(String.valueOf(initialIntent.getIntExtra("likes", 0)));
            dislikesCountTextView.setText(String.valueOf(initialIntent.getIntExtra("dislikes", 0)));
            interactionImageView.setImageResource(initialIntent.getIntExtra("postTypeId", R.drawable.ic_touch_app_primary_28dp));
            postId = initialIntent.getStringExtra("firestoreId");
            postUserId = initialIntent.getStringExtra("postFireUserId");
            userPost = initialIntent.getBooleanExtra("userPost", false);

        } else {
            Glide.with(this).load(getArguments().getString("profileUri")).into(circleImageView);
            statementTextView.setText(getArguments().getString("statement"));
            usernameTextView.setText(getArguments().getString("username"));
            interactionCountTextView.setText(getArguments().getString("interactionCount"));
            likesCountTextView.setText(String.valueOf(getArguments().getInt("likes", 0)));
            dislikesCountTextView.setText(String.valueOf(getArguments().getInt("dislikes", 0)));
            interactionImageView.setImageResource(getArguments().getInt("postTypeId", R.drawable.ic_touch_app_primary_28dp));
            postId = getArguments().getString("firestoreId");
            postUserId = getArguments().getString("postFireUserId");
            userPost = getArguments().getBoolean("userPost", false);
        }

        if(!userPost || getActivity().findViewById(R.id.detail_container) == null)
            fab.hide();

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        commentRecyclerView.setLayoutManager(layoutManager);
        commentRecyclerView.setHasFixedSize(true);

        getAllComments();

        commentEditText.setOnEditorActionListener(new EditText.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    createPostInteractions(0,0, "comment");
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(commentEditText.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(getString(R.string.delete_post_confirm));
                builder.setCancelable(true);
                builder.setPositiveButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.setNegativeButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deletePost();
                        Intent homeIntent = new Intent(getContext(), MainActivity.class);
                        startActivity(homeIntent);
                        getActivity().finish();
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });

        getAllInteraction();
        setLikeClickListeners();

        return view;
    }

    private void getAllComments() {

        realtimeRef.child("posts")
                .child(postId)
                .child("comments")
                .orderByChild("timestamp")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.exists()) {

                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                Comment comment = snapshot.getValue(Comment.class);
                                commentArrayList.add(comment);
                            }

                            loadAdapter(commentArrayList);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
//                .get() //TODO: REMOVE COMMENT
//                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//
//                        if (task.isSuccessful()) {
//                            for (QueryDocumentSnapshot document : task.getResult()) {
//                                Comment comment = document.toObject(Comment.class);
//                                commentArrayList.add(comment);
//
//                            }
//                            loadAdapter(commentArrayList);
//                        }
//                    }
//                });
    }

    private void newCommentListener() {

        DatabaseReference docRef = realtimeRef.child("posts")
                .child(postId);

        docRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()) {

                    Post post = dataSnapshot.getValue(Post.class);

                    if(post.interactionCount > adapter.getItemCount()) {


                        docRef.child("comments")
                                .orderByChild("timestamp")
                                .startAt(adapter.lastTimestamp + 1)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        if(dataSnapshot.exists()) {
                                            ArrayList<Comment> newComments = new ArrayList<>();

                                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                Comment comment = snapshot.getValue(Comment.class);
                                                newComments.add(comment);
                                            }

                                            adapter.swapList(newComments);
                                            commentRecyclerView.smoothScrollToPosition(adapter.getItemCount());

                                        }

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
//                                .get()
//                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                                    @Override
//                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                                        ArrayList<Comment> newComments = new ArrayList<>();
//
//                                        if (task.isSuccessful()) {
//                                            for (QueryDocumentSnapshot document : task.getResult()) {
//                                                Comment comment = document.toObject(Comment.class);
//                                                newComments.add(comment);
//
//                                            }
//                                            adapter.swapList(newComments);
//                                            commentRecyclerView.smoothScrollToPosition(adapter.getItemCount());
//                                        }
//                                    }
//                                });

                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

//        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() { TODO: REMOVE COMMENT
//            @Override
//            public void onEvent(@Nullable DocumentSnapshot snapshot,
//                                @Nullable FirebaseFirestoreException e) {
//                if (e != null) {
//                    return;
//                }
//
//                if (snapshot != null && snapshot.exists()) {
//                    if(snapshot.getLong("interactionCount") > adapter.getItemCount()) {
//
//                        docRef.collection("comments")
//                                .whereGreaterThan("timestamp", adapter.lastTimestamp)
//                                .get()
//                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                                    @Override
//                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                                        ArrayList<Comment> newComments = new ArrayList<>();
//
//                                        if (task.isSuccessful()) {
//                                            for (QueryDocumentSnapshot document : task.getResult()) {
//                                                Comment comment = document.toObject(Comment.class);
//                                                newComments.add(comment);
//
//                                            }
//                                            adapter.swapList(newComments);
//                                            commentRecyclerView.smoothScrollToPosition(adapter.getItemCount());
//                                        }
//                                    }
//                                });
//                    }
//                } else {
//                }
//            }
//        });
    }

    private void getAllInteraction() {

        realtimeRef.child("users")
                .child(sharedPreferences.getString("userFireId", ""))
                .child("postInteractions")
                .child(postId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.exists()) {

                            PostInteraction postInteraction = dataSnapshot.getValue(PostInteraction.class);

                            switch (String.valueOf(postInteraction.like)) {
                                case "-1":
                                    dislikesImageView.setImageResource(R.drawable.ic_thumb_down_accent_28dp);
                                    likeCode = -1;
                                    break;
                                case "1":
                                    likesImageView.setImageResource(R.drawable.ic_thumb_up_accent_28dp);
                                    likeCode = 1;
                                    break;
                                default:
                                    break;
                            }

                            if (postInteraction.comment)
                                interactionImageView.setImageResource(R.drawable.ic_comment_accent_28dp);

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

//                .get()
//                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//
//                        if (task.isSuccessful()) {
//
//                            DocumentSnapshot document = task.getResult();
//
//                            if(document.exists()) {
//
//                                switch (String.valueOf(document.get("like"))) {
//                                    case "-1":
//                                        dislikesImageView.setImageResource(R.drawable.ic_thumb_down_accent_28dp);
//                                        likeCode = -1;
//                                        break;
//                                    case "1":
//                                        likesImageView.setImageResource(R.drawable.ic_thumb_up_accent_28dp);
//                                        likeCode = 1;
//                                        break;
//                                    default:
//                                        break;
//                                }
//
//                                    if (document.getBoolean("comment"))
//                                    interactionImageView.setImageResource(R.drawable.ic_comment_accent_28dp);
//
//                            }
//
//                        }
//
//                    }
//                });

    }

    private void createPostInteractions(int i1, int i2, String type) {

        Map<String, Object> setInteractions = new HashMap<>();
        setInteractions.put("like", 0);
        setInteractions.put("comment", false);

        realtimeRef.child("users")
                .child(sharedPreferences.getString("userFireId", ""))
                .child("postInteractions")
                .child(postId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.exists()) {

                            realtimeRef.child("users")
                                    .child(sharedPreferences.getString("userFireId", ""))
                                    .child("postInteractions")
                                    .child(postId)
                                    .setValue(setInteractions)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if(type.equals("comment"))
                                                addComment();
                                            else
                                                likeInteraction(i1,i2);
                                        }
                                    });

                        } else {

                            if(type.equals("comment"))
                                addComment();
                            else
                                likeInteraction(i1,i2);

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

//                .get()
//                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                        if(task.isSuccessful()) {
//                            DocumentSnapshot document = task.getResult();
//
//                            if(!document.exists()) {
//
//                                realtimeRef.collection("users")
//                                        .document(sharedPreferences.getString("userFireId", ""))
//                                        .collection("postInteractions")
//                                        .document(postId)
//                                        .set(setInteractions)
//                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                            @Override
//                                            public void onComplete(@NonNull Task<Void> task) {
//
//                                                if(type.equals("comment"))
//                                                    addComment();
//                                                else
//                                                    likeInteraction(i1,i2);
//                                            }
//                                        });
//
//                            } else {
//
//                                if(type.equals("comment"))
//                                    addComment();
//                                else
//                                    likeInteraction(i1,i2);
//
//                            }
//                        }
//                    }
//                });
    }



    public void addComment() {

        if (interstitialAd.isLoaded()) {
            interstitialAd.show();
        } else {
        }

        commentEntry = commentEditText.getText().toString();

        if(!commentEntry.equals("")) {

            DatabaseReference commentRef = realtimeRef.child("posts")
                    .child(postId)
                    .child("comments")
                    .child(String.valueOf(System.nanoTime()));

            DatabaseReference postInteractionRef = realtimeRef.child("posts")
                    .child(postId);

            DatabaseReference userInteractRef = realtimeRef.child("users")
                    .child(sharedPreferences.getString("userFireId", ""))
                    .child("postInteractions")
                    .child(postId);


//            DatabaseReference userRef = realtimeRef.child("users")      //TODO: IF BELOW WORKS
//                    .child(sharedPreferences.getString("userFireId", ""));


            DatabaseReference posterRef = realtimeRef.child("users")
                    .child(postUserId);

            Map<String, Object> comments = new HashMap<>();
            comments.put("comment", commentEntry);
            comments.put("userRef", sharedPreferences.getString("userFireId", ""));
            comments.put("timestamp", System.currentTimeMillis());
            commentEditText.setText("");

            commentRef.setValue(comments);

            Map<String, Object> comment = new HashMap<>();
            comment.put("comment", true);

            userInteractRef.updateChildren(comment);

            postInteractionRef
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if(dataSnapshot.exists()) {
                                Post post = dataSnapshot.getValue(Post.class);

                                Map<String, Object> interaction = new HashMap<>();
                                interaction.put("interactionCount", post.interactionCount + 1);

                                postInteractionRef.updateChildren(interaction);

                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

            posterRef
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if(dataSnapshot.exists()) {
                                User user = dataSnapshot.getValue(User.class);

                                Map<String, Object> comment = new HashMap<>();
                                comment.put("comments", user.comments + 1);

                                posterRef.updateChildren(comment);

                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

//            userInteractRef TODO: Remove if above is working
//                    .get()
//                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                        @Override
//                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                            if(task.isSuccessful()) {
//
//                                interactionCountTextView.setText(String.valueOf(Integer.valueOf(interactionCountTextView.getText().toString()) + 1));
//                                interactionImageView.setImageResource(R.drawable.ic_comment_accent_28dp);
//
//                                realtimeRef.runTransaction(new Transaction.Function<Void>() {
//                                    @Override
//                                    public Void apply(Transaction transaction) throws FirebaseFirestoreException {
//
//                                        DocumentSnapshot snapshot1 = transaction.get(postInteractionRef);
//                                        DocumentSnapshot snapshot2 = transaction.get(posterRef);
//
//                                        int newPostInteract = (int) (snapshot1.getLong("interactionCount") + 1);
//                                        int newPosterInteract = (int) (snapshot2.getLong("comments") + 1);
//                                        transaction.update(posterRef, "comments", newPosterInteract);
//                                        transaction.update(postInteractionRef, "interactionCount", newPostInteract);
//                                        transaction.update(userInteractRef, "comment", true);
//                                        transaction.set(commentRef, comments);
//
//                                        return null;
//                                    }
//                                }).addOnSuccessListener(new OnSuccessListener<Void>() {
//                                    @Override
//                                    public void onSuccess(Void aVoid) {
//
//                                    }
//                                })
//                                        .addOnFailureListener(new OnFailureListener() {
//                                            @Override
//                                            public void onFailure(@NonNull Exception e) {
//
//                                            }
//                                        });
//
//                            }
//                        }
//                    });

        }
    }

    public void loadAdapter(ArrayList<Comment> commentArrayList) {

        adapter = new CommentAdapter(commentArrayList);
        commentRecyclerView.setAdapter(adapter);
        newCommentListener();

    }

    private void setLikeClickListeners() {
        likesImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (likeCode) {
                    case -1: likeCode = 1;
                        likesImageView.setImageResource(R.drawable.ic_thumb_up_accent_28dp);
                        likesCountTextView.setText(String.valueOf(Integer.valueOf(likesCountTextView.getText().toString()) + 1));
                        dislikesImageView.setImageResource(R.drawable.ic_thumb_down_primary_28dp);
                        dislikesCountTextView.setText(String.valueOf(Integer.valueOf(dislikesCountTextView.getText().toString()) - 1));
                        createPostInteractions(1,-1,"like");
                        break;
                    case 1: likeCode = 0;
                        likesImageView.setImageResource(R.drawable.ic_thumb_up_primary_28dp);
                        likesCountTextView.setText(String.valueOf(Integer.valueOf(likesCountTextView.getText().toString()) - 1));
                        createPostInteractions(-1, 0, "like");
                        break;
                    case 0: likeCode = 1;
                        likesImageView.setImageResource(R.drawable.ic_thumb_up_accent_28dp);
                        likesCountTextView.setText(String.valueOf(Integer.valueOf(likesCountTextView.getText().toString()) + 1));
                        createPostInteractions(1, 0, "like");
                        break;
                }
            }
        });
        dislikesImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (likeCode) {
                    case -1: likeCode = 0;
                        dislikesImageView.setImageResource(R.drawable.ic_thumb_down_primary_28dp);
                        dislikesCountTextView.setText(String.valueOf(Integer.valueOf(dislikesCountTextView.getText().toString()) - 1));
                        createPostInteractions(0, -1, "like");
                        break;
                    case 1: likeCode = -1;
                        dislikesImageView.setImageResource(R.drawable.ic_thumb_down_accent_28dp);
                        dislikesCountTextView.setText(String.valueOf(Integer.valueOf(dislikesCountTextView.getText().toString()) + 1));
                        likesImageView.setImageResource(R.drawable.ic_thumb_up_primary_28dp);
                        likesCountTextView.setText(String.valueOf(Integer.valueOf(likesCountTextView.getText().toString()) - 1));
                        createPostInteractions(-1, 1, "like");
                        break;
                    case 0: likeCode = -1;
                        dislikesImageView.setImageResource(R.drawable.ic_thumb_down_accent_28dp);
                        dislikesCountTextView.setText(String.valueOf(Integer.valueOf(dislikesCountTextView.getText().toString()) + 1));
                        createPostInteractions(0, 1, "like");
                        break;
                }
            }
        });
    }


    private void likeInteraction(int like, int dislike) {

        Map<String, Object> setInteractions = new HashMap<>();
        setInteractions.put("like", 0);
        setInteractions.put("comment", false);

        DatabaseReference postInteractionRef = realtimeRef.child("posts")
                .child(postId);

        DatabaseReference userInteractRef = realtimeRef.child("users")
                .child(sharedPreferences.getString("userFireId", ""))
                .child("postInteractions")
                .child(postId);

        DatabaseReference userRef = realtimeRef.child("users")
                .child(postUserId);

        Map<String, Object> code = new HashMap<>();
        code.put("like", likeCode);

        userInteractRef.updateChildren(code);

        postInteractionRef
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.exists()) {
                            Post post = dataSnapshot.getValue(Post.class);

                            Map<String, Object> likes = new HashMap<>();
                            likes.put("likes", post.likes + like);
                            likes.put("dislikes", post.dislikes + dislike);

                            postInteractionRef.updateChildren(likes);

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        userRef
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.exists()) {
                            User user = dataSnapshot.getValue(User.class);

                            Map<String, Object> likes = new HashMap<>();
                            likes.put("likes", user.likes + like);
                            likes.put("dislikes", user.dislikes + dislike);

                            userRef.updateChildren(likes);

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

//        userInteractRef
//                .get()
//                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                        if(task.isSuccessful()) {
//
//                            realtimeRef.runTransaction(new Transaction.Function<Void>() {
//                                @Override
//                                public Void apply(Transaction transaction) throws FirebaseFirestoreException {
//                                    DocumentSnapshot snapshot1 = transaction.get(userRef);
//                                    DocumentSnapshot snapshot2 = transaction.get(postInteractionRef);
//
//                                    int newUserLikes = (int) (snapshot1.getLong("likes") + like);
//                                    int newUserDislikes = (int) (snapshot1.getLong("dislikes") + dislike);
//                                    transaction.update(userRef, "likes", newUserLikes);
//                                    transaction.update(userRef, "dislikes", newUserDislikes);
//
//
//                                    transaction.update(userInteractRef, "like", likeCode);
//
//                                    int newPostLikes = (int) (snapshot2.getLong("likes") + like);
//                                    int newPostDislikes = (int) (snapshot2.getLong("dislikes") + dislike);
//                                    transaction.update(postInteractionRef, "likes", newPostLikes);
//                                    transaction.update(postInteractionRef, "dislikes", newPostDislikes);
//
//                                    return null;
//                                }
//                            }).addOnSuccessListener(new OnSuccessListener<Void>() {
//                                @Override
//                                public void onSuccess(Void aVoid) {
//                                }
//                            })
//                                    .addOnFailureListener(new OnFailureListener() {
//                                        @Override
//                                        public void onFailure(@NonNull Exception e) {
//                                        }
//                                    });
//
//                        }
//                    }
//                });

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void deletePost() {

        DatabaseReference docRef = realtimeRef.child("posts")
                .child(postId);

        docRef
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
    }
}
