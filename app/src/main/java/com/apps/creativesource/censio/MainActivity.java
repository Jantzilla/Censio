package com.apps.creativesource.censio;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        realtimeRef = FirebaseDatabase.getInstance().getReference();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();
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
            case R.id.it_close_account:

                Context context = this;

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
                        functions = FirebaseFunctions.getInstance();
                        auth = FirebaseAuth.getInstance();

                        final EditText input = new EditText(context);


                        String token = sharedPreferences.getString("AuthToken", null);


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


//                        mintAdminToken(auth.getUid())
//                                .addOnCompleteListener(new OnCompleteListener<String>() {
//                                    @Override
//                                    public void onComplete(@NonNull Task<String> task) {
//                                        if (!task.isSuccessful()) {
//                                            Exception e = task.getException();
//                                            if (e instanceof FirebaseFunctionsException) {
//                                                FirebaseFunctionsException ffe = (FirebaseFunctionsException) e;
//                                                FirebaseFunctionsException.Code code = ffe.getCode();
//                                                Object details = ffe.getDetails();
//                                            }
//
//                                            Log.d("MINT", "Error");
//                                            Log.d("MINT", e.getMessage());
//
//                                        }
//
//                                        Log.d("MINT", "Worked!");
//
//                                            recursiveDelete("users/hb1XtSaZ8sETaGBGSixg/")
//                                                    .addOnCompleteListener(new OnCompleteListener<String>() {
//                                                        @Override
//                                                        public void onComplete(@NonNull Task<String> task) {
//                                                            if (!task.isSuccessful()) {
//                                                                Exception e = task.getException();
//                                                                if (e instanceof FirebaseFunctionsException) {
//                                                                    FirebaseFunctionsException ffe = (FirebaseFunctionsException) e;
//                                                                    FirebaseFunctionsException.Code code = ffe.getCode();
//                                                                    Object details = ffe.getDetails();
//                                                                }
//
//                                                                Log.d("DELETE", e.getMessage());
//
//                                                                // ...
//                                                            }
//
//                                                            // ...
//                                                        }
//                                                    });
//
//
//                                    }
//                                });

//                                        AuthUI.getInstance()              //              TODO: reauthenticate user to delete
//                        .delete(MainActivity.this)
//                        .addOnCompleteListener(new OnCompleteListener<Void>() {
//                            @Override
//                            public void onComplete(@NonNull Task<Void> task) {
//                                if(task.isSuccessful()) {
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

//        Map<String, String> data = new HashMap<>();
//        data.put("uid", uid);

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

    private void removeUserAuth(DialogInterface dialog, EditText input) {

        if (auth.getCurrentUser() != null) {
            String token = sharedPreferences.getString("AuthToken", null);
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
                                        editor.putString("AuthToken", null);
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

}
