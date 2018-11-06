package com.apps.creativesource.censio;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.io.FileNotFoundException;

public class MainActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;

    private FloatingActionButton fab;
    private ImageView profileImageView;
    private TabAdapter tabAdapter;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private String username;
    private TextView firstNameTextView;
    private TextView lastNameTextView;
    private TextView likesTextView;
    private TextView dislikesTextView;
    private TextView votesTextView;
    private TextView commentsTextView;

    private Uri profileUri;
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private FirebaseFunctions functions;

    private FirebaseFirestore firestore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fab = findViewById(R.id.fab_add);
        tabLayout = findViewById(R.id.tl_tab_layout);
        viewPager = findViewById(R.id.vp_view_pager);
        profileImageView = findViewById(R.id.iv_profile);
        firstNameTextView = findViewById(R.id.tv_first_name);
        lastNameTextView = findViewById(R.id.tv_last_name);
        likesTextView = findViewById(R.id.tv_likes);
        dislikesTextView = findViewById(R.id.tv_dislikes);
        votesTextView = findViewById(R.id.tv_votes);
        commentsTextView = findViewById(R.id.tv_comments);

        firestore = FirebaseFirestore.getInstance();

        auth = FirebaseAuth.getInstance();

        if(!isUserLogin())
            toLogin();
        else {
            firebaseUser = auth.getCurrentUser();
            assert firebaseUser != null;
            for (UserInfo profile : firebaseUser.getProviderData()) {

                String name = profile.getDisplayName();
                if(name.split("\\w+").length>1){

                    lastNameTextView.setText(name.substring(name.lastIndexOf(" ")+1));
                    firstNameTextView.setText(name.substring(0, name.lastIndexOf(' ')));
                }
                else{
                    firstNameTextView.setText(name);
                }

                profileUri = Uri.parse(profile.getPhotoUrl().toString());
            }
//            setTitle(username);
            Glide.with(this).load(profileUri.toString()).into(profileImageView);

        }

        getInteractions();

        tabAdapter = new TabAdapter(getSupportFragmentManager());
        tabAdapter.addFragment(new FeedFragment(), "Feed");
        tabAdapter.addFragment(new PollsFragment(), "Polls");

        viewPager.setAdapter(tabAdapter);
        tabLayout.setupWithViewPager(viewPager);


        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentAdd = new Intent(getApplicationContext(), AddActivity.class);
                startActivity(intentAdd);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri targetUri = data.getData();
            Bitmap bitmap;
            try {
                bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(targetUri));
                profileImageView.setImageBitmap(bitmap);
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setPhotoUri(targetUri)
                        .build();

                firebaseUser.updateProfile(profileUpdates)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getApplicationContext(),"User profile updated.",Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int selectedMenuItem = item.getItemId();

        switch (selectedMenuItem) {
            case R.id.it_sign_out:
                AuthUI.getInstance()
                        .signOut(MainActivity.this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful())
                                    toLogin();
                                else
                                    Toast.makeText(getApplicationContext(), "Sign out failed.", Toast.LENGTH_LONG).show();
                            }
                        });
                break;
            case R.id.it_close_account:

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Are you sure you want to close your account?");
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

                        removeProfile();

//                                        AuthUI.getInstance()                            TODO: reauthenticate user to delete
//                        .delete(MainActivity.this)
//                        .addOnCompleteListener(new OnCompleteListener<Void>() {
//                            @Override
//                            public void onComplete(@NonNull Task<Void> task) {
//                                if(task.isSuccessful()) {
//                                    removeProfile();
//                                    dialog.cancel();
//                                    toLogin();
//                                } else
//                                    Toast.makeText(getApplicationContext(), "Account deletion failed.", Toast.LENGTH_LONG).show();
//                            }
//                        });
                    }
                });
                builder.show();
                break;
            default:
                break;
        }
        return true;
    }

    private boolean isUserLogin() {
        if(auth.getCurrentUser() != null) {
            return true;
        }
        return false;
    }

    private void toLogin() {
        Intent intentToLogin = new Intent(this, LoginActivity.class);
        startActivity(intentToLogin);
        finish();
    }

    private void getInteractions() {
        firestore.collection("users")
                .whereEqualTo("id", auth.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            for(QueryDocumentSnapshot document : task.getResult()) {
                                likesTextView.setText(String.valueOf(document.getLong("likes")));
                                dislikesTextView.setText(String.valueOf(document.getLong("dislikes")));
                                votesTextView.setText(String.valueOf(document.getLong("votes")));
                                commentsTextView.setText(String.valueOf(document.getLong("comments")));
                            }
                        }
                    }
                });
    }

    private void removeProfile() {
//        DocumentReference userRef = firestore.collection("users")               TODO: Fix cloud functions implementation.
//                .document("hb1XtSaZ8sETaGBGSixg");
//
//        functions = FirebaseFunctions.getInstance();
//
//        functions.getHttpsCallable("mintAdminToken")
//                .call(auth.getUid())
//                .continueWith(new Continuation<HttpsCallableResult, Object>() {
//                    @Override
//                    public Object then(@NonNull Task<HttpsCallableResult> task) throws Exception {
//                        return null;
//                    }
//                });
////                .addOnCompleteListener(new OnCompleteListener<HttpsCallableResult>() {
////                    @Override
////                    public void onComplete(@NonNull Task<HttpsCallableResult> task) {
////
////                        functions.getHttpsCallable("recursiveDelete")
////                                .call(userRef.toString())
////                                .addOnCompleteListener(new OnCompleteListener<HttpsCallableResult>() {
////                                    @Override
////                                    public void onComplete(@NonNull Task<HttpsCallableResult> task) {
////
////                                    }
////                                });
////                    }
////                });

        Intent intentToLogin = new Intent(this, LoginActivity.class);
        startActivity(intentToLogin);
        finish();
    }
}
