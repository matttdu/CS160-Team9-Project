package edu.sjsu.android.myapplication.ui.dashboard;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import edu.sjsu.android.myapplication.R;
import edu.sjsu.android.myapplication.SQLiteController;

public class PostDetailFragment extends Fragment {

    private int postId;
    private String titleText, authorText, contentText;
    private SQLiteController dbController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_post_detail, container, false);

        // Initialize DB controller
        dbController = new SQLiteController(requireContext());

        // Get post data from bundle
        if (getArguments() != null) {
            postId = getArguments().getInt("id", -1);
            titleText = getArguments().getString("title", "Untitled");
            authorText = getArguments().getString("author", "Unknown");
            contentText = getArguments().getString("content", "");
        }

        TextView title = view.findViewById(R.id.detailTitle);
        TextView author = view.findViewById(R.id.detailAuthor);
        TextView content = view.findViewById(R.id.detailContent);
        Button editButton = view.findViewById(R.id.editPostButton);
        LinearLayout commentSection = view.findViewById(R.id.commentSection);
        Button addCommentButton = view.findViewById(R.id.addCommentButton);

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

        loadComments(commentSection);
        // Add comment button
        addCommentButton.setOnClickListener(v -> showAddCommentDialog(commentSection));

        return view;
    }

    private void showEditDialog(LayoutInflater inflater, TextView titleView, TextView contentView, String loggedInUser) {
        View dialogView = inflater.inflate(R.layout.dialog_add_post, null);

        EditText editTitle = dialogView.findViewById(R.id.editPostTitle);
        EditText editContent = dialogView.findViewById(R.id.editPostContent);

        editTitle.setText(titleText);
        editContent.setText(contentText);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Edit Post")
                .setView(dialogView)
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                .setNeutralButton("Delete", null)
                .create();

        dialog.setOnShowListener(dlg -> {
            Button saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button deleteButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);

            // Save changes
            saveButton.setOnClickListener(v -> {
                String newTitle = editTitle.getText().toString().trim();
                String newContent = editContent.getText().toString().trim();

                if (newTitle.isEmpty() || newContent.isEmpty()) {
                    return; // skip saving if empty
                }

                boolean success = dbController.updatePostById(postId, newTitle, newContent, loggedInUser);
                if (success) {
                    titleView.setText(newTitle);
                    contentView.setText(newContent);
                    titleText = newTitle;
                    contentText = newContent;
                    dialog.dismiss();
                }
            });

            // Delete post
            deleteButton.setOnClickListener(v -> {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Delete Post")
                        .setMessage("Are you sure you want to delete this post?")
                        .setPositiveButton("Yes", (confirmDialog, which) -> {
                            boolean deleted = dbController.deletePostById(postId, loggedInUser);                            if (deleted) {
                                dialog.dismiss();
                                confirmDialog.dismiss();
                                // Go back to previous fragment
                                requireActivity().getSupportFragmentManager().popBackStack();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        });
        dialog.show();
    }
    private void loadComments(LinearLayout commentSection) {
        commentSection.removeAllViews();
        Cursor cursor = dbController.getComments(postId);

        while (cursor.moveToNext()) {
            String author = cursor.getString(cursor.getColumnIndexOrThrow(SQLiteController.COL_COMMENT_AUTHOR));
            String content = cursor.getString(cursor.getColumnIndexOrThrow(SQLiteController.COL_COMMENT_CONTENT));

            TextView commentView = new TextView(requireContext());
            commentView.setText(author + ": " + content);
            commentView.setTextSize(14f);
            commentView.setPadding(0, 4, 0, 4);
            commentSection.addView(commentView);
        }

        cursor.close();
    }
    private void showAddCommentDialog(LinearLayout commentSection) {
        EditText input = new EditText(requireContext());

        new AlertDialog.Builder(requireContext())
                .setTitle("Add Comment")
                .setView(input)
                .setPositiveButton("Post", (dialog, which) -> {
                    String comment = input.getText().toString().trim();

                    // Limit comment length to 5000 characters
                    int maxLength = 5000;
                    if (comment.isEmpty()) return; // skip empty comments
                    if (comment.length() > maxLength) {
                        comment = comment.substring(0, maxLength);
                    }

                    // Get logged-in user
                    SharedPreferences prefs = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                    String loggedInUser = prefs.getString("loggedInUser", "Anonymous");

                    dbController.addComment(postId, loggedInUser, comment);
                    loadComments(commentSection); // reload to show new comment
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}