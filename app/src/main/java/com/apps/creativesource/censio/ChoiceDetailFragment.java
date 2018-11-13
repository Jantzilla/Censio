package com.apps.creativesource.censio;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import worker8.com.github.radiogroupplus.RadioGroupPlus;

public class ChoiceDetailFragment extends Fragment {
    private ArrayList<ListenerRegistration> registrations = new ArrayList<>();
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
    private DatabaseReference realtimeRef;
    private String postId;
    private String postUserId;
    private String chosenRadioButton;
    private int likeCode = 0;
    private boolean userPost;
    private InterstitialAd interstitialAd;

    public ChoiceDetailFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_choice_detail, container, false);

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

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(R.string.delete_post_confirm);
                builder.setCancelable(true);
                builder.setPositiveButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.setNegativeButton(R.string.yes, new DialogInterface.OnClickListener() {
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

    @Override
    public void onStop() {
        super.onStop();

        for(int i = 0; i < registrations.size(); i++) {
            registrations.get(0).remove();
        }
    }



    private void getAllChoices() {

        realtimeRef.child("posts")
                .child(postId)
                .child("choices")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Choice choice = snapshot.getValue(Choice.class);          //TODO: CREATE OBJECT
                            createRadioButton(choice, dataSnapshot.getRef());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void getAllInteraction() {

        realtimeRef.child("users")
                .child(sharedPreferences.getString("userFireId", ""))
                .child("postInteractions")
                .child(postId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(dataSnapshot.exists()) {
                            PostInteraction postInteraction = dataSnapshot.getValue(PostInteraction.class);

                            chosenRadioButton = postInteraction.choice;

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


                            if(!chosenRadioButton.equals("null"))
                                interactionImageView.setImageResource(R.drawable.ic_touch_app_accent_28dp);

                        }


                    getAllChoices();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    private void likeInteraction(int like, int dislike) {

        Map<String, Object> setInteractions = new HashMap<>();
        setInteractions.put("like", 0);
        setInteractions.put("choice", null);

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

        userInteractRef.setValue(code);

        postInteractionRef
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.exists()) {
                            Post post = dataSnapshot.getValue(Post.class);

                            Map<String, Object> likes = new HashMap<>();
                            likes.put("likes", post.likes + like);
                            likes.put("dislikes", post.likes + dislike);

                            postInteractionRef.setValue(likes);

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

                            userRef.setValue(likes);

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

    private void createPostInteractions(int i1, int i2, String type) {

        Map<String, Object> setInteractions = new HashMap<>();
        setInteractions.put("like", 0);
        setInteractions.put("choice", "null");

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
//                                realtimeRef.child("users")
//                                        .child(sharedPreferences.getString("userFireId", ""))
//                                        .child("postInteractions")
//                                        .child(postId)
//                                        .set(setInteractions)
//                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                            @Override
//                                            public void onComplete(@NonNull Task<Void> task) {
//
//                                                if(type.equals("choice"))
//                                                    makechoice();
//                                                else
//                                                    likeInteraction(i1,i2);
//                                            }
//                                        });
//
//                            } else {
//
//                                if(type.equals("choice"))
//                                    makechoice();
//                                else
//                                    likeInteraction(i1,i2);
//
//                            }
//                        }
//                    }
//                });
    }

    private void makechoice() {

        if (interstitialAd.isLoaded()) {
            interstitialAd.show();
        } else {
        }

        realtimeRef.child("users")
                .child(sharedPreferences.getString("userFireId", ""))
                .child("postInteractions")
                .child(postId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.exists()) {
                            PostInteraction postInteraction = dataSnapshot.getValue(PostInteraction.class);

                            realtimeRef.child("posts")
                                    .child(postId)
                                    .child("choices")
                                    .equalTo("title",postInteraction.choice)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                            if(dataSnapshot.exists()) {

                                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                    Choice choice = snapshot.getValue(Choice.class);

                                                    DatabaseReference choiceCountRef = realtimeRef.child("posts")
                                                            .child(postId)
                                                            .child("choices")
                                                            .child(snapshot.getKey());

                                                    choiceCountRef
                                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                                    if(dataSnapshot.exists()) {
                                                                        Choice choice = dataSnapshot.getValue(Choice.class);
                                                                        Map<String, Object> choiceMap = new HashMap<>();
                                                                        choiceMap.put("like", choice.count - 1);
                                                                        choiceCountRef.setValue(choiceMap);
                                                                    }

                                                                }

                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                }
                                                            });

//                                                    realtimeRef.runTransaction(new Transaction.Function<Void>() {
//                                                        @Override
//                                                        public Void apply(Transaction transaction) throws FirebaseFirestoreException {
//
//                                                            DocumentSnapshot snapshot1 = transaction.get(choiceCountRef);
//
//                                                            int newChoiceCount = (int) (snapshot1.getLong("count") - 1);
//                                                            transaction.update(choiceCountRef, "count", newChoiceCount);
//
//                                                            return null;
//                                                        }
//                                                    }).addOnSuccessListener(new OnSuccessListener<Void>() {
//                                                        @Override
//                                                        public void onSuccess(Void aVoid) {
//
//                                                        }
//                                                    })
//                                                            .addOnFailureListener(new OnFailureListener() {
//                                                                @Override
//                                                                public void onFailure(@NonNull Exception e) {
//
//                                                                }
//                                                            });

                                                }

                                            }

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });

//                                    .get()
//                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                                        @Override
//                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                                            if (task.isSuccessful()) {
//                                                for (QueryDocumentSnapshot document : task.getResult()) {
//
//                                                    DatabaseReference choiceCountRef = realtimeRef.child("posts")
//                                                            .child(postId)
//                                                            .child("choices")
//                                                            .child(document.getId());
//
//                                                    realtimeRef.runTransaction(new Transaction.Function<Void>() {
//                                                        @Override
//                                                        public Void apply(Transaction transaction) throws FirebaseFirestoreException {
//
//                                                            DocumentSnapshot snapshot1 = transaction.get(choiceCountRef);
//
//                                                            int newChoiceCount = (int) (snapshot1.getLong("count") - 1);
//                                                            transaction.update(choiceCountRef, "count", newChoiceCount);
//
//                                                            return null;
//                                                        }
//                                                    }).addOnSuccessListener(new OnSuccessListener<Void>() {
//                                                        @Override
//                                                        public void onSuccess(Void aVoid) {
//
//                                                        }
//                                                    })
//                                                            .addOnFailureListener(new OnFailureListener() {
//                                                                @Override
//                                                                public void onFailure(@NonNull Exception e) {
//
//                                                                }
//                                                            });
//
//                                                }
//                                            }
//                                        }
//                                    });

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
//                        if (task.isSuccessful()) {
//                            DocumentSnapshot document = task.getResult();
//
//                            realtimeRef.child("posts")
//                                    .child(postId)
//                                    .child("choices")
//                                    .equalTo("title",document.getString("choice"))
//                                    .get()
//                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                                        @Override
//                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                                            if (task.isSuccessful()) {
//                                                for (QueryDocumentSnapshot document : task.getResult()) {
//
//                                                    DatabaseReference choiceCountRef = realtimeRef.child("posts")
//                                                            .child(postId)
//                                                            .child("choices")
//                                                            .child(document.getId());
//
//                                                    realtimeRef.runTransaction(new Transaction.Function<Void>() {
//                                                        @Override
//                                                        public Void apply(Transaction transaction) throws FirebaseFirestoreException {
//
//                                                            DocumentSnapshot snapshot1 = transaction.get(choiceCountRef);
//
//                                                            int newChoiceCount = (int) (snapshot1.getLong("count") - 1);
//                                                            transaction.update(choiceCountRef, "count", newChoiceCount);
//
//                                                            return null;
//                                                        }
//                                                    }).addOnSuccessListener(new OnSuccessListener<Void>() {
//                                                        @Override
//                                                        public void onSuccess(Void aVoid) {
//
//                                                        }
//                                                    })
//                                                            .addOnFailureListener(new OnFailureListener() {
//                                                                @Override
//                                                                public void onFailure(@NonNull Exception e) {
//
//                                                                }
//                                                            });
//
//                                                }
//                                            }
//                                        }
//                                    });
//                        }
//                    }
//                });

        realtimeRef.child("posts")
                .child(postId)
                .child("choices")
                .equalTo("title", chosenRadioButton)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.exists()) {

                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                Choice choice = snapshot.getValue(Choice.class);

                                DatabaseReference choiceCountRef = realtimeRef.child("posts")
                                        .child(postId)
                                        .child("choices")
                                        .child(snapshot.getKey());

                                DatabaseReference postInteractionRef = realtimeRef.child("posts")
                                        .child(postId);

                                DatabaseReference userInteractRef = realtimeRef.child("users")
                                        .child(sharedPreferences.getString("userFireId", ""))
                                        .child("postInteractions")
                                        .child(postId);

                                DatabaseReference posterRef = realtimeRef.child("users")
                                        .child(postUserId);

                                userInteractRef
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                if(dataSnapshot.exists()) {
                                                    PostInteraction postInteraction = dataSnapshot.getValue(PostInteraction.class);

                                                    if(postInteraction.choice.equals("null")) {

                                                        interactionCountTextView.setText(String.valueOf(Integer.valueOf(interactionCountTextView.getText().toString()) + 1));
                                                        interactionImageView.setImageResource(R.drawable.ic_touch_app_accent_28dp);

                                                        postInteractionRef
                                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                                        if(dataSnapshot.exists()) {
                                                                            Post post = dataSnapshot.getValue(Post.class);

                                                                            Map<String, Object> interaction = new HashMap<>();
                                                                            interaction.put("interactionCount", post.interactionCount + 1);

                                                                            postInteractionRef.setValue(interaction);
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

                                                                            Map<String, Object> vote = new HashMap<>();
                                                                            vote.put("votes", user.votes + 1);

                                                                            posterRef.setValue(vote);
                                                                        }

                                                                    }

                                                                    @Override
                                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                    }
                                                                });

                                                    }

                                                    choiceCountRef
                                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                                    if(dataSnapshot.exists()) {
                                                                        Choice choice = dataSnapshot.getValue(Choice.class);

                                                                        Map<String, Object> choiceMap = new HashMap<>();
                                                                        choiceMap.put("count", choice.count + 1);

                                                                        choiceCountRef.setValue(choiceMap);
                                                                    }

                                                                }

                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                }
                                                            });

                                                    Map<String, Object> choiceMap = new HashMap<>();
                                                    choiceMap.put("choice", chosenRadioButton);

                                                    userInteractRef.setValue(choiceMap);

                                                }

                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });

//                                realtimeRef.runTransaction(new Transaction.Function<Void>() {
//                                    @Override
//                                    public Void apply(Transaction transaction) throws FirebaseFirestoreException {
//
//                                        DocumentSnapshot snapshot3 = transaction.get(userInteractRef);
//                                        DocumentSnapshot snapshot1 = transaction.get(choiceCountRef);
//
//
//                                        if(snapshot3.getString("choice").equals("null")) {
//                                            DocumentSnapshot snapshot2 = transaction.get(postInteractionRef);
//                                            DocumentSnapshot snapshot4 = transaction.get(posterRef);
//
//                                            interactionCountTextView.setText(String.valueOf(Integer.valueOf(interactionCountTextView.getText().toString()) + 1));
//                                            interactionImageView.setImageResource(R.drawable.ic_touch_app_accent_28dp);
//
//                                            int newPosterInteract = (int) (snapshot4.getLong("votes") + 1);
//                                            int newPostInteract = (int) (snapshot2.getLong("interactionCount") + 1);
//                                            transaction.update(posterRef, "votes", newPosterInteract);
//                                            transaction.update(postInteractionRef, "interactionCount", newPostInteract);
//
//                                        }
//
//                                        int newChoiceCount = (int) (snapshot1.getLong("count") + 1);
//                                        transaction.update(choiceCountRef, "count", newChoiceCount);
//
//                                        transaction.update(userInteractRef, "choice", chosenRadioButton);
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
//                                            }
//                                        });

                            }

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

//                .get()
//                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                    @Override
//                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                        if (task.isSuccessful()) {
//                            for (QueryDocumentSnapshot document : task.getResult()) {
//
//                                DatabaseReference choiceCountRef = realtimeRef.child("posts")
//                                        .child(postId)
//                                        .child("choices")
//                                        .child(document.getId());
//
//                                DatabaseReference postInteractionRef = realtimeRef.child("posts")
//                                        .child(postId);
//
//                                DatabaseReference userInteractRef = realtimeRef.child("users")
//                                        .child(sharedPreferences.getString("userFireId", ""))
//                                        .child("postInteractions")
//                                        .child(postId);
//
//                                DatabaseReference posterRef = realtimeRef.child("users")
//                                        .child(postUserId);
//
//                                realtimeRef.runTransaction(new Transaction.Function<Void>() {
//                                    @Override
//                                    public Void apply(Transaction transaction) throws FirebaseFirestoreException {
//
//                                        DocumentSnapshot snapshot3 = transaction.get(userInteractRef);
//                                        DocumentSnapshot snapshot1 = transaction.get(choiceCountRef);
//
//
//                                        if(snapshot3.getString("choice").equals("null")) {
//                                            DocumentSnapshot snapshot2 = transaction.get(postInteractionRef);
//                                            DocumentSnapshot snapshot4 = transaction.get(posterRef);
//
//                                            interactionCountTextView.setText(String.valueOf(Integer.valueOf(interactionCountTextView.getText().toString()) + 1));
//                                            interactionImageView.setImageResource(R.drawable.ic_touch_app_accent_28dp);
//
//                                            int newPosterInteract = (int) (snapshot4.getLong("votes") + 1);
//                                            int newPostInteract = (int) (snapshot2.getLong("interactionCount") + 1);
//                                            transaction.update(posterRef, "votes", newPosterInteract);
//                                            transaction.update(postInteractionRef, "interactionCount", newPostInteract);
//
//                                        }
//
//                                        int newChoiceCount = (int) (snapshot1.getLong("count") + 1);
//                                        transaction.update(choiceCountRef, "count", newChoiceCount);
//
//                                        transaction.update(userInteractRef, "choice", chosenRadioButton);
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
//                                            }
//                                        });
//                            }
//                        }
//                    }
//                });

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

    private void createRadioButton(Choice choice, DatabaseReference ref) {
        View radioButtonLayout = getLayoutInflater().inflate(R.layout.radio_button, null);
        ConstraintLayout constraintLayout = radioButtonLayout.findViewById(R.id.cl_option);
        RadioButton radioButton = radioButtonLayout.findViewById(R.id.rb_choice);
        TextView textView = radioButtonLayout.findViewById(R.id.tv_percent);
        if(constraintLayout.getParent()!=null)
            ((ViewGroup)constraintLayout.getParent()).removeView(constraintLayout);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()) {
                    Choice innerChoice = dataSnapshot.getValue(Choice.class);

                    textView.setText(String.valueOf((int)(innerChoice.count / Float.valueOf(interactionCountTextView.getText().toString()) * 100)) + "%");

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

//        ListenerRegistration registration = docRef.addSnapshotListener(getActivity(), new EventListener<DocumentSnapshot>() {
//            @Override
//            public void onEvent(@Nullable DocumentSnapshot snapshot,
//                                @Nullable FirebaseFirestoreException e) {
//                if (e != null) {
//                    return;
//                }
//
//                if (snapshot != null && snapshot.exists()) {
//                    textView.setText(String.valueOf((int)(snapshot.getLong("count") / Float.valueOf(interactionCountTextView.getText().toString()) * 100)) + "%");
//                } else {
//                }
//            }
//        });
//
//        registrations.add(registration);

        radioButton.setText(choice.title);
        radioButton.setId((int) System.currentTimeMillis());
        textView.setText(String.valueOf((int)(choice.count / Float.valueOf(interactionCountTextView.getText().toString()) * 100)) + "%");

        choicesRadioGroup.addView(constraintLayout);
        choicesRadioGroup.setOnCheckedChangeListener(new RadioGroupPlus.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroupPlus radioGroupPlus, int i) {
                RadioButton rb = radioGroupPlus.findViewById(i);
                chosenRadioButton = rb.getText().toString();
                createPostInteractions(0,0,"choice");
            }

        });

        if(choice.title.equals(chosenRadioButton))
            radioButton.setChecked(true);
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
