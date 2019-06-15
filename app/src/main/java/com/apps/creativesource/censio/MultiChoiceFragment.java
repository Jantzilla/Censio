package com.apps.creativesource.censio;

import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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
    private String optionString;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_multi_choice, container, false);

        multiChoiceList = view.findViewById(R.id.rv_multi_choice);

        optionString = getString(R.string.option);


        fab = view.findViewById(R.id.fab_add);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());

        multiChoiceList.setLayoutManager(layoutManager);
        multiChoiceList.setHasFixedSize(true);

        adapter = new MultiChoiceAdapter(optionCount,this);
        multiChoiceList.setAdapter(adapter);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(adapter.itemCount < 10) {
                    adapter.options.add("");
                    adapter.itemCount += 1;
                    adapter.notifyItemInserted(adapter.itemCount);
                    multiChoiceList.smoothScrollToPosition(adapter.itemCount);
                } else
                    Toast.makeText(getActivity(),getString(R.string.multi_choice_limit_reached),Toast.LENGTH_LONG).show();
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
            if(viewHolder != null && !adapter.options.get(i).equals(optionString + " " + (i + 1)))
                viewHolder.multiChoiceEditText.setHint(optionString + " " + (i + 1));
        }
    }

    private boolean isEditTextFinished() {
        boolean result = true;
        for(int i=0;i<adapter.getItemCount();i++){
            MultiChoiceAdapter.ChoiceViewHolder viewHolder = (MultiChoiceAdapter.ChoiceViewHolder)
                    multiChoiceList.findViewHolderForAdapterPosition(i);
            if(adapter.options.get(i).equals("")) {
                if(viewHolder != null) {
                    viewHolder.multiChoiceEditText.clearFocus();
                    viewHolder.multiChoiceEditText.setError(getString(R.string.please_enter_choice));
                    Toast.makeText(getActivity(), R.string.please_enter_choice, Toast.LENGTH_LONG).show();
                }
                else
                    Toast.makeText(getActivity(),R.string.please_enter_choice,Toast.LENGTH_LONG).show();
                result = false;
            } if(adapter.options.get(i).length() > 150) {
                if(viewHolder != null) {
                    viewHolder.multiChoiceEditText.clearFocus();
                    viewHolder.multiChoiceEditText.setError(getString(R.string.cannot_exceed_150_chars));
                    Toast.makeText(getActivity(),R.string.cannot_exceed_150_chars,Toast.LENGTH_LONG).show();
                }
                else
                    Toast.makeText(getActivity(),R.string.cannot_exceed_150_chars,Toast.LENGTH_LONG).show();
                result = false;
            }
        }

        return result;
    }

    @Override
    public ArrayList<String> myAction() {
        if(isEditTextFinished())
            return adapter.options;
        else
            return null;

    }
}
