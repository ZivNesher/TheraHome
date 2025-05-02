package com.example.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class UserDetailsBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_USER = "user";
    private User user;

    public static UserDetailsBottomSheet newInstance(User user) {
        UserDetailsBottomSheet fragment = new UserDetailsBottomSheet();
        Bundle args = new Bundle();
        args.putSerializable(ARG_USER, user);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_details, container, false);

        if (getArguments() != null) {
            user = (User) getArguments().getSerializable(ARG_USER);
        }

        if (user != null) {
            TextView IDTextView = view.findViewById(R.id.id_user_details);
            TextView emailTextView = view.findViewById(R.id.email);
            TextView ageTextView = view.findViewById(R.id.age);
            TextView weightTextView = view.findViewById(R.id.weight);
            TextView heightTextView = view.findViewById(R.id.height);

            IDTextView.setText(user.Id);
            emailTextView.setText(user.Email);
            weightTextView.setText(user.weight);
            heightTextView.setText(user.height);

            int calculatedAge = user.getAge();
            ageTextView.setText(calculatedAge >= 0 ? String.valueOf(calculatedAge) : "N/A");
        }

        Button editButton = view.findViewById(R.id.edit_button);
        editButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ProfileCompletionActivity.class);
            intent.putExtra("userId", user.userId);
            intent.putExtra("email", user.Email);
            intent.putExtra("surname", user.surName);
            intent.putExtra("firstname", user.firstName);
            intent.putExtra("dateOfBirth", user.dateOfBirth);
            intent.putExtra("height", user.height);
            intent.putExtra("weight", user.weight);
            intent.putExtra("ID", user.Id);
            startActivity(intent);
            dismiss();
        });

        Button logoutButton = view.findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(v -> {
            if (getActivity() instanceof UserManagerCallback) {
                ((UserManagerCallback) getActivity()).goToLoginScreen();  // ✅ Callback instead of direct call
            }
            dismiss();
        });

        return view;
    }
}