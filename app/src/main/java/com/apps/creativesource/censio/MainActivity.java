package com.apps.creativesource.censio;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
                                    Toast.makeText(getApplicationContext(), "Sign out failed.", Toast.LENGTH_LONG).show();
                            }
                        });
                break;
            case R.id.it_close_account:

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
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

    private void toLogin() {
        Intent intentToLogin = new Intent(this, LoginActivity.class);
        startActivity(intentToLogin);
        finish();
    }

}
