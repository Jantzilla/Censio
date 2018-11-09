package com.apps.creativesource.censio;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AddActivity extends AppCompatActivity {
    ArrayList<String> choices;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private TabAdapter tabAdapter;
    private Button publishButton;
    private EditText statementEditText;
    private SQLiteDatabase db;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private int postTypeId;
    private PublishClickListener clickListener;

    public interface PublishClickListener {
        ArrayList<String> myAction();
    }

    public void setClickListener (PublishClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        ActionBar actionBar = getSupportActionBar();

        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        setTitle("Create");

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();

        choices = new ArrayList<>();

        tabLayout = findViewById(R.id.tl_tab_layout);
        viewPager = findViewById(R.id.vp_view_pager);
        publishButton = findViewById(R.id.btn_publish);
        statementEditText = findViewById(R.id.et_title);

        postTypeId = R.drawable.ic_touch_app_white_28dp;

        CensioDbHelper dbHelper = new CensioDbHelper(this);
        db = dbHelper.getWritableDatabase();

        auth = FirebaseAuth.getInstance();

        firestore = FirebaseFirestore.getInstance();

        MultiChoiceFragment multiChoiceFragment = new MultiChoiceFragment();
        setClickListener(multiChoiceFragment);

        tabAdapter = new TabAdapter(getSupportFragmentManager());
        tabAdapter.addFragment(multiChoiceFragment, "Multi-Choice");
        tabAdapter.addFragment(new CommentFragment(), "Comment");

        viewPager.setAdapter(tabAdapter);
        tabLayout.setupWithViewPager(viewPager);

        publishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isValidTextFields()){

                    postTypeId = getPostTypeId();
                    addNewPost(statementEditText.getText().toString());
                    Toast.makeText(getApplicationContext(), "Your poll has been posted!", Toast.LENGTH_LONG).show();
                    Intent returnIntent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(returnIntent);
                    finish();
                }
            }
        });

    }

    public boolean isValidTextFields() {
        if(statementEditText.getText().toString().equals("")){
            Toast.makeText(getApplicationContext(),"Please enter a statement.", Toast.LENGTH_LONG).show();
            statementEditText.setError("Please enter a statement.");
            return false;
        } if(statementEditText.getText().toString().length() > 150) {
            Toast.makeText(getApplicationContext(),"Cannot exceed 150 characters.", Toast.LENGTH_LONG).show();
            statementEditText.setError("Statements cannot exceed 150 characters.");
            return false;
        }
        return true;
    }

    public int getPostTypeId() {
        switch (viewPager.getCurrentItem()) {
            case 0: //Todo: Write Multi-Choice Case;
                return R.drawable.ic_touch_app_white_28dp;
            case 1: //Todo: Write Comment Case;
                return R.drawable.ic_comment_white_28dp;
            default: //Todo: Write Default Case;
                return R.drawable.ic_touch_app_white_28dp;

        }
    }

    private void addNewPost(String statement) {

        Map<String, Object> posts = new HashMap<>();                    //TODO: FIX THIS FIRESTORE USER POST IMPLEMENTATION
        posts.put("author", Objects.requireNonNull(auth.getUid()));
        posts.put("statement", statement);
        posts.put("postTypeId", postTypeId);
        posts.put("likes", 0);
        posts.put("dislikes", 0);
        posts.put("interactionCount", 0);
        posts.put("userRef", sharedPreferences.getString("userFireId", ""));
        posts.put("timestamp", System.currentTimeMillis());

// Add a new document with a generated ID
        firestore.collection("posts")
                .add(posts)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        if(getPostTypeId() == R.drawable.ic_touch_app_white_28dp) {

                            choices.addAll(clickListener.myAction());
                            WriteBatch batch = firestore.batch();

                            for(int i = 0; i < choices.size(); i++) {
                                DocumentReference choiceRef = firestore.collection("posts").document(documentReference.getId())
                                        .collection("choices")
                                        .document(String.valueOf(System.nanoTime()));


                                Map<String, Object> choice = new HashMap<>();             //TODO: FIX THIS FIRESTORE USER CHOICES IMPLEMENTATION
                                choice.put("title", choices.get(i));
                                choice.put("count", 0);

                                batch.set(choiceRef, choice);
                            }

//                            Map<String, Object> postInteractions = new HashMap<>();
//                            postInteractions.put("choice", "null");
//                            postInteractions.put("like", 0);
//
//                            DocumentReference postInteractionsRef = firestore.collection("users")
//                                    .document(sharedPreferences.getString("userFireId", ""))
//                                    .collection("postInteractions")
//                                    .document(documentReference.getId());
//
//                            batch.set(postInteractionsRef, postInteractions);

                            batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                }
                            })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w("Firestore Log", "Error adding document", e);
                                        }
                                    });
                        } //else {
//
//                            Map<String, Object> postInteractions = new HashMap<>();
//                            postInteractions.put("comment", false);
//                            postInteractions.put("like", 0);
//
//                            firestore.collection("users")
//                                    .document(sharedPreferences.getString("userFireId", ""))
//                                    .collection("postInteractions")
//                                    .document(documentReference.getId())
//                                    .set(postInteractions)
//                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                        @Override
//                                        public void onComplete(@NonNull Task<Void> task) {
//
//                                        }
//                                    })
//                                    .addOnFailureListener(new OnFailureListener() {
//                                        @Override
//                                        public void onFailure(@NonNull Exception e) {
//
//                                        }
//                                    });
//                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Firestore Log", "Error adding document", e);
                    }
                });

//        ContentValues cv = new ContentValues();                    TODO: Remove database code
//
//        cv.put(CensioContract.Posts.COLUMN_POST_AUTHOR, "Me");
//        cv.put(CensioContract.Posts.COLUMN_POST_TITLE, statement);
//        cv.put(CensioContract.Posts.COLUMN_POST_INTERACTION_COUNT, 0);
//        cv.put(CensioContract.Posts.COLUMN_POST_BODY, "");
//        cv.put(CensioContract.Posts.COLUMN_POST_COMMENTS, "");
//
//        return db.insert(CensioContract.Posts.TABLE_NAME, null, cv);
    }

}
