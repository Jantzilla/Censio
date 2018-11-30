package com.apps.creativesource.censio;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private FirebaseFunctions functions;
    private FirebaseAuth auth;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private DatabaseReference realtimeRef;
    private LinearLayout linearLayout;
    private FragmentManager fragmentManager;
    private Fragment fragment;
    private String profileUri;
    private String username;
    private String statement;
    private String interactionCount;
    private int likes;
    private int dislikes;
    private int postTypeId;
    private String id;
    private String postFireUserId;
    private boolean userPost;
    public static int tabIndex = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        realtimeRef = FirebaseDatabase.getInstance().getReference();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();
        linearLayout = findViewById(R.id.ll_transparent);

        if(findViewById(R.id.detail_container) != null) {

            Intent detailIntent = getIntent();

            if(detailIntent.hasExtra("orientation")) {

                profileUri = detailIntent.getStringExtra("profileUri");
                username = detailIntent.getStringExtra("username");
                statement = detailIntent.getStringExtra("statement");
                interactionCount = detailIntent.getStringExtra("interactionCount");
                likes = detailIntent.getIntExtra("likes", 0);
                dislikes = detailIntent.getIntExtra("dislikes", 0);
                postTypeId = detailIntent.getIntExtra("postTypeId", 0);
                id = detailIntent.getStringExtra("firestoreId");
                postFireUserId = detailIntent.getStringExtra("postFireUserId");
                userPost = detailIntent.getBooleanExtra("userPost", false);

                if (postTypeId == R.drawable.ic_touch_app_primary_28dp)
                    fragment = new ChoiceDetailFragment();
                else
                    fragment = new CommentDetailFragment();

                Bundle args = new Bundle();

                args.putString("profileUri", profileUri);
                args.putString("username", username);
                args.putString("statement", statement);
                args.putString("interactionCount", interactionCount);
                args.putInt("likes", likes);
                args.putInt("dislikes", dislikes);
                args.putInt("postTypeId", postTypeId);
                args.putString("firestoreId", id);
                args.putString("postFireUserId", postFireUserId);
                args.putBoolean("userPost", userPost);

                fragment.setArguments(args);

                fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.detail_container, fragment)
                        .commit();
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int selectedMenuItem = item.getItemId();

        switch (selectedMenuItem) {
            case R.id.it_sign_out:
                linearLayout.setVisibility(View.VISIBLE);
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful())
                                    toLogin();
                                else
                                    Toast.makeText(getApplicationContext(), getString(R.string.sign_out_failed), Toast.LENGTH_LONG).show();
                            }
                        });
                break;
            case R.id.it_settings:
                Intent intent = new Intent(this, SettingActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
        return true;
    }

    private Task<String> recursiveDelete(String path) {

        Map<String, Object> data = new HashMap<>();
        data.put("path", path);
        data.put("push", true);

        return functions
                .getHttpsCallable("recursiveDelete")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        String result = (String) task.getResult().getData();
                        return result;
                    }
                });


    }

    private Task<String> mintAdminToken (String uid) {


        return functions
                .getHttpsCallable("mintAdminToken")
                .call(uid)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        String result = (String) task.getResult().getData();
                        return result;
                    }
                });


    }

    private void toLogin() {
        Intent intentToLogin = new Intent(this, LoginActivity.class);
        startActivity(intentToLogin);
        finish();
    }

}
