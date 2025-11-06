package edu.sjsu.android.myapplication.ui.dashboard;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import edu.sjsu.android.myapplication.R;
import edu.sjsu.android.myapplication.SQLiteController;

public class PostDetailFragment extends Fragment {

    private String titleText, authorText, contentText;
    private SQLiteController dbController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_post_detail, container, false);

        TextView title = view.findViewById(R.id.detailTitle);
        TextView author = view.findViewById(R.id.detailAuthor);
        TextView content = view.findViewById(R.id.detailContent);
        Button editButton = view.findViewById(R.id.editPostButton);

        dbController = new SQLiteController(requireContext());

        // Get post data from bundle
        if (getArguments() != null) {
            titleText = getArguments().getString("title", "Untitled");
            authorText = getArguments().getString("author", "Unknown");
            contentText = getArguments().getString("content", "");
        }

        title.setText(titleText);
        author.setText("Posted by: " + authorText);
        content.setText(contentText);

        // Get logged-in username
        SharedPreferences prefs = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String loggedInUser = prefs.getString("loggedInUser", "Anonymous");

        // Show edit button only if this user owns the post
        if (authorText.equals(loggedInUser)) {
            editButton.setVisibility(View.VISIBLE);
            editButton.setOnClickListener(v -> showEditDialog(inflater, title, content, loggedInUser));
        }

        return view;
    }

    private void showEditDialog(LayoutInflater inflater, TextView titleView, TextView contentView, String loggedInUser) {
        View dialogView = inflater.inflate(R.layout.dialog_add_post, null);

        EditText editTitle = dialogView.findViewById(R.id.editPostTitle);
        EditText editContent = dialogView.findViewById(R.id.editPostContent);

        editTitle.setText(titleText);
        editContent.setText(contentText);

        new AlertDialog.Builder(requireContext())
                .setTitle("Edit Post")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newTitle = editTitle.getText().toString().trim();
                    String newContent = editContent.getText().toString().trim();

                    if (newTitle.isEmpty() || newContent.isEmpty()) {
                        Toast.makeText(requireContext(), "Fields cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    boolean success = dbController.updatePost(titleText, newTitle, newContent, loggedInUser);
                    if (success) {
                        Toast.makeText(requireContext(), "Post updated!", Toast.LENGTH_SHORT).show();
                        titleView.setText(newTitle);
                        contentView.setText(newContent);
                        titleText = newTitle;
                        contentText = newContent;
                    } else {
                        Toast.makeText(requireContext(), "Failed to update post.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
