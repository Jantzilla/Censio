package com.apps.creativesource.censio;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
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
    private CommentAdapter adapter;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private FirebaseFirestore firestore;
    private String postId;
    private SQLiteDatabase db;
    private int likeCode = 0;
    private String postUserId;
    private boolean userPost;

    public CommentDetailFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comment_detail, container, false);

//        ActionBar actionBar = getgetSupportActionBar();

        CensioDbHelper dbHelper = new CensioDbHelper(getContext());
        db = dbHelper.getWritableDatabase();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        editor = sharedPreferences.edit();

        firestore = FirebaseFirestore.getInstance();

//        if(actionBar != null) {
//            actionBar.setDisplayHomeAsUpEnabled(true);
//        }

        circleImageView = view.findViewById(R.id.iv_profile);
        interactionImageView = view.findViewById(R.id.iv_interaction);
        likesImageView = view.findViewById(R.id.iv_likes);
        dislikesImageView = view.findViewById(R.id.iv_dislikes);
        statementTextView = view.findViewById(R.id.tv_statement);
        usernameTextView = view.findViewById(R.id.tv_username);
        interactionCountTextView = view.findViewById(R.id.tv_interaction_count);
        likesCountTextView = view.findViewById(R.id.tv_likes_count);
        dislikesCountTextView = view.findViewById(R.id.tv_dislikes_count);

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

        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
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

        getAllInteraction();
        setLikeClickListeners();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if(userPost)
            inflater.inflate(R.menu.post, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                //Todo: do something
            break;
            case R.id.it_delete:
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
//                        deletePost(postId);                                                        Todo: Fix delete post implementation
                        Intent homeIntent = new Intent(getContext(), MainActivity.class);
                        startActivity(homeIntent);
                        getActivity().finish();
                        dialog.cancel();
                    }
                });
                builder.show();
            break;
            default:
                //Todo: do something else else
                break;
        }

        if(id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(getActivity());
        }
        return super.onOptionsItemSelected(item);
    }

    private void getAllComments() {

        //TODO: FIX POST USER IMPLEMENTATION

        firestore.collection("posts")
                .document(postId)
                .collection("comments")
                .orderBy("timestamp", Query.Direction.DESCENDING)        //Todo: Add "timestamp" and orderBy that
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

        Comment comment = new Comment();

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

                                        comment.comment = commentEntry;
                                        comment.userRef = userRef;
                                        ArrayList<Comment> newComments = new ArrayList<>();

                                        newComments.add(comment);
                                        newComments.addAll(commentArrayList);
                                        commentArrayList = newComments;

                                        adapter.swapList(commentArrayList);

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


//    private boolean deletePost(long id) {                                                           Todo: Fix delete post implementation
//        return db.delete(CensioContract.Posts.TABLE_NAME,
//                CensioContract.Posts._ID + "=" + id, null) > 0;
//    }
}
