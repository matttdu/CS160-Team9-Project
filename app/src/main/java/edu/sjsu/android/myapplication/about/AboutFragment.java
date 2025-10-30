package edu.sjsu.android.myapplication.about;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import edu.sjsu.android.myapplication.R;

public class AboutFragment extends Fragment {

    public AboutFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_about, container, false);

        // --- Version text ---
        TextView tvVersion = v.findViewById(R.id.tv_version);
        tvVersion.setText("Version 1.0");  // You can replace "1.0" with BuildConfig.VERSION_NAME later

        // --- GitHub Button ---
        Button btnGit = v.findViewById(R.id.btn_open_github);
        btnGit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("https://github.com/matttdu/CS160-Team9-Project");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        // --- Email Team Button ---
        Button btnEmail = v.findViewById(R.id.btn_email_team);
        btnEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto:team9.binsight@example.com"));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "BinSight Feedback");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "I’d like to share some feedback:");
                startActivity(Intent.createChooser(emailIntent, "Choose Email App"));
            }
        });

        return v;
    }
}
