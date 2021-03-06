package com.apps.creativesource.censio;

import android.content.Intent;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainFragment extends Fragment {

    @BindView(R.id.fab_add) FloatingActionButton fab;
    private TabAdapter tabAdapter;
    @BindView(R.id.tl_tab_layout) TabLayout tabLayout;
    @BindView(R.id.vp_view_pager) ViewPager viewPager;

    private boolean first = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, view);

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
