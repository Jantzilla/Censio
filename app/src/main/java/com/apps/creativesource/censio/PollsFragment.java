package com.apps.creativesource.censio;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class PollsFragment extends Fragment implements UserPollsAdapter.ListItemClickListener {
    private RecyclerView pollsList;
    private ProgressBar progressBar;
    private TextView emptyListTextView;

    private boolean twoPane = false;

    private FragmentManager fragmentManager;

    private UserPollsAdapter adapter;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

    private SQLiteDatabase db;

    public PollsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onResume() {
        super.onResume();
        getAllPosts();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_polls, container, false);

        fragmentManager = getActivity().getSupportFragmentManager();
//
//        Fragment fragment = new ChoiceDetailFragment();

        if(getActivity().findViewById(R.id.detail_container) != null) {
            twoPane = true;
//            fragmentManager.beginTransaction()
//                    .add(R.id.fl_detail_container, fragment)
//                    .commit();
        }
        CensioDbHelper dbHelper = new CensioDbHelper(getContext());
        db = dbHelper.getWritableDatabase();

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        pollsList = view.findViewById(R.id.rv_user_polls);
        progressBar = view.findViewById(R.id.pb_user_polls);
        emptyListTextView = view.findViewById(R.id.tv_empty_list_notification);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());

        pollsList.setLayoutManager(layoutManager);
        pollsList.setHasFixedSize(true);

        getAllPosts();

        return view;
    }

    private void getAllPosts() {

        //TODO: FIX POST USER IMPLEMENTATION

        ArrayList<Post> postArrayList = new ArrayList<>();

        firestore.collection("posts")
                .whereEqualTo("author", auth.getUid())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Post post = document.toObject(Post.class);
                                post.firestoreId = document.getId();
                                postArrayList.add(post);

                            }
                            loadAdapter(postArrayList);
                        }
                    }
                });

//        return db.query(CensioContract.Posts.TABLE_NAME,
//                null,
//                null,
//                null,
//                null,
//                null,
//                CensioContract.Posts.COLUMN_POST_TIMESTAMP + " DESC");
    }

    public void loadAdapter(ArrayList<Post> postArrayList) {
        if(!postArrayList.isEmpty()) {
            adapter = new UserPollsAdapter(postArrayList,false, PollsFragment.this);
            pollsList.setAdapter(adapter);
            pollsList.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE );
        } else {
            progressBar.setVisibility(View.GONE );
            emptyListTextView.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onListItemClicked(int clickedItemIndex, String profileUri, String username, String statement,
                                  String interactionCount, int likes, int dislikes, int postTypeId, String id, String postFireUserId) {
        if(twoPane) {

            Fragment fragment;

            if(postTypeId == R.drawable.ic_touch_app_white_28dp)
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
            detailIntent.putExtra("statement", statement);
            detailIntent.putExtra("interactionCount", interactionCount);
            detailIntent.putExtra("likes", likes);
            detailIntent.putExtra("dislikes", dislikes);
            detailIntent.putExtra("postTypeId", postTypeId);
            detailIntent.putExtra("firestoreId", id);
            detailIntent.putExtra("postFireUserId", postFireUserId);
            detailIntent.putExtra("userPost", true);
            startActivity(detailIntent);
        }
    }
}
