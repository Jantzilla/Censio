package com.apps.creativesource.censio;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.core.app.NavUtils;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

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

public class SettingActivity extends AppCompatActivity {
    private Button removeButton;
    private SharedPreferences sharedPreferences;
    private DatabaseReference realtimeRef;
    private FirebaseAuth auth;
    private LinearLayout linearLayout;
    private SharedPreferences.Editor editor;
    private Typeface typeface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        Context context = this;
        ActionBar actionBar = getSupportActionBar();
        typeface = Typeface.createFromAsset(getAssets(), "ColorTube.otf");
        View view = LayoutInflater.from(this).inflate(R.layout.title_bar,null);
        TextView textView = view.findViewById(R.id.tv_title);
        textView.setText(this.getTitle());
        textView.setTypeface(typeface);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(view);

        removeButton = findViewById(R.id.btn_remove_account);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();
        realtimeRef = FirebaseDatabase.getInstance().getReference();
        linearLayout = findViewById(R.id.ll_transparent);

        if(actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(R.string.close_account_confim);
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
                        linearLayout.setVisibility(View.VISIBLE);
                        auth = FirebaseAuth.getInstance();

                        final EditText input = new EditText(context);


                        String token = sharedPreferences.getString("GoogleToken", null);


                        if (token == null) {

                            final AlertDialog dialog2 = new AlertDialog.Builder(context)
                                    .setView(input)
                                    .setTitle(R.string.re_enter_password)
                                    .setPositiveButton(R.string.remove_account, null)
                                    .setNegativeButton(R.string.cancel, null)
                                    .create();

                            dialog2.setOnShowListener(new DialogInterface.OnShowListener() {

                                @Override
                                public void onShow(DialogInterface dialogInterface) {

                                    Button postitiveButton = ((AlertDialog) dialog2).getButton(AlertDialog.BUTTON_POSITIVE);
                                    postitiveButton.setOnClickListener(new View.OnClickListener() {

                                        @Override
                                        public void onClick(View view) {

                                            if(input.getText().toString().isEmpty())
                                                input.setError(getString(R.string.field_is_empty));
                                            else {
                                                removeUserAuth(dialog2, input);
                                            }

                                        }
                                    });

                                    Button cancelButton = ((AlertDialog) dialog2).getButton(AlertDialog.BUTTON_NEGATIVE);
                                    cancelButton.setOnClickListener(new View.OnClickListener() {

                                        @Override
                                        public void onClick(View view) {

                                            dialog2.cancel();

                                        }
                                    });
                                }
                            });
                            dialog2.show();

                        } else {
                            removeUserAuth(null, input);
                        }
                    }
                });
                builder.show();
            }
        });
    }

    private void removeUserAuth(DialogInterface dialog, EditText input) {

        if (auth.getCurrentUser() != null) {
            String token = sharedPreferences.getString("GoogleToken", null);
            String password = input.getText().toString();

            AuthCredential credential;

            if (token == null) {
                credential = EmailAuthProvider.getCredential(auth.getCurrentUser().getEmail(), password);
            } else {
                credential = GoogleAuthProvider.getCredential(token, null);
            }

            auth.getCurrentUser().reauthenticate(credential)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            auth.getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        //Ok, user remove
                                        editor.putString("GoogleToken", null);
                                        if(dialog != null)
                                            dialog.cancel();
                                        deleteFirestoreUser();
                                    } else {
                                        if(!password.isEmpty())
                                            input.setError(getString(R.string.invalid_password));
                                        task.getException();
                                    }
                                }
                            });
                        }
                    });
        }
    }

    private void deleteFirestoreUser() {

        realtimeRef.child("users")
                .child(sharedPreferences.getString("userFireId", ""))
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        toLogin();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
    }

    private void toLogin() {
        Intent intentToLogin = new Intent(this, LoginActivity.class);
        startActivity(intentToLogin);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home)
            NavUtils.navigateUpFromSameTask(this);
        return super.onOptionsItemSelected(item);
    }
}
