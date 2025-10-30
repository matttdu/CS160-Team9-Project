package edu.sjsu.android.myapplication.ui.dashboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import edu.sjsu.android.myapplication.SQLiteController;
import edu.sjsu.android.myapplication.databinding.FragmentDashboardBinding;
import edu.sjsu.android.myapplication.R;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private SQLiteController dbController;
    private String loggedInUser;

    @Override
    public @NonNull View onCreateView(@NonNull LayoutInflater inflater,
                                      @Nullable ViewGroup container,
                                      @Nullable Bundle savedInstanceState) {

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize DB controller
        dbController = new SQLiteController(requireContext());

        // Retrieving logged-in username from SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        loggedInUser = prefs.getString("loggedInUser", "Anonymous"); // fallback if missing

        // Load saved posts from SQLite
        loadForumPosts(inflater);

        // When Add button is clicked, add a mock post
        binding.addForumButton.setOnClickListener(v -> addMockForumPost(inflater));

        return root;
    }

    // Load all saved posts from SQLite and display them
    private void loadForumPosts(LayoutInflater inflater) {
        binding.forumList.removeAllViews(); // clear existing views
        Cursor cursor = dbController.getAllPosts();

        if (cursor.getCount() == 0) {
            Toast.makeText(requireContext(), "No posts yet. Add one!", Toast.LENGTH_SHORT).show();
        } else {
            while (cursor.moveToNext()) {
                String titleText = cursor.getString(cursor.getColumnIndexOrThrow(SQLiteController.COL_POST_TITLE));
                String contentText = cursor.getString(cursor.getColumnIndexOrThrow(SQLiteController.COL_POST_CONTENT));
                String authorText = cursor.getString(cursor.getColumnIndexOrThrow(SQLiteController.COL_POST_AUTHOR));

                View postView = inflater.inflate(R.layout.item_forum_post, binding.forumList, false);

                TextView title = postView.findViewById(R.id.postTitle);
                TextView content = postView.findViewById(R.id.postContent);

                title.setText(titleText);
                content.setText(contentText + "\n\nPosted by: " + authorText);

                binding.forumList.addView(postView);
            }
        }
        cursor.close();
    }
    private void addMockForumPost(LayoutInflater inflater) {
        int postNumber = binding.forumList.getChildCount() + 1;
        String titleText = "New Post #" + postNumber;
        String contentText = "This is a mock post added by tapping the Add button!";

        // Save to SQLite
        boolean success = dbController.addPost(titleText, contentText, loggedInUser);
        if (success) {
            Toast.makeText(requireContext(), "Post by " + loggedInUser + " saved!", Toast.LENGTH_SHORT).show();
            loadForumPosts(inflater); // refresh list
        } else {
            Toast.makeText(requireContext(), "Failed to save post.", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
