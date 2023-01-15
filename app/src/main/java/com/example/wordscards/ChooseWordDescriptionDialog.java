package com.example.wordscards;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class ChooseWordDescriptionDialog extends DialogFragment {

    private OnLinkItemSelectedListener mListener;

    public interface OnLinkItemSelectedListener {
        void onLinkItemSelected(String link);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof ChooseWordDescriptionDialog.OnLinkItemSelectedListener) {
            mListener = (OnLinkItemSelectedListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement MyListFragment.OnItemSelectedListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = "Choose the most suitable definition";
        if (getArguments().getBoolean("isEmpty")) {
            title = "We didn't find anything :(";
        }
        final String[] variants = getArguments().getStringArray("array");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title)
                .setItems(variants, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onLinkItemSelected(variants[which]);
                    }
                });

        return builder.create();
    }
}
