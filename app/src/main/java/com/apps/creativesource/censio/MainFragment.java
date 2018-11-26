package com.apps.creativesource.censio;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainFragment extends Fragment {

    private FloatingActionButton fab;
    private ImageView profileImageView;
    private TabAdapter tabAdapter;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private TextView firstNameTextView;
    private TextView lastNameTextView;
    private TextView likesTextView;
    private TextView dislikesTextView;
    private TextView votesTextView;
    private TextView commentsTextView;

    private Uri profileUri;
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;

    private DatabaseReference realtimeRef;
    private boolean first = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        fab = view.findViewById(R.id.fab_add);
        tabLayout = view.findViewById(R.id.tl_tab_layout);
        viewPager = view.findViewById(R.id.vp_view_pager);
        profileImageView = view.findViewById(R.id.iv_profile);
        firstNameTextView = view.findViewById(R.id.tv_first_name);
        lastNameTextView = view.findViewById(R.id.tv_last_name);
        likesTextView = view.findViewById(R.id.tv_likes);
        dislikesTextView = view.findViewById(R.id.tv_dislikes);
        votesTextView = view.findViewById(R.id.tv_votes);
        commentsTextView = view.findViewById(R.id.tv_comments);

        realtimeRef = FirebaseDatabase.getInstance().getReference();

        if(getActivity().getIntent().hasExtra("orientation")) {
            first = false;
        }

        Bundle bundle = new Bundle();
        bundle.putBoolean("first", first);

        auth = FirebaseAuth.getInstance();

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

        tabAdapter = new TabAdapter(getActivity().getSupportFragmentManager());

        FeedFragment feedFragment = new FeedFragment();
        feedFragment.setArguments(bundle);

        PollsFragment pollsFragment = new PollsFragment();
        pollsFragment.setArguments(bundle);

        tabAdapter.addFragment(feedFragment, getString(R.string.feed));
        tabAdapter.addFragment(pollsFragment, getString(R.string.polls));

        viewPager.setAdapter(tabAdapter);
        tabLayout.setupWithViewPager(viewPager);

        viewPager.setCurrentItem(MainActivity.tabIndex);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentAdd = new Intent(getContext(), AddActivity.class);
                startActivity(intentAdd);
            }
        });

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        MainActivity.tabIndex = viewPager.getCurrentItem();
    }

    @Override
    public void onResume() {
        super.onResume();
        viewPager.setCurrentItem(MainActivity.tabIndex);
    }

    private boolean isUserLogin() {
        if(auth.getCurrentUser() != null) {
            return true;
        }
        return false;
    }

    private void toLogin() {
        Intent intentToLogin = new Intent(getContext(), LoginActivity.class);
        startActivity(intentToLogin);
        getActivity().finish();
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
