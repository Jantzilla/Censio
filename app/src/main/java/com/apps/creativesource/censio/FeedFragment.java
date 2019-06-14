package com.apps.creativesource.censio;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FeedFragment extends Fragment implements UserPollsAdapter.ListItemClickListener {
    @BindView(R.id.rv_polls_feed) private RecyclerView pollsList;
    @BindView(R.id.pb_polls_feed) private ProgressBar progressBar;
    @BindView(R.id.tv_empty_list_notification) private TextView emptyListTextView;

    private FragmentManager fragmentManager;

    private UserPollsAdapter adapter;
    private FirebaseAuth auth;
    private DatabaseReference realtimeRef;

    private boolean twoPane = false;
    private boolean first = true;

    @Override
    public void onResume() {
        super.onResume();
        getAllPosts();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feed, container, false);
        ButterKnife.bind(this, view);

        fragmentManager = getActivity().getSupportFragmentManager();

        if(getActivity().findViewById(R.id.detail_container) != null) {
            twoPane = true;
        }

        realtimeRef = FirebaseDatabase.getInstance().getReference();
        auth = FirebaseAuth.getInstance();

        first = getArguments().getBoolean("first");

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        pollsList.setLayoutManager(layoutManager);
        pollsList.setHasFixedSize(true);


        return view;
    }

    private void getAllPosts() {

        ArrayList<Post> postArrayList = new ArrayList<>();

        realtimeRef.child("posts")
                .orderByChild("timestamp")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(dataSnapshot.exists()) {

                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                Post post = snapshot.getValue(Post.class);
                                post.firestoreId = snapshot.getKey();
                                if(!post.author.equals(auth.getUid()))
                                    postArrayList.add(post);

                            }
                            loadAdapter(postArrayList);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    public void loadAdapter(ArrayList<Post> postArrayList) {
        if(!postArrayList.isEmpty()) {
            adapter = new UserPollsAdapter(getActivity(), postArrayList, first, twoPane, FeedFragment.this);
            pollsList.setAdapter(adapter);
            pollsList.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            emptyListTextView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onListItemClicked(int clickedItemIndex, String profileUri, String username, String statement,
                                  String interactionCount, int likes, int dislikes, int postTypeId, String id, String postFireUserId, ImageView profileImageView) {


        if(twoPane) {

            Fragment fragment;

            if(postTypeId == R.drawable.ic_touch_app_primary_28dp)
                fragment = new ChoiceDetailFragment();
            else
                fragment = new CommentDetailFragment();

            Bundle args = new Bundle();

            args.putString("profileUri", profileUri);
            args.putString("username", username);
            args.putString("statement",statement);
            args.putString("interactionCount", interactionCount);
            args.putInt("likes", likes);
            args.putInt("dislikes", dislikes);
            args.putInt("postTypeId", postTypeId);
            args.putString("firestoreId", id);
            args.putString("postFireUserId", postFireUserId);
            args.putBoolean("userPost", false);

            fragment.setArguments(args);

            ProgressBar progressBar = getActivity().findViewById(R.id.pb_detail);
            progressBar.setVisibility(View.GONE);

            fragmentManager.beginTransaction()
                    .replace(R.id.detail_container, fragment)
                    .commit();
        } else {
            Intent detailIntent = new Intent(getContext(), DetailActivity.class);
            detailIntent.putExtra("profileUri", profileUri);
            detailIntent.putExtra("username", username);
            detailIntent.putExtra("statement",statement);
            detailIntent.putExtra("interactionCount", interactionCount);
            detailIntent.putExtra("likes", likes);
            detailIntent.putExtra("dislikes", dislikes);
            detailIntent.putExtra("postTypeId", postTypeId);
            detailIntent.putExtra("firestoreId", id);
            detailIntent.putExtra("postFireUserId", postFireUserId);
            detailIntent.putExtra("userPost", false);
            detailIntent.putExtra("twoPane", twoPane);
            detailIntent.putExtra("portrait", true);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                Bundle bundle = ActivityOptions
                        .makeSceneTransitionAnimation(getActivity(), profileImageView,
                                profileImageView.getTransitionName())
                        .toBundle();

                startActivity(detailIntent, bundle);

            } else
                startActivity(detailIntent);
        }
    }
}
