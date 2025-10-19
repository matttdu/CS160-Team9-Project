package edu.sjsu.android.myapplication.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import edu.sjsu.android.myapplication.databinding.FragmentDashboardBinding;
import edu.sjsu.android.myapplication.R;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;

    @Override
    public @NonNull View onCreateView(@NonNull LayoutInflater inflater,
                                      @Nullable ViewGroup container,
                                      @Nullable Bundle savedInstanceState) {

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // When Add button is clicked, add a mock post
        binding.addForumButton.setOnClickListener(v -> addMockForumPost(inflater));

        return root;
    }

    private void addMockForumPost(LayoutInflater inflater) {
        // Inflate the reusable forum post layout
        View postView = inflater.inflate(R.layout.item_forum_post, binding.forumList, false);

        // Update its contents
        TextView title = postView.findViewById(R.id.postTitle);
        TextView content = postView.findViewById(R.id.postContent);

        int postNumber = binding.forumList.getChildCount() + 1;
        title.setText("New Post #" + postNumber);
        content.setText("This is a mock post added by tapping the Add button!");

        // Add it to the forum list
        binding.forumList.addView(postView);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
