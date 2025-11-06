package edu.sjsu.android.myapplication.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import edu.sjsu.android.myapplication.R;

public class PostDetailFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_post_detail, container, false);

        TextView title = view.findViewById(R.id.detailTitle);
        TextView author = view.findViewById(R.id.detailAuthor);
        TextView content = view.findViewById(R.id.detailContent);

        // Get arguments from bundle
        if (getArguments() != null) {
            title.setText(getArguments().getString("title", "Untitled"));
            author.setText("Posted by: " + getArguments().getString("author", "Unknown"));
            content.setText(getArguments().getString("content", ""));
        }

        return view;
    }
}
