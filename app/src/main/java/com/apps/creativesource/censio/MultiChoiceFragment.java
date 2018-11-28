package com.apps.creativesource.censio;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;

public class MultiChoiceFragment extends Fragment implements MultiChoiceAdapter.ListItemClickListener, AddActivity.PublishClickListener {
    private static int optionCount = 2;
    private MultiChoiceAdapter adapter;
    private RecyclerView multiChoiceList;
    private FloatingActionButton fab;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_multi_choice, container, false);

        multiChoiceList = view.findViewById(R.id.rv_multi_choice);


        fab = view.findViewById(R.id.fab_add);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());

        multiChoiceList.setLayoutManager(layoutManager);
        multiChoiceList.setHasFixedSize(true);

        adapter = new MultiChoiceAdapter(optionCount,this);
        multiChoiceList.setAdapter(adapter);
//        multiChoiceList.setItemViewCacheSize(6);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(adapter.itemCount < 6) {
                    adapter.options.add("");
                    adapter.itemCount += 1;
                    adapter.notifyItemInserted(adapter.itemCount);
                }
            }
        });

        return view;
    }

    @Override
    public void onListItemClick(int clickedItemIndex) {
        if(adapter.itemCount > 2) {
            adapter.itemCount -= 1;
            adapter.options.remove(clickedItemIndex);
            adapter.notifyItemRemoved(clickedItemIndex);
            resetEditTextHint();
        }
    }

    private void resetEditTextHint() {
        for(int i=0;i<adapter.getItemCount();i++){
            MultiChoiceAdapter.ChoiceViewHolder viewHolder = (MultiChoiceAdapter.ChoiceViewHolder)
                    multiChoiceList.findViewHolderForAdapterPosition(i);
            if(viewHolder != null && !adapter.options.get(i).equals("Option " + (i + 1)))
                viewHolder.multiChoiceEditText.setHint("Option " + (i + 1));
        }
    }

    private boolean isEditTextFinished() {
        boolean result = true;
        for(int i=0;i<adapter.getItemCount();i++){
            MultiChoiceAdapter.ChoiceViewHolder viewHolder = (MultiChoiceAdapter.ChoiceViewHolder)
                    multiChoiceList.findViewHolderForAdapterPosition(i);
            if(adapter.options.get(i).equals("")) {
                if(viewHolder != null)
                    viewHolder.multiChoiceEditText.setError(getString(R.string.please_enter_choice));
                else
                    Toast.makeText(getActivity(),R.string.please_enter_choice,Toast.LENGTH_LONG);
                result = false;
            } if(adapter.options.get(i).length() > 150) {
                if(viewHolder != null)
                    viewHolder.multiChoiceEditText.setError(getString(R.string.cannot_exceed_150_chars));
                else
                    Toast.makeText(getActivity(),R.string.cannot_exceed_150_chars,Toast.LENGTH_LONG);
                result = false;
            }
        }

        return result;
    }

//    private ArrayList<String> getAllEditText() {
//        ArrayList<String> editTextStrings = new ArrayList<>();
//
//        for(int i=0;i<adapter.getItemCount();i++){
//            MultiChoiceAdapter.ChoiceViewHolder viewHolder = (MultiChoiceAdapter.ChoiceViewHolder)
//                    multiChoiceList.findViewHolderForAdapterPosition(i);
//
//            editTextStrings.add(viewHolder.multiChoiceEditText.getText().toString());
//        }
//
//        return editTextStrings;
//    }

    @Override
    public ArrayList<String> myAction() {
        if(isEditTextFinished())
            return adapter.options;
        else
            return null;

    }
}
