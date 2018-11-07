package com.apps.creativesource.censio;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import worker8.com.github.radiogroupplus.RadioGroupPlus;

public class ChoiceDetailFragment extends Fragment {
    private ArrayList<Comment> commentArrayList = new ArrayList<>();
    private TextView statementTextView;
    private TextView usernameTextView;
    private TextView interactionCountTextView;
    private TextView likesCountTextView;
    private TextView dislikesCountTextView;
    private CircleImageView circleImageView;
    private ImageView interactionImageView;
    private ImageView likesImageView;
    private ImageView dislikesImageView;
    private FloatingActionButton fab;
    private RadioGroupPlus choicesRadioGroup;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private FirebaseFirestore firestore;
    private String postId;
    private String postUserId;
    private String chosenRadioButton;
    private SQLiteDatabase db;
    private int likeCode = 0;
    private boolean userPost;

    public ChoiceDetailFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_choice_detail, container, false);

//        ActionBar actionBar = getSupportActionBar();

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
        choicesRadioGroup = view.findViewById(R.id.rg_choices);
        fab = view.findViewById(R.id.fab_delete);

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

        if(userPost && getActivity().findViewById(R.id.detail_container) == null)
            fab.hide();

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
//                        deletePost(postId);                                                        Todo: Fix delete post implementation
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

    private void getAllChoices() {

        //TODO: FIX POST USER IMPLEMENTATION

        firestore.collection("posts")
                .document(postId)
                .collection("choices")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                createRadioButton(document);

                            }
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

                                chosenRadioButton = document.getString("choice");

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


                                if(!chosenRadioButton.equals("null"))
                                    interactionImageView.setImageResource(R.drawable.ic_touch_app_accent_28dp);

                            }

                        }

                        getAllChoices();
                    }
                });

    }

    private void likeInteraction(int like, int dislike) {

        Map<String, Object> setInteractions = new HashMap<>();
        setInteractions.put("like", 0);
        setInteractions.put("choice", null);

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

    private void createPostInteractions(int i1, int i2, String type) {

        Map<String, Object> setInteractions = new HashMap<>();
        setInteractions.put("like", 0);
        setInteractions.put("choice", "null");

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

                                                if(type.equals("choice"))
                                                    makechoice();
                                                else
                                                    likeInteraction(i1,i2);
                                            }
                                        });

                            } else {

                                if(type.equals("choice"))
                                    makechoice();
                                else
                                    likeInteraction(i1,i2);

                            }
                        }
                    }
                });
    }

    private void makechoice() {

//        DocumentReference choiceCountRef = firestore.collection("posts")
//                .document(postId)
//                .collection("choices")
//                .document();

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

                            firestore.collection("posts")
                                    .document(postId)
                                    .collection("choices")
                                    .whereEqualTo("title",document.getString("choice"))
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                for (QueryDocumentSnapshot document : task.getResult()) {

                                                    DocumentReference choiceCountRef = firestore.collection("posts")
                                                            .document(postId)
                                                            .collection("choices")
                                                            .document(document.getId());

                                                    firestore.runTransaction(new Transaction.Function<Void>() {
                                                        @Override
                                                        public Void apply(Transaction transaction) throws FirebaseFirestoreException {

                                                            DocumentSnapshot snapshot1 = transaction.get(choiceCountRef);

                                                            int newChoiceCount = (int) (snapshot1.getLong("count") - 1);
                                                            transaction.update(choiceCountRef, "count", newChoiceCount);

                                                            return null;
                                                        }
                                                    }).addOnSuccessListener(new OnSuccessListener<Void>() {
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
                                        }
                                    });
                        }
                    }
                });

        firestore.collection("posts")
                .document(postId)
                .collection("choices")
                .whereEqualTo("title", chosenRadioButton)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                DocumentReference choiceCountRef = firestore.collection("posts")
                                        .document(postId)
                                        .collection("choices")
                                        .document(document.getId());

                                DocumentReference postInteractionRef = firestore.collection("posts")
                                        .document(postId);

                                DocumentReference userInteractRef = firestore.collection("users")
                                        .document(sharedPreferences.getString("userFireId", ""))
                                        .collection("postInteractions")
                                        .document(postId);

                                DocumentReference posterRef = firestore.collection("users")
                                        .document(postUserId);

                                firestore.runTransaction(new Transaction.Function<Void>() {
                                    @Override
                                    public Void apply(Transaction transaction) throws FirebaseFirestoreException {

                                        DocumentSnapshot snapshot3 = transaction.get(userInteractRef);
                                        DocumentSnapshot snapshot1 = transaction.get(choiceCountRef);


                                        if(snapshot3.getString("choice").equals("null")) {
                                            DocumentSnapshot snapshot2 = transaction.get(postInteractionRef);
                                            DocumentSnapshot snapshot4 = transaction.get(posterRef);

                                            interactionCountTextView.setText(String.valueOf(Integer.valueOf(interactionCountTextView.getText().toString()) + 1));
                                            interactionImageView.setImageResource(R.drawable.ic_touch_app_accent_28dp);

                                            int newPosterInteract = (int) (snapshot4.getLong("votes") + 1);
                                            int newPostInteract = (int) (snapshot2.getLong("interactionCount") + 1);
                                            transaction.update(posterRef, "votes", newPosterInteract);
                                            transaction.update(postInteractionRef, "interactionCount", newPostInteract);

                                        }

                                        int newChoiceCount = (int) (snapshot1.getLong("count") + 1);
                                        transaction.update(choiceCountRef, "count", newChoiceCount);

                                        transaction.update(userInteractRef, "choice", chosenRadioButton);

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
                    }
                });

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

    private void createRadioButton(QueryDocumentSnapshot document) {
        View radioButtonLayout = getLayoutInflater().inflate(R.layout.radio_button, null);
        ConstraintLayout constraintLayout = radioButtonLayout.findViewById(R.id.cl_option);
        RadioButton radioButton = radioButtonLayout.findViewById(R.id.rb_choice);
        TextView textView = radioButtonLayout.findViewById(R.id.tv_percent);
        if(constraintLayout.getParent()!=null)
            ((ViewGroup)constraintLayout.getParent()).removeView(constraintLayout);

        radioButton.setText(document.getString("title"));
        radioButton.setId((int) System.currentTimeMillis());
        textView.setText(String.valueOf((int)(document.getLong("count") / Float.valueOf(interactionCountTextView.getText().toString()) * 100)) + "%");

        choicesRadioGroup.addView(constraintLayout);
        choicesRadioGroup.setOnCheckedChangeListener(new RadioGroupPlus.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroupPlus radioGroupPlus, int i) {
                RadioButton rb = radioGroupPlus.findViewById(i);
                chosenRadioButton = rb.getText().toString();
                createPostInteractions(0,0,"choice");
            }

//            @Override
//            public void onCheckedChanged(RadioGroup group, int checkedId) {
//                RadioButton rb = group.findViewById(checkedId);
//                chosenRadioButton = rb.getText().toString();
//                makechoice();
//            }
        });

        if(document.getString("title").equals(chosenRadioButton))
            radioButton.setChecked(true);
    }

//    private boolean deletePost(long id) {                                                           Todo: Fix delete post implementation
//        return db.delete(CensioContract.Posts.TABLE_NAME,
//                CensioContract.Posts._ID + "=" + id, null) > 0;
//    }
}
