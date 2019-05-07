package com.apps.creativesource.censio;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    private LinearLayout linearLayout;
    private ImageView profileImageView;
    private TextView firstNameTextView;
    private TextView lastNameTextView;
    private TextView likesTextView;
    private TextView dislikesTextView;
    private TextView votesTextView;
    private TextView commentsTextView;
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private DatabaseReference realtimeRef;
    private FragmentManager fragmentManager;
    private Fragment fragment;
    private Uri profileUri;
    private String profileString;
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
    private Typeface typeface;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        linearLayout = findViewById(R.id.ll_transparent);
        profileImageView = findViewById(R.id.iv_profile);
        firstNameTextView = findViewById(R.id.tv_first_name);
        lastNameTextView = findViewById(R.id.tv_last_name);
        likesTextView = findViewById(R.id.tv_likes);
        dislikesTextView = findViewById(R.id.tv_dislikes);
        votesTextView = findViewById(R.id.tv_votes);
        commentsTextView = findViewById(R.id.tv_comments);
        auth = FirebaseAuth.getInstance();
        realtimeRef = FirebaseDatabase.getInstance().getReference();
        typeface = Typeface.createFromAsset(getAssets(), "ColorTube.otf");
        View view = LayoutInflater.from(this).inflate(R.layout.title_bar,null);
        TextView textView = view.findViewById(R.id.tv_title);
        textView.setText(this.getTitle());
        textView.setTypeface(typeface);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(view);

        if(!isUserLogin())
            toLogin();
        else {
            firebaseUser = auth.getCurrentUser();
            assert firebaseUser != null;
            for (UserInfo profile : firebaseUser.getProviderData()) {

                if(!profile.getDisplayName().isEmpty()) {

                    String name = profile.getDisplayName();
                    if(name.split("\\w+").length>1){

                        lastNameTextView.setText(name.substring(name.lastIndexOf(" ")+1));
                        firstNameTextView.setText(name.substring(0, name.lastIndexOf(' ')));
                    }
                    else{
                        firstNameTextView.setText(name);
                    }

                }

                if(profile.getPhotoUrl() != null) {

                    profileUri = Uri.parse(profile.getPhotoUrl().toString());
                    Glide.with(this).load(profileUri.toString()).into(profileImageView);

                }
            }

        }

        getInteractions();

        if(findViewById(R.id.detail_container) != null) {

            Intent detailIntent = getIntent();

            if(detailIntent.hasExtra("orientation")) {

                profileString = detailIntent.getStringExtra("profileUri");
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

                args.putString("profileUri", profileString);
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
            case R.id.it_privacy_policy:
                Intent intentURL = new Intent(Intent.ACTION_VIEW, Uri.parse("https://creativesource.000webhostapp.com/censio-privacy-policy"));
                startActivity(intentURL);
                break;
            default:
                break;
        }
        return true;
    }

    private void toLogin() {
        Intent intentToLogin = new Intent(this, LoginActivity.class);
        startActivity(intentToLogin);
        finish();
    }

    private boolean isUserLogin() {
        if(auth.getCurrentUser() != null) {
            return true;
        }
        return false;
    }

    private void getInteractions() {
        realtimeRef.child("users")
                .child(auth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.exists()) {

                            User user = dataSnapshot.getValue(User.class);

                            likesTextView.setText(String.valueOf(user.likes));
                            dislikesTextView.setText(String.valueOf(user.dislikes));
                            votesTextView.setText(String.valueOf(user.votes));
                            commentsTextView.setText(String.valueOf(user.comments));


                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

}
