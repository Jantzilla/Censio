package com.apps.creativesource.censio;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
    private DatabaseReference realtimeRef;
    private FirebaseAuth auth;
    private SharedPreferences sharedPreferences;
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

        setTitle(R.string.create);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        choices = new ArrayList<>();

        tabLayout = findViewById(R.id.tl_tab_layout);
        viewPager = findViewById(R.id.vp_view_pager);
        publishButton = findViewById(R.id.btn_publish);
        statementEditText = findViewById(R.id.et_title);

        postTypeId = R.drawable.ic_touch_app_primary_28dp;

        auth = FirebaseAuth.getInstance();

        realtimeRef = FirebaseDatabase.getInstance().getReference();

        MultiChoiceFragment multiChoiceFragment = new MultiChoiceFragment();
        setClickListener(multiChoiceFragment);

        tabAdapter = new TabAdapter(getSupportFragmentManager());
        tabAdapter.addFragment(multiChoiceFragment, getString(R.string.multi_choice));
        tabAdapter.addFragment(new CommentFragment(), getString(R.string.comment));

        viewPager.setAdapter(tabAdapter);
        tabLayout.setupWithViewPager(viewPager);

        publishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isValidTextFields()){

                    postTypeId = getPostTypeId();

                    if(postTypeId == R.drawable.ic_comment_primary_28dp || clickListener.myAction() != null) {

                        addNewPost(statementEditText.getText().toString());
                        Toast.makeText(getApplicationContext(), getString(R.string.poll_posted), Toast.LENGTH_LONG).show();
                        Intent returnIntent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(returnIntent);
                        finish();

                    }
                }
            }
        });

    }

    public boolean isValidTextFields() {
        if(statementEditText.getText().toString().equals("")){
            Toast.makeText(getApplicationContext(),getString(R.string.please_enter_statement), Toast.LENGTH_LONG).show();
            statementEditText.setError(getString(R.string.please_enter_statement));
            return false;
        } if(statementEditText.getText().toString().length() > 150) {
            Toast.makeText(getApplicationContext(),getString(R.string.cannot_exceed_150_chars), Toast.LENGTH_LONG).show();
            statementEditText.setError(getString(R.string.cannot_exceed_150_chars));
            return false;
        }
        return true;
    }

    public int getPostTypeId() {
        switch (viewPager.getCurrentItem()) {
            case 0:
                return R.drawable.ic_touch_app_primary_28dp;
            case 1:
                return R.drawable.ic_comment_primary_28dp;
            default:
                return R.drawable.ic_touch_app_primary_28dp;

        }
    }

    private void addNewPost(String statement){

        Long postId = System.nanoTime();

        Map<String, Object> posts = new HashMap<>();
        posts.put("author", Objects.requireNonNull(auth.getUid()));
        posts.put("statement", statement);
        posts.put("postTypeId", postTypeId);
        posts.put("likes", 0);
        posts.put("dislikes", 0);
        posts.put("interactionCount", 0);
        posts.put("userRef", sharedPreferences.getString("userFireId", ""));
        posts.put("timestamp", System.currentTimeMillis());

        realtimeRef.child("posts").child(String.valueOf(postId)).setValue(posts)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if(getPostTypeId() == R.drawable.ic_touch_app_primary_28dp) {

                            choices.addAll(clickListener.myAction());

                            for(int i = 0; i < choices.size(); i++) {

                                String title = choices.get(i);

                                Intent serviceIntent = new Intent(getApplicationContext(), PostService.class);
                                serviceIntent.putExtra("title", title);
                                serviceIntent.putExtra("count", 0);
                                serviceIntent.putExtra("postId", postId);

                                getApplication().startService(serviceIntent);


                            }
                        }
                    }
                });
    }

}
