package edu.sjsu.android.myapplication.ui.notifications;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.SharedPreferences;

import edu.sjsu.android.myapplication.SQLiteController;
import edu.sjsu.android.myapplication.databinding.FragmentNotificationsBinding;

import edu.sjsu.android.myapplication.R;

public class NotificationsFragment extends Fragment {
    private SQLiteController dbController;
    private FragmentNotificationsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_notifications, container, false);

        dbController = new SQLiteController(requireContext());

        String username = requireContext()
                .getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                .getString("loggedInUser", "");


        TextView usernameField = v.findViewById(R.id.profile_header);
        usernameField.setText("Welcome, " + username + "!");

        TextView upvoteField = v.findViewById(R.id.profile_num_upvotes);
        upvoteField.setText("Number of upvotes/downvotes: " + dbController.getMarkerRatingsCountByUser(username));

        TextView postsField = v.findViewById(R.id.profile_num_messages2);
        postsField.setText("Number of community messages posted: " + dbController.getPostCountByUser(username));

        TextView commentsField = v.findViewById(R.id.profile_num_messages3);
        commentsField.setText("Number of community messages commented on: " + dbController.getCommentCountByUser(username));


        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}