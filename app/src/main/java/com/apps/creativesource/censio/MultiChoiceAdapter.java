package com.apps.creativesource.censio;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import java.util.ArrayList;

public class MultiChoiceAdapter extends RecyclerView.Adapter<MultiChoiceAdapter.ChoiceViewHolder> {
    public static int itemCount;
    private final ListItemClickListener onClickListener;
    public ArrayList<String> options = new ArrayList<>();

    public interface ListItemClickListener {
        void onListItemClick(int clickedItemIndex);
    }

    public MultiChoiceAdapter(int numberOfOptions, ListItemClickListener listener) {
        itemCount = numberOfOptions;
        options.add("");
        options.add("");

        onClickListener = listener;

    }

    @NonNull
    @Override
    public ChoiceViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = layoutInflater.inflate(R.layout.multi_choice_option, viewGroup, shouldAttachToParentImmediately);
        ChoiceViewHolder viewHolder = new ChoiceViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ChoiceViewHolder choiceViewHolder, int i) {
        choiceViewHolder.multiChoiceEditText.setHint("Option" + " " + (i + 1));

        choiceViewHolder.multiChoiceEditText.setText(options.get(choiceViewHolder.getAdapterPosition()));

        if(!options.get(choiceViewHolder.getAdapterPosition()).equals(""))
            choiceViewHolder.multiChoiceEditText.setError(null);

        choiceViewHolder.multiChoiceEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if(choiceViewHolder.getAdapterPosition() != -1) {
                    options.set(choiceViewHolder.getAdapterPosition(), s.toString());
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return itemCount;
    }

    class ChoiceViewHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener, View.OnFocusChangeListener {
        EditText multiChoiceEditText;
        ImageView deleteImageView;

        public ChoiceViewHolder(@NonNull View itemView) {
            super(itemView);

            multiChoiceEditText = itemView.findViewById(R.id.et_multi_choice);
            deleteImageView = itemView.findViewById(R.id.iv_delete);

            deleteImageView.setOnClickListener(this);
            multiChoiceEditText.setOnFocusChangeListener(this);

        }

        @Override
        public void onClick(View v) {
            int clickedPosition = getAdapterPosition();
            onClickListener.onListItemClick(clickedPosition);
            multiChoiceEditText.setText("");
            multiChoiceEditText.setError(null);
            deleteImageView.setVisibility(View.GONE);
        }

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            int focusedPosition = getAdapterPosition();

                if (hasFocus) {
                    if (focusedPosition > 1)
                        deleteImageView.setVisibility(View.VISIBLE);
                    multiChoiceEditText.setError(null);
                }
                else
                    deleteImageView.setVisibility(View.GONE);

        }
    }
}
