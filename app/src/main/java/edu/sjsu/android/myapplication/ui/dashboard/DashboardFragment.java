package edu.sjsu.android.myapplication.ui.dashboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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

        // When Add button is clicked, show add post dialog
        binding.addForumButton.setOnClickListener(v -> showAddPostDialog(inflater));

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
                TextView author = postView.findViewById(R.id.postAuthor);

                title.setText(titleText);
                content.setText(contentText);
                author.setText("Posted by: " + authorText);

                binding.forumList.addView(postView);
            }
        }
        cursor.close();
    }
    private void showAddPostDialog(LayoutInflater inflater) {
        // Inflate dialog layout
        View dialogView = inflater.inflate(R.layout.dialog_add_post, null);

        EditText titleInput = dialogView.findViewById(R.id.editPostTitle);
        EditText contentInput = dialogView.findViewById(R.id.editPostContent);

        new AlertDialog.Builder(requireContext())
                .setTitle("Create a New Post")
                .setView(dialogView)
                .setPositiveButton("Post", (dialog, which) -> {
                    String titleText = titleInput.getText().toString().trim();
                    String contentText = contentInput.getText().toString().trim();

                    if (titleText.isEmpty() || contentText.isEmpty()) {
                        Toast.makeText(requireContext(), "Title and description cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Save to database
                    boolean success = dbController.addPost(titleText, contentText, loggedInUser);
                    if (success) {
                        Toast.makeText(requireContext(), "Post added!", Toast.LENGTH_SHORT).show();
                        loadForumPosts(inflater);
                    } else {
                        Toast.makeText(requireContext(), "Failed to add post.", Toast.LENGTH_SHORT).show();
                    }

                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
