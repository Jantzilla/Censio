package com.apps.creativesource.censio;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class MultiChoiceFragment extends Fragment implements MultiChoiceAdapter.ListItemClickListener, AddActivity.PublishClickListener {
    private static int optionCount = 2;
    private MultiChoiceAdapter adapter;
    private RecyclerView multiChoiceList;
    private FloatingActionButton fab;

    public MultiChoiceFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_multi_choice, container, false);

        multiChoiceList = view.findViewById(R.id.rv_multi_choice);


        fab = view.findViewById(R.id.fab_add);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());

        multiChoiceList.setLayoutManager(layoutManager);
        multiChoiceList.setHasFixedSize(true);

        adapter = new MultiChoiceAdapter(optionCount,this);
        multiChoiceList.setAdapter(adapter);
        multiChoiceList.setItemViewCacheSize(6);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(optionCount < 6) {
                    adapter = new MultiChoiceAdapter(optionCount += 1, MultiChoiceFragment.this);
                    multiChoiceList.swapAdapter(adapter, true);
                }
            }
        });

        return view;
    }

    @Override
    public void onListItemClick(int clickedItemIndex) {
        if(optionCount > 2) {
            Toast.makeText(getContext(), "option # " + clickedItemIndex, Toast.LENGTH_LONG).show();
            MultiChoiceAdapter adapter2 = new MultiChoiceAdapter(optionCount -= 1, MultiChoiceFragment.this);
            multiChoiceList.swapAdapter(adapter2, true);
        }
    }

    private boolean isEditTextFinished() {
        boolean result = true;
        for(int i=0;i<adapter.getItemCount();i++){
            MultiChoiceAdapter.ChoiceViewHolder viewHolder = (MultiChoiceAdapter.ChoiceViewHolder)
                    multiChoiceList.findViewHolderForAdapterPosition(i);
            if(viewHolder.multiChoiceEditText.getText().toString().isEmpty()) {
                viewHolder.multiChoiceEditText.setError("Please enter a choice");
                result = false;
            } if(viewHolder.multiChoiceEditText.getText().toString().length() > 150) {
                viewHolder.multiChoiceEditText.setError("Cannot exceed 150 characters.");
                result = false;
            }
        }

        return result;
    }

    private ArrayList<String> getAllEditText() {
        ArrayList<String> editTextStrings = new ArrayList<>();

             //TODO: FIX THIS FIRESTORE USER CHOICES IMPLEMENTATION

        for(int i=0;i<adapter.getItemCount();i++){
            MultiChoiceAdapter.ChoiceViewHolder viewHolder = (MultiChoiceAdapter.ChoiceViewHolder)
                    multiChoiceList.findViewHolderForAdapterPosition(i);

            editTextStrings.add(viewHolder.multiChoiceEditText.getText().toString());
        }

        return editTextStrings;
    }

    @Override
    public ArrayList<String> myAction() {
        if(isEditTextFinished())
            return getAllEditText();
        else
            return getAllEditText();

    }
}
