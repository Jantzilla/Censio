package com.apps.creativesource.censio;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;

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
    private SharedPreferences.Editor editor;
    private FirebaseFirestore firestore;
    private String postId;
    private SQLiteDatabase db;
    private int likeCode = 0;
    private String postUserId;
    private boolean userPost;

    private InterstitialAd interstitialAd;

    public CommentDetailFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comment_detail, container, false);

//        ActionBar actionBar = getgetSupportActionBar();

        MobileAds.initialize(getContext(), "ca-app-pub-3940256099942544~3347511713");

        interstitialAd = new InterstitialAd(getContext());
        interstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        interstitialAd.loadAd(new AdRequest.Builder().build());

        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                // Load the next interstitial.
                interstitialAd.loadAd(new AdRequest.Builder().build());
            }

        });

        CensioDbHelper dbHelper = new CensioDbHelper(getContext());
        db = dbHelper.getWritableDatabase();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        editor = sharedPreferences.edit();

        firestore = FirebaseFirestore.getInstance();

//        if(actionBar != null) {
//            actionBar.setDisplayHomeAsUpEnabled(true);
//        }

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
            interactionImageView.setImageResource(initialIntent.getIntExtra("postTypeId", R.drawable.ic_touch_app_white_28dp));
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
            interactionImageView.setImageResource(getArguments().getInt("postTypeId", R.drawable.ic_touch_app_white_28dp));
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
                builder.setTitle("Delete this post?");
                builder.setCancelable(true);
                builder.setPositiveButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
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

        //TODO: FIX POST USER IMPLEMENTATION

        firestore.collection("posts")
                .document(postId)
                .collection("comments")
                .orderBy("timestamp", Query.Direction.ASCENDING)        //Todo: Add "timestamp" and orderBy that
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Comment comment = document.toObject(Comment.class);
                                commentArrayList.add(comment);

                            }
                            loadAdapter(commentArrayList);
                        }
                    }
                });
    }

    private void newCommentListener() {

        DocumentReference docRef = firestore.collection("posts")
                .document(postId);

        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
//                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
//                    Log.d(TAG, "Current data: " + snapshot.getData());
                    if(snapshot.getLong("interactionCount") > adapter.getItemCount()) {

                        docRef.collection("comments")
                                .whereGreaterThan("timestamp", adapter.lastTimestamp)
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        ArrayList<Comment> newComments = new ArrayList<>();

                                        if (task.isSuccessful()) {
                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                Comment comment = document.toObject(Comment.class);
                                                newComments.add(comment);

                                            }
                                            adapter.swapList(newComments);
                                            commentRecyclerView.smoothScrollToPosition(adapter.getItemCount());
                                        }
                                    }
                                });
                    }
                } else {
//                    Log.d(TAG, "Current data: null");
                }
            }
        });
    }

    private void getAllInteraction() {

        firestore.collection("users")
                .document(sharedPreferences.getString("userFireId", ""))
                .collection("postInteractions")
                .document(postId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if (task.isSuccessful()) {

                            DocumentSnapshot document = task.getResult();

                            if(document.exists()) {

                                switch (String.valueOf(document.get("like"))) {
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

                                    if (document.getBoolean("comment"))
                                    interactionImageView.setImageResource(R.drawable.ic_comment_accent_28dp);

                            }

                        }

                    }
                });

    }

    private void createPostInteractions(int i1, int i2, String type) {

        Map<String, Object> setInteractions = new HashMap<>();
        setInteractions.put("like", 0);
        setInteractions.put("comment", false);

        firestore.collection("users")
                .document(sharedPreferences.getString("userFireId", ""))
                .collection("postInteractions")
                .document(postId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();

                            if(!document.exists()) {

                                firestore.collection("users")
                                        .document(sharedPreferences.getString("userFireId", ""))
                                        .collection("postInteractions")
                                        .document(postId)
                                        .set(setInteractions)
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
                    }
                });
    }



    public void addComment() {

//        Comment comment = new Comment();

        if (interstitialAd.isLoaded()) {
            interstitialAd.show();
        } else {
            Log.d("TAG", "The interstitial wasn't loaded yet.");
        }

        commentEntry = commentEditText.getText().toString();

        if(!commentEntry.equals("")) {

            DocumentReference commentRef = firestore.collection("posts")
                    .document(postId)
                    .collection("comments")
                    .document(String.valueOf(System.nanoTime()));

            DocumentReference postInteractionRef = firestore.collection("posts")
                    .document(postId);

            DocumentReference userInteractRef = firestore.collection("users")
                    .document(sharedPreferences.getString("userFireId", ""))
                    .collection("postInteractions")
                    .document(postId);


            DocumentReference userRef = firestore.collection("users")
                    .document(sharedPreferences.getString("userFireId", ""));


            DocumentReference posterRef = firestore.collection("users")
                    .document(postUserId);

            Map<String, Object> comments = new HashMap<>();                    //TODO: FIX THIS FIRESTORE USER COMMENT IMPLEMENTATION
            comments.put("comment", commentEntry);
            comments.put("userRef", userRef);
            comments.put("timestamp", System.currentTimeMillis());
            commentEditText.setText("");

            // Add a new document with a generated ID

            userInteractRef
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful()) {

                                interactionCountTextView.setText(String.valueOf(Integer.valueOf(interactionCountTextView.getText().toString()) + 1));
                                interactionImageView.setImageResource(R.drawable.ic_comment_accent_28dp);

                                firestore.runTransaction(new Transaction.Function<Void>() {
                                    @Override
                                    public Void apply(Transaction transaction) throws FirebaseFirestoreException {

                                        DocumentSnapshot snapshot1 = transaction.get(postInteractionRef);
                                        DocumentSnapshot snapshot2 = transaction.get(posterRef);

                                        int newPostInteract = (int) (snapshot1.getLong("interactionCount") + 1);
                                        int newPosterInteract = (int) (snapshot2.getLong("comments") + 1);
                                        transaction.update(posterRef, "comments", newPosterInteract);
                                        transaction.update(postInteractionRef, "interactionCount", newPostInteract);
                                        transaction.update(userInteractRef, "comment", true);
                                        transaction.set(commentRef, comments);

                                        return null;
                                    }
                                }).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

//                                        comment.comment = commentEntry;
//                                        comment.userRef = userRef;
//                                        ArrayList<Comment> newComments = new ArrayList<>();
//
//                                        newComments.add(comment);
////                                        newComments.addAll(commentArrayList);
////                                        commentArrayList = newComments;
//
//                                        adapter.swapList(newComments);

                                    }
                                })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {

                                            }
                                        });

                            }
                        }
                    });

//            firestore.collection("posts")
//                    .document(postId)
//                    .collection("comments")
//                    .add(comments)
//                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
//                        @Override
//                        public void onSuccess(DocumentReference documentReference) {
//
//                        }
//                    })
//                    .addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//                            Log.w("Firestore Log", "Error adding document", e);
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
                Map<String, Object> choice = new HashMap<>();
                switch (likeCode) {
                    case -1: likeCode = 1;
                        likesImageView.setImageResource(R.drawable.ic_thumb_up_accent_28dp);
                        likesCountTextView.setText(String.valueOf(Integer.valueOf(likesCountTextView.getText().toString()) + 1));
                        dislikesImageView.setImageResource(R.drawable.ic_thumb_down_white_28dp);
                        dislikesCountTextView.setText(String.valueOf(Integer.valueOf(dislikesCountTextView.getText().toString()) - 1));
                        createPostInteractions(1,-1,"like");
//                    likeInteraction(1, -1);
                        break;
                    case 1: likeCode = 0;
                        likesImageView.setImageResource(R.drawable.ic_thumb_up_white_28dp);
                        likesCountTextView.setText(String.valueOf(Integer.valueOf(likesCountTextView.getText().toString()) - 1));
                        createPostInteractions(-1, 0, "like");
//                    likeInteraction(-1, 0);
                        break;
                    case 0: likeCode = 1;
                        likesImageView.setImageResource(R.drawable.ic_thumb_up_accent_28dp);
                        likesCountTextView.setText(String.valueOf(Integer.valueOf(likesCountTextView.getText().toString()) + 1));
                        createPostInteractions(1, 0, "like");
//                    likeInteraction(1, 0);
                        break;
                }
            }
        });
        dislikesImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Object> choice = new HashMap<>();
                switch (likeCode) {
                    case -1: likeCode = 0;
                        dislikesImageView.setImageResource(R.drawable.ic_thumb_down_white_28dp);
                        dislikesCountTextView.setText(String.valueOf(Integer.valueOf(dislikesCountTextView.getText().toString()) - 1));
                        createPostInteractions(0, -1, "like");
//                    likeInteraction(0, -1);
                        break;
                    case 1: likeCode = -1;
                        dislikesImageView.setImageResource(R.drawable.ic_thumb_down_accent_28dp);
                        dislikesCountTextView.setText(String.valueOf(Integer.valueOf(dislikesCountTextView.getText().toString()) + 1));
                        likesImageView.setImageResource(R.drawable.ic_thumb_up_white_28dp);
                        likesCountTextView.setText(String.valueOf(Integer.valueOf(likesCountTextView.getText().toString()) - 1));
                        createPostInteractions(-1, 1, "like");
//                    likeInteraction(-1, 1);
                        break;
                    case 0: likeCode = -1;
                        dislikesImageView.setImageResource(R.drawable.ic_thumb_down_accent_28dp);
                        dislikesCountTextView.setText(String.valueOf(Integer.valueOf(dislikesCountTextView.getText().toString()) + 1));
                        createPostInteractions(0, 1, "like");
//                    likeInteraction(0, 1);
                        break;
                }
            }
        });
    }


    private void likeInteraction(int like, int dislike) {

        Map<String, Object> setInteractions = new HashMap<>();
        setInteractions.put("like", 0);
        setInteractions.put("comment", false);

        DocumentReference postInteractionRef = firestore.collection("posts")
                .document(postId);

        DocumentReference userInteractRef = firestore.collection("users")
                .document(sharedPreferences.getString("userFireId", ""))
                .collection("postInteractions")
                .document(postId);

        DocumentReference userRef = firestore.collection("users")
                .document(postUserId);

        userInteractRef
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()) {

                            firestore.runTransaction(new Transaction.Function<Void>() {
                                @Override
                                public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                                    DocumentSnapshot snapshot1 = transaction.get(userRef);
                                    DocumentSnapshot snapshot2 = transaction.get(postInteractionRef);

                                    int newUserLikes = (int) (snapshot1.getLong("likes") + like);
                                    int newUserDislikes = (int) (snapshot1.getLong("dislikes") + dislike);
                                    transaction.update(userRef, "likes", newUserLikes);
                                    transaction.update(userRef, "dislikes", newUserDislikes);


                                    transaction.update(userInteractRef, "like", likeCode);

                                    int newPostLikes = (int) (snapshot2.getLong("likes") + like);
                                    int newPostDislikes = (int) (snapshot2.getLong("dislikes") + dislike);
                                    transaction.update(postInteractionRef, "likes", newPostLikes);
                                    transaction.update(postInteractionRef, "dislikes", newPostDislikes);

                                    // Success
                                    return null;
                                }
                            }).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
//                Log.d(TAG, "Transaction success!");
                                }
                            })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
//                        Log.w(TAG, "Transaction failure.", e);
                                        }
                                    });

                        }
                    }
                });

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void deletePost() {                                                        //   Todo: Fix delete post implementation

        DocumentReference docRef = firestore.collection("posts")
                .document(postId);

        docRef
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
//                        Log.d("Successful Delete", "DocumentSnapshot successfully deleted!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
//                       Log.w("Delete Failed", "Error deleting document", e);
                    }
                });
    }
}
