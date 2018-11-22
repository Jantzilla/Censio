package com.apps.creativesource.censio;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DetailActivity extends AppCompatActivity {
    private boolean userPost;
    private String postId;
    private DatabaseReference realtimeRef;
    private boolean twoPane;
    private String profileUri;
    private String username;
    private String statement;
    private String interactionCount;
    private int likes;
    private int dislikes;
    private int postTypeId;
    private String postFireUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        realtimeRef = FirebaseDatabase.getInstance().getReference();

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment;

        Intent initialIntent = getIntent();

        if(initialIntent.getIntExtra("postTypeId", R.drawable.ic_touch_app_primary_28dp) == R.drawable.ic_touch_app_primary_28dp)
            fragment = new ChoiceDetailFragment();
        else
            fragment = new CommentDetailFragment();

        fragmentManager.beginTransaction()
                .add(R.id.fl_detail, fragment)
                .commit();

        userPost = initialIntent.getBooleanExtra("userPost", false);
        postId = initialIntent.getStringExtra("firestoreId");
        twoPane = initialIntent.getBooleanExtra("twoPane", false);

        profileUri = initialIntent.getStringExtra("profileUri");
        username = initialIntent.getStringExtra("username");
        statement = initialIntent.getStringExtra("statement");
        interactionCount = initialIntent.getStringExtra("interactionCount");
        likes = initialIntent.getIntExtra("firestoreId", 0);
        dislikes = initialIntent.getIntExtra("twoPane", 0);
        postTypeId = initialIntent.getIntExtra("userPost", 0);
        postFireUserId = initialIntent.getStringExtra("firestoreId");


        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        View view = getLayoutInflater().inflate(R.layout.activity_main, null);
        if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE && view.findViewById(R.id.detail_container) != null) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            Intent detailIntent = new Intent(this, DetailActivity.class);
            detailIntent.putExtra("profileUri", profileUri);
            detailIntent.putExtra("username", username);
            detailIntent.putExtra("statement", statement);
            detailIntent.putExtra("interactionCount", interactionCount);
            detailIntent.putExtra("likes", likes);
            detailIntent.putExtra("dislikes", dislikes);
            detailIntent.putExtra("postTypeId", postTypeId);
            detailIntent.putExtra("firestoreId", postId);
            detailIntent.putExtra("postFireUserId", postFireUserId);
            detailIntent.putExtra("userPost", userPost);
            detailIntent.putExtra("twoPane", twoPane);
            startActivity(detailIntent);
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        if(userPost)
            inflater.inflate(R.menu.post, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                break;
            case R.id.it_delete:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
                        Intent homeIntent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(homeIntent);
                        finish();
                        dialog.cancel();
                    }
                });
                builder.show();
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
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
