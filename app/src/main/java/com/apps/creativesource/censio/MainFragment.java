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
    private TabAdapter tabAdapter;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    private Uri profileUri;

    private boolean first = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        fab = view.findViewById(R.id.fab_add);
        tabLayout = view.findViewById(R.id.tl_tab_layout);
        viewPager = view.findViewById(R.id.vp_view_pager);

        if(getActivity().getIntent().hasExtra("orientation")) {
            first = false;
        }

        Bundle bundle = new Bundle();
        bundle.putBoolean("first", first);

        tabAdapter = new TabAdapter(getActivity().getSupportFragmentManager());

        FeedFragment feedFragment = new FeedFragment();
        feedFragment.setArguments(bundle);

        PollsFragment pollsFragment = new PollsFragment();
        pollsFragment.setArguments(bundle);

        tabAdapter.addFragment(feedFragment, getString(R.string.feed));
        tabAdapter.addFragment(pollsFragment, getString(R.string.posts));

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
}
