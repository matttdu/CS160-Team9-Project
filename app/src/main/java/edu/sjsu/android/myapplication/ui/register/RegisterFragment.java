package edu.sjsu.android.myapplication.ui.register;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import edu.sjsu.android.myapplication.R;
import edu.sjsu.android.myapplication.SQLiteController;
import edu.sjsu.android.myapplication.databinding.FragmentRegisterBinding;

import java.util.regex.*;

public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;
    private SQLiteController db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = SQLiteController.dbInstance(getContext());

        final EditText usernameEditText = binding.username;
        final EditText emailEditText = binding.email;
        final EditText passwordEditText = binding.password;
        final Button registerButton = binding.registerButton;
        final ProgressBar loadingProgressBar = binding.loading;

        // Enable register button only if fields are not empty
        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                boolean enabled = !usernameEditText.getText().toString().trim().isEmpty()
                        && !emailEditText.getText().toString().trim().isEmpty()
                        && !passwordEditText.getText().toString().trim().isEmpty();
                registerButton.setEnabled(enabled);
            }
        };

        usernameEditText.addTextChangedListener(afterTextChangedListener);
        emailEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);

        registerButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            Pattern pattern = Pattern.compile("\\b[A-Z0-9._%-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}\\b", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(email);

            if (!matcher.matches()) {
                Toast.makeText(getContext(), "Email is invalid", Toast.LENGTH_SHORT).show();
            } else {
                if (db.registerUser(username, email, password)) {
                    Toast.makeText(getContext(), "Registration successful!", Toast.LENGTH_SHORT).show();
                    // Navigate back to login
                    Navigation.findNavController(view).navigate(R.id.action_register_to_login);
                } else {
                    Toast.makeText(getContext(), "Username or email already exists", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button backButton = binding.backButton;
        backButton.setOnClickListener(v -> {
            Navigation.findNavController(v).navigateUp(); // goes back in the navigation stack
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
