package com.apps.creativesource.censio;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 200;
    private FirebaseAuth auth;
    private DatabaseReference realtimeRef;

    String token;

    private Button loginButton;
    private TextView signUpButton;
    private ConstraintLayout signInLayout;
    private ProgressBar progressBar;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();

        loginButton = findViewById(R.id.btn_login);
        signUpButton = findViewById(R.id.tv_sign_up);
        signInLayout = findViewById(R.id.cl_sign_in);
        progressBar = findViewById(R.id.pb_sign_in);

        if(isUserLogin()) {
            toMainActivity();
        }

        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivityForResult(
                        AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setAvailableProviders(providers)
                                .setLogo(R.mipmap.ic_launcher_foreground)
                                .setTheme(R.style.AppTheme_NoActionBar)
                                .build(),
                        RC_SIGN_IN);
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivityForResult(
                        AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setAvailableProviders(providers)
                                .setLogo(R.mipmap.ic_launcher_foreground)
                                .setTheme(R.style.AppTheme_NoActionBar)
                                .setIsSmartLockEnabled(false,false)
                                .build(),
                        RC_SIGN_IN);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RC_SIGN_IN) {
            if(resultCode == RESULT_OK) {
                IdpResponse idpResponse = IdpResponse.fromResultIntent(data);

                if(idpResponse.getIdpToken() == null)
                    editor.putString("AuthToken", null);
                else
                    editor.putString("AuthToken", idpResponse.getIdpToken());
                loginUser();
            } if(resultCode == RESULT_CANCELED) {
                Toast.makeText(this,getString(R.string.login_cancelled), Toast.LENGTH_LONG).show();
            }
            return;
        }
        Toast.makeText(this,getString(R.string.unknown_response), Toast.LENGTH_LONG).show();
    }

    private boolean isUserLogin() {
        return auth.getCurrentUser() != null;
    }

    private void toMainActivity() {
        UserInfoWidget.sendRefreshBroadcast(getApplicationContext());
        Intent intentLogin = new Intent(this, MainActivity.class);
        startActivity(intentLogin);
        finish();
    }

    private void loginUser() {
        signInLayout.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        realtimeRef = FirebaseDatabase.getInstance().getReference();

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            return;
                        }

                        token = task.getResult().getToken();

                    }
                });

        realtimeRef.child("users")
                .child(auth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        String photoUrl;

                        if(auth.getCurrentUser().getPhotoUrl() != null) {

                            photoUrl = auth.getCurrentUser().getPhotoUrl().toString();

                        } else
                            photoUrl = null;

                        if(!dataSnapshot.exists()) {

                            createNewUser(auth.getUid(),auth.getCurrentUser().getDisplayName(),photoUrl,token);


                            editor.putString("userFireId",auth.getUid());
                            editor.putString("name", auth.getCurrentUser().getDisplayName());
                            editor.putString("profileUri", photoUrl);
                            editor.apply();

                            progressBar.setVisibility(View.GONE);
                            toMainActivity();


                        } else {

                            editor.putString("userFireId",auth.getUid());
                            editor.putString("name", auth.getCurrentUser().getDisplayName());
                            editor.putString("profileUri", photoUrl);
                            editor.apply();

                            toMainActivity();


                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void createNewUser(String id, String name, String profileUri, String token){

        Map<String, Object> user = new HashMap<>();
        user.put("id", id);
        user.put("name", name);
        user.put("profileUri", profileUri);
        user.put("likes", 0);
        user.put("dislikes", 0);
        user.put("comments", 0);
        user.put("votes", 0);
        user.put("token", token);

        realtimeRef.child("users").child(id).setValue(user);
    }
}
