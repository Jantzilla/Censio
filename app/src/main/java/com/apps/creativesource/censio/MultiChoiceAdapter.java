package com.apps.creativesource.censio;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

public class MultiChoiceAdapter extends RecyclerView.Adapter<MultiChoiceAdapter.ChoiceViewHolder> {
    private int itemCount;
    private final ListItemClickListener onClickListener;

    public interface ListItemClickListener {
        void onListItemClick(int clickedItemIndex);
    }

    public MultiChoiceAdapter(int numberOfOptions, ListItemClickListener listener) {
        itemCount = numberOfOptions;
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
        choiceViewHolder.multiChoiceEditText.setHint((R.string.option) + " " + (i + 1));
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
        }

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            int focusedPosition = getAdapterPosition();

            if(focusedPosition > 1) {
                if (hasFocus)
                    deleteImageView.setVisibility(View.VISIBLE);
                else
                    deleteImageView.setVisibility(View.GONE);
            }

        }
    }
}
